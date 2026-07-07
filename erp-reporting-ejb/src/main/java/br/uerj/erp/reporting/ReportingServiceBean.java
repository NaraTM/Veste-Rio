package br.uerj.erp.reporting;

import br.uerj.erp.shared.dto.DashboardMetrics;
import br.uerj.erp.shared.dto.MonthlyFinancialRow;
import br.uerj.erp.shared.dto.PaymentSummaryRow;
import br.uerj.erp.shared.dto.ProductBalanceRow;
import br.uerj.erp.shared.dto.TransactionItemRow;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class ReportingServiceBean implements ReportingServiceLocal {

    @PersistenceContext(unitName = "erpPU")
    private EntityManager entityManager;

    @Override
    public DashboardMetrics dashboard() {
        DashboardMetrics metrics = new DashboardMetrics();
        metrics.setTotalUsers(entityManager.createQuery("select count(u) from AppUser u", Long.class).getSingleResult());
        metrics.setTotalProducts(entityManager.createQuery("select count(p) from Product p", Long.class).getSingleResult());
        metrics.setLowStockProducts(entityManager.createQuery("select count(p) from Product p where p.stockQuantity <= p.minStock", Long.class).getSingleResult());
        metrics.setSalesTotal(entityManager.createQuery("select coalesce(sum(s.totalValue), 0) from Sale s", BigDecimal.class).getSingleResult());
        metrics.setPurchasesTotal(entityManager.createQuery("select coalesce(sum(p.totalValue), 0) from Purchase p", BigDecimal.class).getSingleResult());
        metrics.setOpenPayments(entityManager.createQuery("select coalesce(sum(p.amount), 0) from Payment p where p.status <> br.uerj.erp.shared.domain.PaymentStatus.PAID", BigDecimal.class).getSingleResult());
        return metrics;
    }

    @Override
    public List<MonthlyFinancialRow> monthlySales() {
        List<Object[]> rows = entityManager.createNativeQuery("select to_char(sale_date, 'YYYY-MM') as reference_month, coalesce(sum(total_value), 0) as amount from sales group by to_char(sale_date, 'YYYY-MM') order by reference_month").getResultList();
        return rows.stream().map(row -> new MonthlyFinancialRow(String.valueOf(row[0]), (BigDecimal) row[1])).toList();
    }

    @Override
    public List<MonthlyFinancialRow> monthlyPurchases() {
        List<Object[]> rows = entityManager.createNativeQuery("select to_char(purchase_date, 'YYYY-MM') as reference_month, coalesce(sum(total_value), 0) as amount from purchases group by to_char(purchase_date, 'YYYY-MM') order by reference_month").getResultList();
        return rows.stream().map(row -> new MonthlyFinancialRow(String.valueOf(row[0]), (BigDecimal) row[1])).toList();
    }

    @Override
    public List<PaymentSummaryRow> paymentsByMethod() {
        List<Object[]> rows = entityManager.createNativeQuery("select method, coalesce(sum(amount), 0) as amount from payments group by method order by method").getResultList();
        return rows.stream().map(row -> new PaymentSummaryRow(String.valueOf(row[0]), (BigDecimal) row[1])).toList();
    }

    @Override
    public List<ProductBalanceRow> lowStockReport() {
        return entityManager.createQuery("select new br.uerj.erp.shared.dto.ProductBalanceRow(p.sku, p.name, p.stockQuantity, p.minStock, p.unitPrice) from Product p where p.stockQuantity <= p.minStock order by p.name", ProductBalanceRow.class).getResultList();
    }

    @Override
    public List<TransactionItemRow> soldItemsReport() {
        return entityManager.createQuery("select new br.uerj.erp.shared.dto.TransactionItemRow(s.id, s.saleDate, s.seller.fullName, p.sku, p.name, i.quantity, i.unitPrice, i.subtotal) from SaleItem i join i.sale s join i.product p order by s.saleDate desc, s.id desc, i.id desc", TransactionItemRow.class)
                .setMaxResults(300)
                .getResultList();
    }

    @Override
    public List<TransactionItemRow> purchasedItemsReport() {
        return entityManager.createQuery("select new br.uerj.erp.shared.dto.TransactionItemRow(c.id, c.purchaseDate, c.supplier.name, p.sku, p.name, i.quantity, i.unitCost, i.subtotal) from PurchaseItem i join i.purchase c join i.product p order by c.purchaseDate desc, c.id desc, i.id desc", TransactionItemRow.class)
                .setMaxResults(300)
                .getResultList();
    }
}
