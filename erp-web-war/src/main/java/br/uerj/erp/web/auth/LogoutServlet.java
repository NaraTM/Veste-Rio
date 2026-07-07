package br.uerj.erp.web.auth;

import br.uerj.erp.web.session.SessionContext;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionContext sessionContext = CDI.current().select(SessionContext.class).get();
        sessionContext.clear();
        request.getSession().invalidate();
        response.sendRedirect(request.getContextPath() + "/login.xhtml");
    }
}
