package github.projects.authentication.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * The issuer claim embedded in and verified against every token.
     */
    private String issuer;

    /**
     * Secret key used to sign/verify tokens with HMAC256. Should be overridden
     * via the JWT_SECRET environment variable in any real deployment.
     */
    private String secret;

    private long accessTokenExpirationMinutes = 15;

    private long refreshTokenExpirationDays = 7;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public void setAccessTokenExpirationMinutes(long accessTokenExpirationMinutes) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    public long getRefreshTokenExpirationDays() {
        return refreshTokenExpirationDays;
    }

    public void setRefreshTokenExpirationDays(long refreshTokenExpirationDays) {
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }
}
