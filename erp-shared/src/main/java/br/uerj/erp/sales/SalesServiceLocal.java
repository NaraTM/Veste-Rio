package br.uerj.erp.sales;

import br.uerj.erp.shared.domain.Payment;
import br.uerj.erp.shared.domain.PaymentMethod;
import br.uerj.erp.shared.domain.PaymentStatus;
import br.uerj.erp.shared.domain.Sale;
import br.uerj.erp.shared.dto.SaleItemInput;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Local
public interface SalesServiceLocal {
    List<Sale> findAllSales();
    Sale createSale(Long sellerId, LocalDate saleDate, List<SaleItemInput> items, PaymentMethod paymentMethod, BigDecimal discountPercent, String couponCode);
    List<Payment> listPayments();
    void updatePaymentStatus(Long paymentId, PaymentStatus status);
}
