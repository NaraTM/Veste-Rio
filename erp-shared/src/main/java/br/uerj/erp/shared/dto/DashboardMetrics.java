package br.uerj.erp.shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class DashboardMetrics implements Serializable {
    private long totalUsers;
    private long totalProducts;
    private long lowStockProducts;
    private BigDecimal salesTotal = BigDecimal.ZERO;
    private BigDecimal purchasesTotal = BigDecimal.ZERO;
    private BigDecimal openPayments = BigDecimal.ZERO;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public BigDecimal getSalesTotal() {
        return salesTotal;
    }

    public void setSalesTotal(BigDecimal salesTotal) {
        this.salesTotal = salesTotal;
    }

    public BigDecimal getPurchasesTotal() {
        return purchasesTotal;
    }

    public void setPurchasesTotal(BigDecimal purchasesTotal) {
        this.purchasesTotal = purchasesTotal;
    }

    public BigDecimal getOpenPayments() {
        return openPayments;
    }

    public void setOpenPayments(BigDecimal openPayments) {
        this.openPayments = openPayments;
    }
}
