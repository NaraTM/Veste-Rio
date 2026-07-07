package br.uerj.erp.web.auth;

import br.uerj.erp.web.session.SessionContext;
import br.uerj.erp.web.session.SessionUser;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@WebServlet("/oauth/callback")
public class OAuthCallbackServlet extends HttpServlet {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String expected = (String) request.getSession().getAttribute("oauth_state");

        if (code == null || state == null || expected == null || !expected.equals(state)) {
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
            return;
        }

        try {
            request.getSession().removeAttribute("oauth_state");

            String accessToken = exchangeCodeForToken(code);
            JsonObject claims = decodeJwtPayload(accessToken);

            SessionUser sessionUser = new SessionUser();
            sessionUser.setUsername(readString(claims, "preferred_username", readString(claims, "username", readString(claims, "sub", null))));
            sessionUser.setFullName(readString(claims, "full_name", sessionUser.getUsername()));
            sessionUser.setAccessToken(accessToken);
            sessionUser.setRoles(readRoles(claims));

            CDI.current().select(SessionContext.class).get().setCurrentUser(sessionUser);
            if (sessionUser.getRoles().contains("SELLER") && !sessionUser.getRoles().contains("ADMIN") && !sessionUser.getRoles().contains("MANAGER")) {
                response.sendRedirect(request.getContextPath() + "/app/sales.xhtml");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/app/dashboard.xhtml");
        } catch (Exception exception) {
            exception.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/login.xhtml");
        }
    }

    private String exchangeCodeForToken(String code) throws Exception {
        String body = "grant_type=authorization_code"
                + "&code=" + url(code)
                + "&redirect_uri=" + url(OAuthSettings.redirectUri())
                + "&client_id=" + url(OAuthSettings.clientId())
                + "&client_secret=" + url(OAuthSettings.clientSecret());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OAuthSettings.authInternalBaseUrl() + "/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("token endpoint retornou " + response.statusCode() + ": " + response.body());
        }

        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            JsonObject json = reader.readObject();
            if (!json.containsKey("access_token")) {
                throw new IllegalStateException("resposta do token sem access_token: " + json);
            }
            return json.getString("access_token");
        }
    }

    private JsonObject decodeJwtPayload(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            throw new IllegalStateException("access token não é JWT válido");
        }

        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        String payload = new String(decoded, StandardCharsets.UTF_8);

        try (JsonReader reader = Json.createReader(new StringReader(payload))) {
            return reader.readObject();
        }
    }

    private List<String> readRoles(JsonObject claims) {
        List<String> roles = new ArrayList<>();
        JsonArray array = claims.getJsonArray("roles");
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                roles.add(array.getString(i));
            }
        }
        return roles;
    }

    private String readString(JsonObject json, String key, String fallback) {
        return json.containsKey(key) && !json.isNull(key) ? json.getString(key) : fallback;
    }

    private String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
