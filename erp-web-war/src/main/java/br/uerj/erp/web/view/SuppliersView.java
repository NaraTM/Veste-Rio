package br.uerj.erp.web.view;

import br.uerj.erp.procurement.ProcurementServiceLocal;
import br.uerj.erp.shared.domain.Supplier;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.List;

@Named
@ViewScoped
public class SuppliersView extends BasePage {

    @EJB
    private ProcurementServiceLocal procurementService;

    private List<Supplier> suppliers;
    private Supplier form;

    @PostConstruct
    public void init() {
        reset();
        load();
    }

    public void load() {
        suppliers = procurementService.findAllSuppliers();
    }

    public void edit(Supplier supplier) {
        if (!hasRole("ADMIN")) {
            Messages.error("Apenas administrador pode editar fornecedores");
            return;
        }
        form = new Supplier();
        form.setName(supplier.getName());
        form.setContactName(supplier.getContactName());
        form.setEmail(supplier.getEmail());
        form.setPhone(supplier.getPhone());
        form.setActive(supplier.isActive());
        form.setId(supplier.getId());
    }

    public void save() {
        procurementService.saveSupplier(form);
        Messages.info("Fornecedor salvo");
        reset();
        load();
    }

    public void delete(Long id) {
        if (!hasRole("ADMIN")) {
            Messages.error("Apenas administrador pode excluir fornecedores");
            return;
        }
        boolean removed = procurementService.removeSupplier(id);
        if (removed) {
            if (form != null && id.equals(form.getId())) {
                reset();
            }
            Messages.info("Fornecedor removido");
        } else {
            Messages.error("Fornecedor vinculado a produtos ou compras");
        }
        load();
    }

    public void reset() {
        form = new Supplier();
        form.setActive(true);
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public Supplier getForm() {
        return form;
    }

    public void setForm(Supplier form) {
        this.form = form;
    }
}
