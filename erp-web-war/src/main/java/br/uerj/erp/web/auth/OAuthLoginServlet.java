package br.uerj.erp.web.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@WebServlet("/login")
public class OAuthLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String state = UUID.randomUUID().toString();
        request.getSession(true).setAttribute("oauth_state", state);
        String authorizeUrl = OAuthSettings.authPublicBaseUrl()
                + "/oauth2/authorize?response_type=code"
                + "&client_id=" + url(OAuthSettings.clientId())
                + "&scope=" + url("openid profile")
                + "&redirect_uri=" + url(OAuthSettings.redirectUri())
                + "&state=" + url(state);
        response.sendRedirect(authorizeUrl);
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
