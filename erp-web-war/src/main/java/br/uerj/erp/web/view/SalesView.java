package br.uerj.erp.web.view;

import br.uerj.erp.inventory.InventoryServiceLocal;
import br.uerj.erp.sales.SalesServiceLocal;
import br.uerj.erp.shared.domain.AppUser;
import br.uerj.erp.shared.domain.DiscountCoupon;
import br.uerj.erp.shared.domain.PaymentMethod;
import br.uerj.erp.shared.domain.Product;
import br.uerj.erp.shared.domain.Sale;
import br.uerj.erp.shared.dto.SaleItemInput;
import br.uerj.erp.users.UserServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class SalesView extends BasePage {

    public static class SaleRow implements Serializable {
        private Long productId;
        private BigDecimal quantity = BigDecimal.ONE;
        private BigDecimal unitPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

    @EJB
    private SalesServiceLocal salesService;

    @EJB
    private InventoryServiceLocal inventoryService;

    @EJB
    private UserServiceLocal userService;

    private List<Sale> sales;
    private List<Product> products;
    private List<DiscountCoupon> coupons;
    private List<SaleRow> rows;
    private LocalDate saleDate;
    private PaymentMethod paymentMethod;
    private BigDecimal discountPercent;
    private String couponCode;

    @PostConstruct
    public void init() {
        rows = new ArrayList<>();
        rows.add(new SaleRow());
        saleDate = LocalDate.now();
        paymentMethod = PaymentMethod.CREDIT_CARD;
        discountPercent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        couponCode = null;
        load();
    }

    public void load() {
        sales = salesService.findAllSales();
        products = inventoryService.findAllProducts();
        coupons = inventoryService.listCoupons();
    }

    public void addRow() {
        rows.add(new SaleRow());
    }

    public void onProductChange(SaleRow row) {
        if (row == null) {
            return;
        }
        row.setUnitPrice(resolveUnitPrice(row));
        if (row.getQuantity() == null || row.getQuantity().compareTo(BigDecimal.ONE) < 0) {
            row.setQuantity(BigDecimal.ONE);
        }
    }

    public void onCouponChange() {
        DiscountCoupon coupon = getSelectedCoupon();
        if (coupon != null) {
            discountPercent = normalizePercent(coupon.getDiscountPercent());
        } else if (!canManageDiscount()) {
            discountPercent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public void save() {
        AppUser seller = userService.findByUsername(sessionContext().getUsername());
        List<SaleItemInput> items = rows.stream()
                .filter(row -> row.getProductId() != null)
                .map(row -> {
                    SaleItemInput input = new SaleItemInput();
                    input.setProductId(row.getProductId());
                    input.setQuantity(normalizeInteger(row.getQuantity()));
                    input.setUnitPrice(resolveUnitPrice(row));
                    input.setPaymentMethod(paymentMethod);
                    return input;
                })
                .filter(input -> input.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (items.isEmpty()) {
            Messages.error("Informe ao menos um item para a venda");
            return;
        }
        BigDecimal appliedPercent = resolveAppliedDiscountPercent();
        String appliedCouponCode = resolveAppliedCouponCode();
        salesService.createSale(seller.getId(), saleDate, items, paymentMethod, appliedPercent, appliedCouponCode);
        Messages.info("Venda registrada");
        rows = new ArrayList<>();
        rows.add(new SaleRow());
        saleDate = LocalDate.now();
        paymentMethod = PaymentMethod.CREDIT_CARD;
        discountPercent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        couponCode = null;
        load();
    }

    public boolean canManageDiscount() {
        return hasRole("ADMIN") || hasRole("MANAGER");
    }

    public boolean canViewHistory() {
        return hasRole("ADMIN") || hasRole("MANAGER");
    }

    public BigDecimal calculateRowTotal(SaleRow row) {
        if (row == null || row.getProductId() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal quantity = normalizeInteger(row.getQuantity());
        return resolveUnitPrice(row).multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getGrossTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleRow row : rows) {
            total = total.add(calculateRowTotal(row));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDiscountAmount() {
        return getGrossTotal().multiply(resolveAppliedDiscountPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getNetTotal() {
        return getGrossTotal().subtract(getDiscountAmount()).setScale(2, RoundingMode.HALF_UP);
    }

    public String describeProduct(Product product) {
        if (product == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (product.getIconName() != null && !product.getIconName().isBlank()) {
            builder.append(product.getIconName()).append(' ');
        }
        builder.append(product.getName());
        if (product.getColorName() != null && !product.getColorName().isBlank()) {
            builder.append(" - ").append(capitalize(product.getColorName()));
        }
        if (product.getSizeLabel() != null && !product.getSizeLabel().isBlank()) {
            builder.append(" - ").append(product.getSizeLabel());
        }
        return builder.toString();
    }

    public Map<String, PaymentMethod> getPaymentMethodOptions() {
        Map<String, PaymentMethod> options = new LinkedHashMap<>();
        options.put("Cartão de crédito", PaymentMethod.CREDIT_CARD);
        options.put("Cartão de débito", PaymentMethod.DEBIT_CARD);
        options.put("PIX", PaymentMethod.PIX);
        options.put("Boleto", PaymentMethod.BOLETO);
        return options;
    }

    public Map<String, String> getCouponOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        if (coupons == null) {
            return options;
        }
        for (DiscountCoupon coupon : coupons) {
            if (isCouponAvailable(coupon)) {
                options.put(coupon.getCode() + " - " + coupon.getDiscountPercent().setScale(2, RoundingMode.HALF_UP) + "%", coupon.getCode());
            }
        }
        return options;
    }

    public String describePaymentMethod(PaymentMethod value) {
        return switch (value) {
            case CREDIT_CARD -> "Cartão de crédito";
            case DEBIT_CARD -> "Cartão de débito";
            case PIX -> "PIX";
            case BOLETO -> "Boleto";
        };
    }

    public String describeSaleStatus(br.uerj.erp.shared.domain.SaleStatus value) {
        return switch (value) {
            case OPEN -> "Aberta";
            case PAID -> "Paga";
            case CANCELLED -> "Cancelada";
        };
    }

    private BigDecimal resolveUnitPrice(SaleRow row) {
        if (row == null || row.getProductId() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        Product product = findProduct(row.getProductId());
        if (product == null || product.getUnitPrice() == null) {
            row.setProductId(null);
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        row.setUnitPrice(product.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
        return row.getUnitPrice();
    }

    private Product findProduct(Long productId) {
        if (productId == null || products == null) {
            return null;
        }
        return products.stream()
                .filter(item -> productId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    private DiscountCoupon getSelectedCoupon() {
        if (couponCode == null || couponCode.isBlank() || coupons == null) {
            return null;
        }
        return coupons.stream()
                .filter(this::isCouponAvailable)
                .filter(item -> item.getCode().equalsIgnoreCase(couponCode.trim()))
                .findFirst()
                .orElse(null);
    }

    private boolean isCouponAvailable(DiscountCoupon coupon) {
        return coupon != null
                && coupon.isActive()
                && coupon.getDiscountPercent() != null
                && coupon.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0
                && (coupon.getValidUntil() == null || !coupon.getValidUntil().isBefore(LocalDate.now()));
    }

    private BigDecimal resolveAppliedDiscountPercent() {
        DiscountCoupon coupon = getSelectedCoupon();
        if (coupon != null) {
            return normalizePercent(coupon.getDiscountPercent());
        }
        if (canManageDiscount()) {
            return normalizePercent(discountPercent);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveAppliedCouponCode() {
        DiscountCoupon coupon = getSelectedCoupon();
        return coupon == null ? null : coupon.getCode();
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

    private BigDecimal normalizeInteger(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(0, RoundingMode.DOWN);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String lower = value.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public List<Sale> getSales() {
        return sales;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<DiscountCoupon> getCoupons() {
        return coupons;
    }

    public List<SaleRow> getRows() {
        return rows;
    }

    public void setRows(List<SaleRow> rows) {
        this.rows = rows;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
