package br.uerj.erp.web.session;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("sessionContext")
@SessionScoped
public class SessionContext implements Serializable {

    private SessionUser currentUser;

    public SessionUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(SessionUser currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().contains(role);
    }

    public boolean hasAnyRole(String... roles) {
        if (currentUser == null || currentUser.getRoles() == null) {
            return false;
        }
        List<String> currentRoles = currentUser.getRoles();
        for (String role : roles) {
            if (currentRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public String getUsername() {
        return currentUser == null ? null : currentUser.getUsername();
    }

    public String getFullName() {
        return currentUser == null ? null : currentUser.getFullName();
    }

    public void clear() {
        currentUser = null;
    }
}
