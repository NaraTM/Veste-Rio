package br.uerj.erp.web.view;

import br.uerj.erp.inventory.InventoryServiceLocal;
import br.uerj.erp.procurement.ProcurementServiceLocal;
import br.uerj.erp.shared.domain.Product;
import br.uerj.erp.shared.domain.Purchase;
import br.uerj.erp.shared.domain.Supplier;
import br.uerj.erp.shared.dto.PurchaseItemInput;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class PurchasesView extends BasePage {

    public static class PurchaseRow implements Serializable {
        private Long productId;
        private BigDecimal quantity = BigDecimal.ONE;
        private BigDecimal unitCost = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

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

    @EJB
    private ProcurementServiceLocal procurementService;

    @EJB
    private InventoryServiceLocal inventoryService;

    private List<Purchase> purchases;
    private List<Supplier> suppliers;
    private List<Product> products;
    private Long supplierId;
    private LocalDate purchaseDate;
    private List<PurchaseRow> rows;

    @PostConstruct
    public void init() {
        rows = new ArrayList<>();
        rows.add(new PurchaseRow());
        purchaseDate = LocalDate.now();
        load();
    }

    public void load() {
        purchases = procurementService.findAllPurchases();
        suppliers = procurementService.findAllSuppliers();
        products = inventoryService.findAllProducts();
    }

    public void addRow() {
        rows.add(new PurchaseRow());
    }

    public void onSupplierChange() {
        for (PurchaseRow row : rows) {
            if (row.getProductId() != null && findProduct(row.getProductId()) == null) {
                row.setProductId(null);
                row.setUnitCost(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    public void onProductChange(PurchaseRow row) {
        if (row == null) {
            return;
        }
        row.setUnitCost(resolveUnitCost(row));
        if (row.getQuantity() == null || row.getQuantity().compareTo(BigDecimal.ONE) < 0) {
            row.setQuantity(BigDecimal.ONE);
        }
    }

    public void save() {
        List<PurchaseItemInput> items = rows.stream()
                .filter(row -> row.getProductId() != null)
                .map(row -> {
                    PurchaseItemInput input = new PurchaseItemInput();
                    input.setProductId(row.getProductId());
                    input.setQuantity(normalizeInteger(row.getQuantity()));
                    input.setUnitCost(resolveUnitCost(row));
                    return input;
                })
                .filter(input -> input.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (supplierId == null) {
            Messages.error("Selecione o fornecedor");
            return;
        }
        if (items.isEmpty()) {
            Messages.error("Informe ao menos um item para a compra");
            return;
        }
        try {
            procurementService.createPurchase(supplierId, purchaseDate, items);
            Messages.info("Compra registrada");
            supplierId = null;
            purchaseDate = LocalDate.now();
            rows = new ArrayList<>();
            rows.add(new PurchaseRow());
            load();
        } catch (IllegalArgumentException exception) {
            Messages.error("Há item vinculado a outro fornecedor");
        }
    }

    public BigDecimal calculateRowTotal(PurchaseRow row) {
        if (row == null || row.getProductId() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal quantity = normalizeInteger(row.getQuantity());
        return resolveUnitCost(row).multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getGrandTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseRow row : rows) {
            total = total.add(calculateRowTotal(row));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public String describeProduct(Product product) {
        if (product == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (product.getIconName() != null && !product.getIconName().isBlank()) {
            builder.append(product.getIconName()).append(' ');
        }
        builder.append(product.getName());
        if (product.getColorName() != null && !product.getColorName().isBlank()) {
            builder.append(" - ").append(capitalize(product.getColorName()));
        }
        if (product.getSizeLabel() != null && !product.getSizeLabel().isBlank()) {
            builder.append(" - ").append(product.getSizeLabel());
        }
        return builder.toString();
    }

    public String describePurchaseStatus(br.uerj.erp.shared.domain.PurchaseStatus value) {
        return switch (value) {
            case OPEN -> "Aberta";
            case RECEIVED -> "Recebida";
            case CANCELLED -> "Cancelada";
        };
    }

    public List<Product> getAvailableProducts() {
        if (products == null) {
            return List.of();
        }
        if (supplierId == null) {
            return List.of();
        }
        return products.stream()
                .filter(item -> item.getSupplier() != null && supplierId.equals(item.getSupplier().getId()))
                .toList();
    }

    private BigDecimal resolveUnitCost(PurchaseRow row) {
        if (row == null || row.getProductId() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        Product product = findProduct(row.getProductId());
        if (product == null || product.getUnitPrice() == null) {
            row.setProductId(null);
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        row.setUnitCost(product.getUnitPrice().setScale(2, RoundingMode.HALF_UP));
        return row.getUnitCost();
    }

    private Product findProduct(Long productId) {
        if (productId == null || products == null) {
            return null;
        }
        return products.stream()
                .filter(item -> productId.equals(item.getId()))
                .filter(item -> supplierId == null || item.getSupplier() != null && supplierId.equals(item.getSupplier().getId()))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal normalizeInteger(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(0, RoundingMode.DOWN);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String lower = value.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public List<PurchaseRow> getRows() {
        return rows;
    }

    public void setRows(List<PurchaseRow> rows) {
        this.rows = rows;
    }
}
