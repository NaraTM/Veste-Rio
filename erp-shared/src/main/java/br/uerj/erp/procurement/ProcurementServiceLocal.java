package br.uerj.erp.procurement;

import br.uerj.erp.shared.dto.PurchaseItemInput;
import br.uerj.erp.shared.domain.Purchase;
import br.uerj.erp.shared.domain.Supplier;
import jakarta.ejb.Local;
import java.time.LocalDate;
import java.util.List;

@Local
public interface ProcurementServiceLocal {
    List<Supplier> findAllSuppliers();
    Supplier findSupplier(Long id);
    Supplier saveSupplier(Supplier supplier);
    boolean removeSupplier(Long id);
    List<Purchase> findAllPurchases();
    Purchase createPurchase(Long supplierId, LocalDate purchaseDate, List<PurchaseItemInput> items);
    long countSuppliers();
}
