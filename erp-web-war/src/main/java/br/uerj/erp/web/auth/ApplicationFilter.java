package br.uerj.erp.web.auth;

import br.uerj.erp.web.session.SessionContext;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebFilter("/app/*")
public class ApplicationFilter extends HttpFilter implements Filter {

    private static final Map<String, List<String>> RULES = Map.of(
            "/app/users.xhtml", List.of("ADMIN"),
            "/app/products.xhtml", List.of("ADMIN", "MANAGER"),
            "/app/suppliers.xhtml", List.of("ADMIN", "MANAGER"),
            "/app/purchases.xhtml", List.of("ADMIN", "MANAGER"),
            "/app/reports.xhtml", List.of("ADMIN", "MANAGER"),
            "/app/dashboard.xhtml", List.of("ADMIN", "MANAGER"),
            "/app/sales.xhtml", List.of("ADMIN", "MANAGER", "SELLER"),
            "/app/payments.xhtml", List.of("ADMIN", "MANAGER")
    );

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        SessionContext sessionContext = CDI.current().select(SessionContext.class).get();
        if (!sessionContext.isAuthenticated()) {
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
            return;
        }
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        List<String> acceptedRoles = RULES.get(uri);
        if (acceptedRoles != null && acceptedRoles.stream().noneMatch(sessionContext.getCurrentUser().getRoles()::contains)) {
            response.sendRedirect(request.getContextPath() + "/denied.xhtml");
            return;
        }
        chain.doFilter(request, response);
    }
}
