package br.uerj.erp.inventory;

import br.uerj.erp.shared.domain.DiscountCoupon;
import br.uerj.erp.shared.domain.Product;
import br.uerj.erp.shared.domain.StockMovement;
import br.uerj.erp.shared.domain.StockMovementType;
import jakarta.ejb.Local;
import java.math.BigDecimal;
import java.util.List;

@Local
public interface InventoryServiceLocal {
    List<Product> findAllProducts();
    Product findProduct(Long id);
    Product saveProduct(Product product);
    boolean removeProduct(Long id);
    void adjustStock(Long productId, BigDecimal quantity, StockMovementType type, String note, String referenceType);
    List<StockMovement> listMovements();
    List<Product> findLowStockProducts();
    List<DiscountCoupon> listCoupons();
    DiscountCoupon findCouponByCode(String code);
    DiscountCoupon saveCoupon(DiscountCoupon coupon);
    boolean removeCoupon(Long id);
    long countProducts();
}
