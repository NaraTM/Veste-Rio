package br.uerj.erp.web.auth;

public final class OAuthSettings {

    private OAuthSettings() {
    }

    public static String authPublicBaseUrl() {
        return read("AUTH_PUBLIC_BASE_URL", "http://localhost:9000");
    }

    public static String authInternalBaseUrl() {
        return read("AUTH_INTERNAL_BASE_URL", "http://auth-server:9000");
    }

    public static String appPublicBaseUrl() {
        return read("APP_PUBLIC_BASE_URL", "http://localhost:8080/erp-web-war");
    }

    public static String clientId() {
        return read("OAUTH_CLIENT_ID", "erp-web-client");
    }

    public static String clientSecret() {
        return read("OAUTH_CLIENT_SECRET", "erp-web-secret");
    }

    public static String redirectUri() {
        return appPublicBaseUrl() + "/oauth/callback";
    }

    private static String read(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
