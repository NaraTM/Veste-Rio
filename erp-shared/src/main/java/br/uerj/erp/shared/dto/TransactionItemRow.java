package br.uerj.erp.shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionItemRow implements Serializable {
    private Long documentId;
    private LocalDate referenceDate;
    private String partnerName;
    private String sku;
    private String productName;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal unitAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public TransactionItemRow() {
    }

    public TransactionItemRow(Long documentId, LocalDate referenceDate, String partnerName, String sku, String productName, BigDecimal quantity, BigDecimal unitAmount, BigDecimal totalAmount) {
        this.documentId = documentId;
        this.referenceDate = referenceDate;
        this.partnerName = partnerName;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitAmount = unitAmount;
        this.totalAmount = totalAmount;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(BigDecimal unitAmount) {
        this.unitAmount = unitAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
