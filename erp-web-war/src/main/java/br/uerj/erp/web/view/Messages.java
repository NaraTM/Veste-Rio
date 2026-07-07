package br.uerj.erp.web.view;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

public final class Messages {

    private Messages() {
    }

    public static void info(String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, text, text));
    }

    public static void error(String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, text, text));
    }
}
