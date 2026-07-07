package br.uerj.erp.shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class PurchaseItemInput implements Serializable {
    private Long productId;
    private BigDecimal quantity = BigDecimal.ONE;
    private BigDecimal unitCost = BigDecimal.ZERO;

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

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
