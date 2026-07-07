package br.uerj.erp.shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class MonthlyFinancialRow implements Serializable {
    private String referenceMonth;
    private BigDecimal amount = BigDecimal.ZERO;

    public MonthlyFinancialRow() {
    }

    public MonthlyFinancialRow(String referenceMonth, BigDecimal amount) {
        this.referenceMonth = referenceMonth;
        this.amount = amount;
    }

    public String getReferenceMonth() {
        return referenceMonth;
    }

    public void setReferenceMonth(String referenceMonth) {
        this.referenceMonth = referenceMonth;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
