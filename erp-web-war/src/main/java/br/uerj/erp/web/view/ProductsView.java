package br.uerj.erp.web.view;

import br.uerj.erp.inventory.InventoryServiceLocal;
import br.uerj.erp.procurement.ProcurementServiceLocal;
import br.uerj.erp.shared.domain.DiscountCoupon;
import br.uerj.erp.shared.domain.Product;
import br.uerj.erp.shared.domain.StockMovement;
import br.uerj.erp.shared.domain.StockMovementType;
import br.uerj.erp.shared.domain.Supplier;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class ProductsView extends BasePage {

    @EJB
    private InventoryServiceLocal inventoryService;

    @EJB
    private ProcurementServiceLocal procurementService;

    private List<Product> products;
    private List<StockMovement> movements;
    private List<DiscountCoupon> coupons;
    private List<Supplier> suppliers;
    private Product form;
    private DiscountCoupon couponForm;
    private Long adjustmentProductId;
    private BigDecimal adjustmentQuantity = BigDecimal.ONE;
    private StockMovementType adjustmentType = StockMovementType.ADJUSTMENT;
    private String adjustmentNote = "Ajuste manual";

    @PostConstruct
    public void init() {
        reset();
        resetCouponForm();
        load();
    }

    public void load() {
        products = inventoryService.findAllProducts();
        movements = inventoryService.listMovements();
        coupons = inventoryService.listCoupons();
        suppliers = procurementService.findAllSuppliers();
    }

    public void edit(Product product) {
        if (!hasRole("ADMIN")) {
            Messages.error("Apenas administrador pode editar itens do estoque");
            return;
        }
        Product loaded = inventoryService.findProduct(product.getId());
        if (loaded == null) {
            Messages.error("Item não encontrado");
            return;
        }
        form = new Product();
        form.setId(loaded.getId());
        form.setSku(loaded.getSku());
        form.setName(loaded.getName());
        form.setDescription(loaded.getDescription());
        Supplier supplier = new Supplier();
        if (loaded.getSupplier() != null) {
            supplier.setId(loaded.getSupplier().getId());
            supplier.setName(loaded.getSupplier().getName());
        }
        form.setSupplier(supplier);
        form.setUnitPrice(loaded.getUnitPrice());
        form.setStockQuantity(loaded.getStockQuantity());
        form.setMinStock(loaded.getMinStock());
        form.setCategory(loaded.getCategory());
        form.setProductType(loaded.getProductType());
        form.setTargetAudience(loaded.getTargetAudience());
        form.setColorName(loaded.getColorName());
        form.setSizeLabel(loaded.getSizeLabel());
        form.setIconName(loaded.getIconName());
        form.setActive(loaded.isActive());
    }

    public void save() {
        if (!hasRole("ADMIN")) {
            Messages.error("Apenas administrador pode salvar itens do estoque");
            return;
        }
        normalizeForm();
        if (!validateForm()) {
            return;
        }
        form.setIconName(resolveIcon(form.getProductType()));
        inventoryService.saveProduct(form);
        Messages.info("Item salvo");
        reset();
        load();
    }

    public void delete(Long id) {
        if (!hasRole("ADMIN")) {
            Messages.error("Apenas administrador pode excluir itens do estoque");
            return;
        }
        boolean removed = inventoryService.removeProduct(id);
        if (removed) {
            if (form != null && id.equals(form.getId())) {
                reset();
            }
            Messages.info("Item removido");
        } else {
            Messages.error("Item vinculado a compras ou vendas");
        }
        load();
    }

    public void adjust() {
        if (adjustmentProductId == null) {
            Messages.error("Selecione o item do estoque");
            return;
        }
        BigDecimal quantity = normalizeInteger(adjustmentQuantity);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            Messages.error("Quantidade inválida");
            return;
        }
        inventoryService.adjustStock(adjustmentProductId, quantity, adjustmentType, adjustmentNote, "MANUAL");
        Messages.info("Estoque ajustado");
        adjustmentProductId = null;
        adjustmentQuantity = BigDecimal.ONE;
        load();
    }

    public void saveCoupon() {
        if (!canManageCoupons()) {
            Messages.error("Apenas administrador ou gerente pode salvar cupons");
            return;
        }
        normalizeCouponForm();
        if (!validateCouponForm()) {
            return;
        }
        inventoryService.saveCoupon(couponForm);
        Messages.info("Cupom salvo");
        resetCouponForm();
        load();
    }

    public void editCoupon(DiscountCoupon coupon) {
        if (!canManageCoupons()) {
            Messages.error("Apenas administrador ou gerente pode editar cupons");
            return;
        }
        couponForm = new DiscountCoupon();
        couponForm.setId(coupon.getId());
        couponForm.setCode(coupon.getCode());
        couponForm.setDescription(coupon.getDescription());
        couponForm.setDiscountPercent(coupon.getDiscountPercent());
        couponForm.setValidUntil(coupon.getValidUntil());
        couponForm.setActive(coupon.isActive());
    }

    public void deleteCoupon(Long id) {
        if (!canManageCoupons()) {
            Messages.error("Apenas administrador ou gerente pode excluir cupons");
            return;
        }
        boolean removed = inventoryService.removeCoupon(id);
        if (removed) {
            if (couponForm != null && id.equals(couponForm.getId())) {
                resetCouponForm();
            }
            Messages.info("Cupom removido");
        } else {
            Messages.error("Cupom não encontrado");
        }
        load();
    }

    public void reset() {
        form = new Product();
        form.setActive(true);
        form.setSupplier(new Supplier());
        form.setUnitPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        form.setStockQuantity(BigDecimal.ZERO);
        form.setMinStock(BigDecimal.ZERO);
        form.setCategory(null);
        form.setProductType(null);
        form.setTargetAudience(null);
        form.setColorName(null);
        form.setSizeLabel(null);
        form.setIconName(null);
    }

    public void resetCouponForm() {
        couponForm = new DiscountCoupon();
        couponForm.setActive(true);
        couponForm.setDiscountPercent(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        couponForm.setValidUntil(LocalDate.now().plusMonths(1));
    }

    public void onGroupChange() {
        if (!getProductTypeOptions().containsValue(form.getProductType())) {
            form.setProductType(null);
        }
        form.setSizeLabel(null);
        form.setIconName(resolveIcon(form.getProductType()));
    }

    public void onProductTypeChange() {
        if (!isSizeAllowed(form.getProductType(), form.getSizeLabel())) {
            form.setSizeLabel(null);
        }
        form.setIconName(resolveIcon(form.getProductType()));
    }

    public boolean canManageCoupons() {
        return hasRole("ADMIN") || hasRole("MANAGER");
    }

    public Map<String, String> getGroupOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("Superior", "SUPERIOR");
        options.put("Inferior", "INFERIOR");
        options.put("Íntimo", "INTIMO");
        options.put("Calçado", "CALCADO");
        return options;
    }

    public Map<String, String> getProductTypeOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        String group = form == null ? null : form.getCategory();
        if ("SUPERIOR".equals(group)) {
            options.put("Camiseta", "CAMISETA");
            options.put("Camisa", "CAMISA");
            options.put("Polo", "POLO");
            options.put("Regata", "REGATA");
            options.put("Moletom", "MOLETOM");
            options.put("Jaqueta", "JAQUETA");
            options.put("Vestido", "VESTIDO");
        }
        if ("INFERIOR".equals(group)) {
            options.put("Calça jeans", "CALCA_JEANS");
            options.put("Calça social", "CALCA_SOCIAL");
            options.put("Bermuda", "BERMUDA");
            options.put("Short", "SHORT");
            options.put("Saia", "SAIA");
        }
        if ("INTIMO".equals(group)) {
            options.put("Cueca", "CUECA");
            options.put("Calcinha", "CALCINHA");
            options.put("Sutiã", "SUTIA");
            options.put("Meia", "MEIA");
        }
        if ("CALCADO".equals(group)) {
            options.put("Tênis", "TENIS");
            options.put("Sapato", "SAPATO");
            options.put("Chinelo", "CHINELO");
            options.put("Sandália", "SANDALIA");
        }
        return options;
    }

    public Map<String, String> getTargetAudienceOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("Masculino", "MASCULINO");
        options.put("Feminino", "FEMININO");
        options.put("Unissex", "UNISSEX");
        options.put("Infantil", "INFANTIL");
        return options;
    }

    public Map<String, String> getColorOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("Preto", "PRETO");
        options.put("Branco", "BRANCO");
        options.put("Azul", "AZUL");
        options.put("Vermelho", "VERMELHO");
        options.put("Verde", "VERDE");
        options.put("Rosa", "ROSA");
        options.put("Cinza", "CINZA");
        options.put("Bege", "BEGE");
        options.put("Marrom", "MARROM");
        return options;
    }

    public Map<String, String> getSizeOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        for (String value : getSizeValues(form == null ? null : form.getProductType())) {
            options.put(value, value);
        }
        return options;
    }

    public String getSizeFieldLabel() {
        if (form != null && ("TENIS".equals(form.getProductType()) || "SAPATO".equals(form.getProductType()) || "CHINELO".equals(form.getProductType()) || "SANDALIA".equals(form.getProductType()))) {
            return "Numeração";
        }
        return "Tamanho";
    }

    public String resolveIcon(String productType) {
        if (productType == null || productType.isBlank()) {
            return null;
        }
        return switch (productType) {
            case "CAMISETA" -> "👕";
            case "CAMISA" -> "👔";
            case "POLO" -> "👕";
            case "REGATA" -> "🎽";
            case "MOLETOM" -> "🧥";
            case "JAQUETA" -> "🧥";
            case "VESTIDO" -> "👗";
            case "CALCA_JEANS" -> "👖";
            case "CALCA_SOCIAL" -> "👖";
            case "BERMUDA" -> "🩳";
            case "SHORT" -> "🩳";
            case "SAIA" -> "👗";
            case "CUECA" -> "🩲";
            case "CALCINHA" -> "🩲";
            case "SUTIA" -> "👙";
            case "MEIA" -> "🧦";
            case "TENIS" -> "👟";
            case "SAPATO" -> "👞";
            case "CHINELO" -> "🩴";
            case "SANDALIA" -> "👡";
            default -> null;
        };
    }

    public String describeGroup(String value) {
        return switch (value) {
            case "SUPERIOR" -> "Superior";
            case "INFERIOR" -> "Inferior";
            case "INTIMO" -> "Íntimo";
            case "CALCADO" -> "Calçado";
            default -> value;
        };
    }

    public String describeType(String value) {
        return switch (value) {
            case "CAMISETA" -> "Camiseta";
            case "CAMISA" -> "Camisa";
            case "POLO" -> "Polo";
            case "REGATA" -> "Regata";
            case "MOLETOM" -> "Moletom";
            case "JAQUETA" -> "Jaqueta";
            case "VESTIDO" -> "Vestido";
            case "CALCA_JEANS" -> "Calça jeans";
            case "CALCA_SOCIAL" -> "Calça social";
            case "BERMUDA" -> "Bermuda";
            case "SHORT" -> "Short";
            case "SAIA" -> "Saia";
            case "CUECA" -> "Cueca";
            case "CALCINHA" -> "Calcinha";
            case "SUTIA" -> "Sutiã";
            case "MEIA" -> "Meia";
            case "TENIS" -> "Tênis";
            case "SAPATO" -> "Sapato";
            case "CHINELO" -> "Chinelo";
            case "SANDALIA" -> "Sandália";
            default -> value;
        };
    }

    public String describeColor(String value) {
        return capitalize(value);
    }

    public String describeAudience(String value) {
        return switch (value) {
            case "MASCULINO" -> "Masculino";
            case "FEMININO" -> "Feminino";
            case "UNISSEX" -> "Unissex";
            case "INFANTIL" -> "Infantil";
            default -> value;
        };
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

    public String describeCouponStatus(DiscountCoupon coupon) {
        if (!coupon.isActive()) {
            return "Inativo";
        }
        if (coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDate.now())) {
            return "Expirado";
        }
        return "Ativo";
    }

    public String describeSupplier(Supplier supplier) {
        return supplier == null ? "" : supplier.getName();
    }

    private void normalizeForm() {
        form.setSku(form.getSku() == null ? null : form.getSku().trim());
        form.setName(form.getName() == null ? null : form.getName().trim());
        form.setDescription(form.getDescription() == null ? null : form.getDescription().trim());
        form.setUnitPrice(normalizeMoney(form.getUnitPrice()));
        form.setStockQuantity(normalizeInteger(form.getStockQuantity()));
        form.setMinStock(normalizeInteger(form.getMinStock()));
        form.setIconName(resolveIcon(form.getProductType()));
    }

    private boolean validateForm() {
        if (form.getSupplier() == null || form.getSupplier().getId() == null) {
            Messages.error("Selecione o fornecedor do item");
            return false;
        }
        if (isBlank(form.getSku())) {
            Messages.error("Informe o SKU do item");
            return false;
        }
        if (isBlank(form.getName())) {
            Messages.error("Informe o nome do item");
            return false;
        }
        if (isBlank(form.getCategory())) {
            Messages.error("Selecione o grupo do item");
            return false;
        }
        if (isBlank(form.getProductType())) {
            Messages.error("Selecione o tipo do item");
            return false;
        }
        if (!getProductTypeOptions().containsValue(form.getProductType())) {
            Messages.error("Tipo incompatível com o grupo");
            return false;
        }
        if (isBlank(form.getTargetAudience())) {
            Messages.error("Selecione o público do item");
            return false;
        }
        if (isBlank(form.getColorName())) {
            Messages.error("Selecione a cor do item");
            return false;
        }
        if (isBlank(form.getSizeLabel())) {
            Messages.error(getSizeFieldLabel() + " é obrigatório");
            return false;
        }
        if (!isSizeAllowed(form.getProductType(), form.getSizeLabel())) {
            Messages.error(getSizeFieldLabel() + " incompatível com o tipo");
            return false;
        }
        if (form.getStockQuantity().compareTo(BigDecimal.ZERO) < 0 || form.getMinStock().compareTo(BigDecimal.ZERO) < 0) {
            Messages.error("Estoque e mínimo devem ser inteiros não negativos");
            return false;
        }
        return true;
    }

    private void normalizeCouponForm() {
        couponForm.setCode(couponForm.getCode() == null ? null : couponForm.getCode().trim().toUpperCase());
        couponForm.setDescription(couponForm.getDescription() == null ? null : couponForm.getDescription().trim());
        couponForm.setDiscountPercent(normalizePercent(couponForm.getDiscountPercent()));
    }

    private boolean validateCouponForm() {
        if (isBlank(couponForm.getCode())) {
            Messages.error("Informe o código do cupom");
            return false;
        }
        if (couponForm.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0) {
            Messages.error("Informe um percentual de desconto válido");
            return false;
        }
        DiscountCoupon existing = inventoryService.findCouponByCode(couponForm.getCode());
        if (existing != null && (couponForm.getId() == null || !existing.getId().equals(couponForm.getId()))) {
            Messages.error("Já existe um cupom com esse código");
            return false;
        }
        return true;
    }

    private boolean isSizeAllowed(String productType, String sizeLabel) {
        if (isBlank(productType) || isBlank(sizeLabel)) {
            return false;
        }
        for (String value : getSizeValues(productType)) {
            if (value.equals(sizeLabel)) {
                return true;
            }
        }
        return false;
    }

    private String[] getSizeValues(String productType) {
        if (productType == null || productType.isBlank()) {
            return new String[0];
        }
        return switch (productType) {
            case "CAMISETA", "CAMISA", "POLO", "REGATA", "MOLETOM", "JAQUETA", "VESTIDO", "CUECA", "CALCINHA" -> new String[]{"PP", "P", "M", "G", "GG", "XG"};
            case "CALCA_JEANS", "CALCA_SOCIAL", "BERMUDA", "SHORT", "SAIA" -> new String[]{"34", "36", "38", "40", "42", "44", "46", "48", "50"};
            case "TENIS", "SAPATO", "CHINELO", "SANDALIA" -> new String[]{"34", "35", "36", "37", "38", "39", "40", "41", "42", "43"};
            case "SUTIA" -> new String[]{"40", "42", "44", "46", "48"};
            case "MEIA" -> new String[]{"34-38", "39-43", "Único"};
            default -> new String[0];
        };
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String lower = value.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizePercent(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (value.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100.00");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeInteger(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(0, RoundingMode.DOWN);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<StockMovement> getMovements() {
        return movements;
    }

    public List<DiscountCoupon> getCoupons() {
        return coupons;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public Product getForm() {
        return form;
    }

    public void setForm(Product form) {
        this.form = form;
    }

    public DiscountCoupon getCouponForm() {
        return couponForm;
    }

    public void setCouponForm(DiscountCoupon couponForm) {
        this.couponForm = couponForm;
    }

    public Long getAdjustmentProductId() {
        return adjustmentProductId;
    }

    public void setAdjustmentProductId(Long adjustmentProductId) {
        this.adjustmentProductId = adjustmentProductId;
    }

    public BigDecimal getAdjustmentQuantity() {
        return adjustmentQuantity;
    }

    public void setAdjustmentQuantity(BigDecimal adjustmentQuantity) {
        this.adjustmentQuantity = adjustmentQuantity;
    }

    public StockMovementType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(StockMovementType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public String getAdjustmentNote() {
        return adjustmentNote;
    }

    public void setAdjustmentNote(String adjustmentNote) {
        this.adjustmentNote = adjustmentNote;
    }

    public StockMovementType[] getMovementTypes() {
        return StockMovementType.values();
    }
}
