package br.uerj.erp.web.view;

import br.uerj.erp.sales.SalesServiceLocal;
import br.uerj.erp.shared.domain.Payment;
import br.uerj.erp.shared.domain.PaymentMethod;
import br.uerj.erp.shared.domain.PaymentStatus;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class PaymentsView extends BasePage {

    @EJB
    private SalesServiceLocal salesService;

    private List<Payment> payments;

    @PostConstruct
    public void init() {
        load();
    }

    public void load() {
        payments = salesService.listPayments();
    }

    public void update(Long paymentId, PaymentStatus status) {
        salesService.updatePaymentStatus(paymentId, status);
        Messages.info("Pagamento atualizado");
        load();
    }

    public String describePaymentMethod(PaymentMethod value) {
        return switch (value) {
            case CREDIT_CARD -> "Cartão de crédito";
            case DEBIT_CARD -> "Cartão de débito";
            case PIX -> "PIX";
            case BOLETO -> "Boleto";
        };
    }

    public String describePaymentStatus(PaymentStatus value) {
        return switch (value) {
            case PENDING -> "Pendente";
            case PAID -> "Pago";
            case CANCELLED -> "Cancelado";
        };
    }

    public Map<String, PaymentStatus> getStatusOptions() {
        Map<String, PaymentStatus> options = new LinkedHashMap<>();
        options.put("Pendente", PaymentStatus.PENDING);
        options.put("Pago", PaymentStatus.PAID);
        options.put("Cancelado", PaymentStatus.CANCELLED);
        return options;
    }

    public List<Payment> getPayments() {
        return payments;
    }
}
