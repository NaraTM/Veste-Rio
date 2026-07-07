package br.uerj.erp.shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductBalanceRow implements Serializable {
    private String sku;
    private String productName;
    private BigDecimal stock = BigDecimal.ZERO;
    private BigDecimal minStock = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;

    public ProductBalanceRow() {
    }

    public ProductBalanceRow(String sku, String productName, BigDecimal stock, BigDecimal minStock, BigDecimal unitPrice) {
        this.sku = sku;
        this.productName = productName;
        this.stock = stock;
        this.minStock = minStock;
        this.unitPrice = unitPrice;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public BigDecimal getMinStock() {
        return minStock;
    }

    public void setMinStock(BigDecimal minStock) {
        this.minStock = minStock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
