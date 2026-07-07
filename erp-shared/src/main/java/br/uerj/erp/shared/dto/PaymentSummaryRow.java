package br.uerj.erp.shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentSummaryRow implements Serializable {
    private String method;
    private BigDecimal amount = BigDecimal.ZERO;

    public PaymentSummaryRow() {
    }

    public PaymentSummaryRow(String method, BigDecimal amount) {
        this.method = method;
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
