package br.uerj.erp.web.view;

import br.uerj.erp.reporting.ReportingServiceLocal;
import br.uerj.erp.shared.dto.DashboardMetrics;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DashboardView extends BasePage {

    @EJB
    private ReportingServiceLocal reportingService;

    private DashboardMetrics metrics;

    @PostConstruct
    public void init() {
        metrics = reportingService.dashboard();
    }

    public DashboardMetrics getMetrics() {
        return metrics;
    }
}
