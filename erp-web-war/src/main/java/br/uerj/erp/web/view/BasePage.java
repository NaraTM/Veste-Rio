package br.uerj.erp.web.view;

import br.uerj.erp.web.session.SessionContext;
import jakarta.enterprise.inject.spi.CDI;
import java.io.Serializable;

public abstract class BasePage implements Serializable {

    protected SessionContext sessionContext() {
        return CDI.current().select(SessionContext.class).get();
    }

    public String getLoggedUser() {
        return sessionContext().getFullName();
    }

    public boolean hasRole(String role) {
        return sessionContext().hasRole(role);
    }
}
