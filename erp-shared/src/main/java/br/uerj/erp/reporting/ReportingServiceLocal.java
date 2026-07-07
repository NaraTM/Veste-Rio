package br.uerj.erp.reporting;

import br.uerj.erp.shared.dto.DashboardMetrics;
import br.uerj.erp.shared.dto.MonthlyFinancialRow;
import br.uerj.erp.shared.dto.PaymentSummaryRow;
import br.uerj.erp.shared.dto.ProductBalanceRow;
import br.uerj.erp.shared.dto.TransactionItemRow;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ReportingServiceLocal {
    DashboardMetrics dashboard();
    List<MonthlyFinancialRow> monthlySales();
    List<MonthlyFinancialRow> monthlyPurchases();
    List<PaymentSummaryRow> paymentsByMethod();
    List<ProductBalanceRow> lowStockReport();
    List<TransactionItemRow> soldItemsReport();
    List<TransactionItemRow> purchasedItemsReport();
}
