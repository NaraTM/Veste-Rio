package br.uerj.erp.sales;

import br.uerj.erp.inventory.InventoryServiceLocal;
import br.uerj.erp.shared.domain.AppUser;
import br.uerj.erp.shared.domain.Payment;
import br.uerj.erp.shared.domain.PaymentMethod;
import br.uerj.erp.shared.domain.PaymentStatus;
import br.uerj.erp.shared.domain.Product;
import br.uerj.erp.shared.domain.Sale;
import br.uerj.erp.shared.domain.SaleItem;
import br.uerj.erp.shared.domain.SaleStatus;
import br.uerj.erp.shared.domain.StockMovementType;
import br.uerj.erp.shared.dto.SaleItemInput;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Stateless
public class SalesServiceBean implements SalesServiceLocal {

    @PersistenceContext(unitName = "erpPU")
    private EntityManager entityManager;

    @EJB
    private InventoryServiceLocal inventoryService;

    @Override
    public List<Sale> findAllSales() {
        return entityManager.createQuery("select distinct s from Sale s join fetch s.seller left join fetch s.items left join fetch s.payments order by s.saleDate desc", Sale.class).getResultList();
    }

    @Override
    public Sale createSale(Long sellerId, LocalDate saleDate, List<SaleItemInput> items, PaymentMethod paymentMethod, BigDecimal discountPercent, String couponCode) {
        AppUser seller = entityManager.find(AppUser.class, sellerId);
        Sale sale = new Sale();
        sale.setSeller(seller);
        sale.setSaleDate(saleDate);
        sale.setStatus(SaleStatus.OPEN);
        sale.setDiscountPercent(BigDecimal.ZERO);
        sale.setDiscountValue(BigDecimal.ZERO);
        sale.setCouponCode(null);
        sale.setTotalValue(BigDecimal.ZERO);
        entityManager.persist(sale);

        BigDecimal grossTotal = BigDecimal.ZERO;
        for (SaleItemInput input : items) {
            Product product = entityManager.find(Product.class, input.getProductId());
            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(input.getQuantity());
            item.setUnitPrice(input.getUnitPrice());
            item.setSubtotal(input.getQuantity().multiply(input.getUnitPrice()));
            entityManager.persist(item);
            sale.getItems().add(item);
            grossTotal = grossTotal.add(item.getSubtotal());
            inventoryService.adjustStock(product.getId(), input.getQuantity(), StockMovementType.OUTBOUND, "Venda - Vendedor " + seller.getFullName(), "SALE");
        }

        BigDecimal appliedPercent = normalizePercent(discountPercent);
        BigDecimal appliedDiscount = grossTotal.multiply(appliedPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (appliedDiscount.compareTo(grossTotal) > 0) {
            appliedDiscount = grossTotal;
        }
        BigDecimal netTotal = grossTotal.subtract(appliedDiscount).setScale(2, RoundingMode.HALF_UP);

        Payment payment = new Payment();
        payment.setSale(sale);
        payment.setMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(saleDate);
        payment.setAmount(netTotal);
        entityManager.persist(payment);
        sale.getPayments().add(payment);
        sale.setDiscountPercent(appliedPercent);
        sale.setDiscountValue(appliedDiscount);
        sale.setCouponCode(couponCode == null || couponCode.isBlank() ? null : couponCode.trim().toUpperCase());
        sale.setTotalValue(netTotal);
        sale.setStatus(SaleStatus.PAID);
        sale = entityManager.merge(sale);
        return sale;
    }

    @Override
    public List<Payment> listPayments() {
        return entityManager.createQuery("select p from Payment p join fetch p.sale s join fetch s.seller order by p.paymentDate desc", Payment.class).getResultList();
    }

    @Override
    public void updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = entityManager.find(Payment.class, paymentId);
        if (payment != null) {
            payment.setStatus(status);
            entityManager.merge(payment);
        }
    }

    private BigDecimal normalizePercent(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (value.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100.00");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
