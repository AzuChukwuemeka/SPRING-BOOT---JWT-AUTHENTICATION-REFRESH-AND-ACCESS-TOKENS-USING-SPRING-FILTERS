package github.projects.authentication.utils;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface JwtServiceI {

    /**
     * Creates a short-lived access token for the given username.
     */
    String generateAccessToken(String username);

    /**
     * Creates a longer-lived refresh token for the given username.
     */
    String generateRefreshToken(String username);

    /**
     * Verifies the token's signature, issuer and expiry.
     *
     * @throws com.auth0.jwt.exceptions.JWTVerificationException if the token is invalid or expired
     */
    DecodedJWT verifyToken(String token);
}
