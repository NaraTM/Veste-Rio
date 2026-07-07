package br.uerj.erp.procurement;

import br.uerj.erp.inventory.InventoryServiceLocal;
import br.uerj.erp.shared.domain.Product;
import br.uerj.erp.shared.domain.Purchase;
import br.uerj.erp.shared.domain.PurchaseItem;
import br.uerj.erp.shared.domain.PurchaseStatus;
import br.uerj.erp.shared.domain.StockMovementType;
import br.uerj.erp.shared.domain.Supplier;
import br.uerj.erp.shared.dto.PurchaseItemInput;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Stateless
public class ProcurementServiceBean implements ProcurementServiceLocal {

    @PersistenceContext(unitName = "erpPU")
    private EntityManager entityManager;

    @EJB
    private InventoryServiceLocal inventoryService;

    @Override
    public List<Supplier> findAllSuppliers() {
        return entityManager.createQuery("select s from Supplier s order by s.name", Supplier.class).getResultList();
    }

    @Override
    public Supplier findSupplier(Long id) {
        return entityManager.find(Supplier.class, id);
    }

    @Override
    public Supplier saveSupplier(Supplier supplier) {
        Supplier managed = supplier.getId() == null ? new Supplier() : entityManager.find(Supplier.class, supplier.getId());
        managed.setName(supplier.getName());
        managed.setContactName(supplier.getContactName());
        managed.setEmail(supplier.getEmail());
        managed.setPhone(supplier.getPhone());
        managed.setActive(supplier.isActive());
        if (managed.getId() == null) {
            entityManager.persist(managed);
        } else {
            managed = entityManager.merge(managed);
        }
        return managed;
    }

    @Override
    public boolean removeSupplier(Long id) {
        Supplier supplier = entityManager.find(Supplier.class, id);
        if (supplier == null) {
            return false;
        }
        Long purchases = entityManager.createQuery("select count(p) from Purchase p where p.supplier.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        Long products = entityManager.createQuery("select count(p) from Product p where p.supplier.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        if (purchases > 0 || products > 0) {
            return false;
        }
        entityManager.remove(supplier);
        return true;
    }

    @Override
    public List<Purchase> findAllPurchases() {
        return entityManager.createQuery("select distinct p from Purchase p join fetch p.supplier left join fetch p.items order by p.purchaseDate desc", Purchase.class).getResultList();
    }

    @Override
    public Purchase createPurchase(Long supplierId, LocalDate purchaseDate, List<PurchaseItemInput> items) {
        Supplier supplier = entityManager.find(Supplier.class, supplierId);
        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setStatus(PurchaseStatus.RECEIVED);
        purchase.setTotalValue(BigDecimal.ZERO);
        entityManager.persist(purchase);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseItemInput input : items) {
            Product product = entityManager.find(Product.class, input.getProductId());
            if (product != null && product.getSupplier() == null) {
                product.setSupplier(supplier);
                entityManager.merge(product);
            }
            if (product != null && product.getSupplier() != null && !supplier.getId().equals(product.getSupplier().getId())) {
                throw new IllegalArgumentException("Produto vinculado a outro fornecedor");
            }
            PurchaseItem item = new PurchaseItem();
            item.setPurchase(purchase);
            item.setProduct(product);
            item.setQuantity(input.getQuantity());
            item.setUnitCost(input.getUnitCost());
            item.setSubtotal(input.getQuantity().multiply(input.getUnitCost()));
            entityManager.persist(item);
            purchase.getItems().add(item);
            total = total.add(item.getSubtotal());
            inventoryService.adjustStock(product.getId(), input.getQuantity(), StockMovementType.INBOUND, "Compra - Fornecedor " + supplier.getName(), "PURCHASE");
        }
        purchase.setTotalValue(total);
        purchase = entityManager.merge(purchase);
        return purchase;
    }

    @Override
    public long countSuppliers() {
        return entityManager.createQuery("select count(s) from Supplier s", Long.class).getSingleResult();
    }
}
