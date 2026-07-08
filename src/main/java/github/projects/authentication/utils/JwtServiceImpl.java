package github.projects.authentication.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import github.projects.authentication.configurations.JwtProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtServiceImpl implements JwtServiceI {

    private final JwtProperties jwtProperties;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generateAccessToken(String username) {
        return buildToken(username, jwtProperties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
    }

    @Override
    public String generateRefreshToken(String username) {
        return buildToken(username, jwtProperties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);
    }

    private String buildToken(String username, long amountToAdd, ChronoUnit unit) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(jwtProperties.getIssuer())
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(Date.from(now.plus(amountToAdd, unit)))
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
    }

    @Override
    public DecodedJWT verifyToken(String token) {
        // Throws an unchecked JWTVerificationException on any failure (bad signature,
        // wrong issuer, or expiry) - callers should catch that, not assume success.
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                .withIssuer(jwtProperties.getIssuer())
                .build();
        return verifier.verify(token);
    }
}
