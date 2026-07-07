package br.uerj.erp.web.view;

import br.uerj.erp.shared.domain.AppUser;
import br.uerj.erp.shared.domain.RoleName;
import br.uerj.erp.users.UserServiceLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named
@ViewScoped
public class UsersView extends BasePage {

    @EJB
    private UserServiceLocal userService;

    private List<AppUser> users;
    private AppUser form;
    private List<RoleName> selectedRoles;
    private String rawPassword;

    @PostConstruct
    public void init() {
        reset();
        load();
    }

    public void load() {
        users = userService.findAllUsers();
    }

    public void edit(AppUser user) {
        form = new AppUser();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setFullName(user.getFullName());
        form.setActive(user.isActive());
        selectedRoles = user.getRoles().stream().map(role -> role.getName()).toList();
        rawPassword = "";
    }

    public void save() {
        userService.saveUser(form, selectedRoles, rawPassword);
        Messages.info("Usuário salvo");
        reset();
        load();
    }

    public void delete(Long id) {
        userService.removeUser(id);
        Messages.info("Usuário removido");
        load();
    }

    public void reset() {
        form = new AppUser();
        form.setActive(true);
        selectedRoles = new ArrayList<>(List.of(RoleName.SELLER));
        rawPassword = "";
    }

    public List<RoleName> getAvailableRoles() {
        return Arrays.asList(RoleName.values());
    }

    public List<AppUser> getUsers() {
        return users;
    }

    public AppUser getForm() {
        return form;
    }

    public void setForm(AppUser form) {
        this.form = form;
    }

    public List<RoleName> getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(List<RoleName> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }
}
