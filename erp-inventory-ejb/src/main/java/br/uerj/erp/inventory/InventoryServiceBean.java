package br.uerj.erp.inventory;

import br.uerj.erp.shared.domain.DiscountCoupon;
import br.uerj.erp.shared.domain.Product;
import br.uerj.erp.shared.domain.StockMovement;
import br.uerj.erp.shared.domain.StockMovementType;
import br.uerj.erp.shared.domain.Supplier;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class InventoryServiceBean implements InventoryServiceLocal {

    @PersistenceContext(unitName = "erpPU")
    private EntityManager entityManager;

    @Override
    public List<Product> findAllProducts() {
        return entityManager.createQuery("select distinct p from Product p left join fetch p.supplier order by p.name", Product.class).getResultList();
    }

    @Override
    public Product findProduct(Long id) {
        List<Product> result = entityManager.createQuery("select p from Product p left join fetch p.supplier where p.id = :id", Product.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public Product saveProduct(Product product) {
        Product managed = product.getId() == null ? new Product() : entityManager.find(Product.class, product.getId());
        Supplier supplier = resolveSupplier(product);
        managed.setSku(product.getSku());
        managed.setName(product.getName());
        managed.setDescription(product.getDescription());
        managed.setSupplier(supplier);
        managed.setUnitPrice(product.getUnitPrice());
        managed.setStockQuantity(product.getStockQuantity());
        managed.setMinStock(product.getMinStock());
        managed.setCategory(product.getCategory());
        managed.setProductType(product.getProductType());
        managed.setTargetAudience(product.getTargetAudience());
        managed.setColorName(product.getColorName());
        managed.setSizeLabel(product.getSizeLabel());
        managed.setIconName(product.getIconName());
        managed.setActive(product.isActive());
        if (managed.getId() == null) {
            entityManager.persist(managed);
        } else {
            managed = entityManager.merge(managed);
        }
        return managed;
    }

    @Override
    public boolean removeProduct(Long id) {
        Product product = entityManager.find(Product.class, id);
        if (product == null) {
            return false;
        }
        Long purchaseItems = entityManager.createQuery("select count(i) from PurchaseItem i where i.product.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        Long saleItems = entityManager.createQuery("select count(i) from SaleItem i where i.product.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        if (purchaseItems > 0 || saleItems > 0) {
            return false;
        }
        entityManager.createQuery("delete from StockMovement m where m.product.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        entityManager.remove(product);
        return true;
    }

    @Override
    public void adjustStock(Long productId, BigDecimal quantity, StockMovementType type, String note, String referenceType) {
        Product product = entityManager.find(Product.class, productId);
        if (product == null) {
            return;
        }
        BigDecimal current = product.getStockQuantity() == null ? BigDecimal.ZERO : product.getStockQuantity();
        if (type == StockMovementType.INBOUND || type == StockMovementType.ADJUSTMENT && quantity.signum() > 0) {
            product.setStockQuantity(current.add(quantity));
        } else {
            product.setStockQuantity(current.subtract(quantity.abs()));
        }
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity.abs());
        movement.setNote(note);
        movement.setReferenceType(referenceType);
        entityManager.persist(movement);
        entityManager.merge(product);
    }

    @Override
    public List<StockMovement> listMovements() {
        return entityManager.createQuery("select m from StockMovement m join fetch m.product p left join fetch p.supplier order by m.movedAt desc", StockMovement.class)
                .setMaxResults(200)
                .getResultList();
    }

    @Override
    public List<Product> findLowStockProducts() {
        return entityManager.createQuery("select p from Product p left join fetch p.supplier where p.stockQuantity <= p.minStock order by p.name", Product.class).getResultList();
    }

    @Override
    public List<DiscountCoupon> listCoupons() {
        return entityManager.createQuery("select c from DiscountCoupon c order by c.code", DiscountCoupon.class).getResultList();
    }

    @Override
    public DiscountCoupon findCouponByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        List<DiscountCoupon> result = entityManager.createQuery("select c from DiscountCoupon c where upper(c.code) = :code", DiscountCoupon.class)
                .setParameter("code", code.trim().toUpperCase())
                .setMaxResults(1)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public DiscountCoupon saveCoupon(DiscountCoupon coupon) {
        DiscountCoupon managed = coupon.getId() == null ? new DiscountCoupon() : entityManager.find(DiscountCoupon.class, coupon.getId());
        managed.setCode(coupon.getCode() == null ? null : coupon.getCode().trim().toUpperCase());
        managed.setDescription(coupon.getDescription());
        managed.setDiscountPercent(coupon.getDiscountPercent());
        managed.setValidUntil(coupon.getValidUntil());
        managed.setActive(coupon.isActive());
        if (managed.getId() == null) {
            entityManager.persist(managed);
        } else {
            managed = entityManager.merge(managed);
        }
        return managed;
    }

    @Override
    public boolean removeCoupon(Long id) {
        DiscountCoupon coupon = entityManager.find(DiscountCoupon.class, id);
        if (coupon == null) {
            return false;
        }
        Long usages = entityManager.createQuery("select count(s) from Sale s where upper(s.couponCode) = :code", Long.class)
                .setParameter("code", coupon.getCode().toUpperCase())
                .getSingleResult();
        if (usages > 0) {
            coupon.setActive(false);
            entityManager.merge(coupon);
            return true;
        }
        entityManager.remove(coupon);
        return true;
    }

    @Override
    public long countProducts() {
        return entityManager.createQuery("select count(p) from Product p", Long.class).getSingleResult();
    }

    private Supplier resolveSupplier(Product product) {
        if (product == null || product.getSupplier() == null || product.getSupplier().getId() == null) {
            return null;
        }
        return entityManager.find(Supplier.class, product.getSupplier().getId());
    }
}
