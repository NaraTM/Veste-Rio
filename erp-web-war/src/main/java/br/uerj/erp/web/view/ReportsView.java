package br.uerj.erp.web.view;

import br.uerj.erp.reporting.ReportingServiceLocal;
import br.uerj.erp.shared.dto.MonthlyFinancialRow;
import br.uerj.erp.shared.dto.PaymentSummaryRow;
import br.uerj.erp.shared.dto.ProductBalanceRow;
import br.uerj.erp.shared.dto.TransactionItemRow;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.List;

@Named
@ViewScoped
public class ReportsView extends BasePage {

    @EJB
    private ReportingServiceLocal reportingService;

    private List<MonthlyFinancialRow> monthlySales;
    private List<MonthlyFinancialRow> monthlyPurchases;
    private List<PaymentSummaryRow> paymentSummary;
    private List<ProductBalanceRow> lowStock;
    private List<TransactionItemRow> soldItems;
    private List<TransactionItemRow> purchasedItems;

    @PostConstruct
    public void init() {
        monthlySales = reportingService.monthlySales();
        monthlyPurchases = reportingService.monthlyPurchases();
        paymentSummary = reportingService.paymentsByMethod();
        lowStock = reportingService.lowStockReport();
        soldItems = reportingService.soldItemsReport();
        purchasedItems = reportingService.purchasedItemsReport();
    }

    public String describePaymentMethod(String value) {
        return switch (value) {
            case "CREDIT_CARD" -> "Cartão de crédito";
            case "DEBIT_CARD" -> "Cartão de débito";
            case "PIX" -> "PIX";
            case "BOLETO" -> "Boleto";
            default -> value;
        };
    }

    public List<MonthlyFinancialRow> getMonthlySales() {
        return monthlySales;
    }

    public List<MonthlyFinancialRow> getMonthlyPurchases() {
        return monthlyPurchases;
    }

    public List<PaymentSummaryRow> getPaymentSummary() {
        return paymentSummary;
    }

    public List<ProductBalanceRow> getLowStock() {
        return lowStock;
    }

    public List<TransactionItemRow> getSoldItems() {
        return soldItems;
    }

    public List<TransactionItemRow> getPurchasedItems() {
        return purchasedItems;
    }
}
