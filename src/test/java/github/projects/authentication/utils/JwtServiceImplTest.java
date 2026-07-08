package github.projects.authentication.utils;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import github.projects.authentication.configurations.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("test-issuer");
        properties.setSecret("unit-test-secret-key-please-ignore");
        properties.setAccessTokenExpirationMinutes(15);
        properties.setRefreshTokenExpirationDays(7);
        jwtService = new JwtServiceImpl(properties);
    }

    @Test
    void generatedAccessTokenIsValidAndCarriesTheUsername() {
        String token = jwtService.generateAccessToken("alice");

        DecodedJWT decoded = jwtService.verifyToken(token);

        assertThat(decoded.getSubject()).isEqualTo("alice");
        assertThat(decoded.getIssuer()).isEqualTo("test-issuer");
    }

    @Test
    void generatedRefreshTokenIsValidAndCarriesTheUsername() {
        String token = jwtService.generateRefreshToken("bob");

        DecodedJWT decoded = jwtService.verifyToken(token);

        assertThat(decoded.getSubject()).isEqualTo("bob");
    }

    @Test
    void refreshTokenExpiresFurtherInTheFutureThanAccessToken() {
        String access = jwtService.generateAccessToken("carol");
        String refresh = jwtService.generateRefreshToken("carol");

        DecodedJWT decodedAccess = jwtService.verifyToken(access);
        DecodedJWT decodedRefresh = jwtService.verifyToken(refresh);

        assertThat(decodedRefresh.getExpiresAt()).isAfter(decodedAccess.getExpiresAt());
    }

    @Test
    void tokenSignedWithADifferentSecretIsRejected() {
        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setIssuer("test-issuer");
        otherProperties.setSecret("a-completely-different-secret-value");
        otherProperties.setAccessTokenExpirationMinutes(15);
        JwtServiceImpl otherService = new JwtServiceImpl(otherProperties);

        String token = otherService.generateAccessToken("mallory");

        assertThatThrownBy(() -> jwtService.verifyToken(token))
                .isInstanceOf(JWTVerificationException.class);
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateAccessToken("dave");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> jwtService.verifyToken(tampered))
                .isInstanceOf(JWTVerificationException.class);
    }
}
