package github.projects.authentication.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class JwtServiceImpl implements JwtServiceI{
    private final Dotenv dotenv;
    public JwtServiceImpl(Dotenv dotenv) {
        this.dotenv = dotenv;
    }
    @Override
    public String generateToken(String username) {
        return JWT.create()
                .withIssuer(dotenv.get("JWT_ISSUER"))
                .withSubject(username)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .sign(Algorithm.HMAC256(dotenv.get("MY_SECRET_KEY")));
    }
    public String generateToken(String username, ChronoUnit chronoUnit, int add) {
        return JWT.create()
                .withIssuer(dotenv.get("JWT_ISSUER"))
                .withSubject(username)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Date.from(Instant.now().plus(add, chronoUnit)))
                .sign(Algorithm.HMAC256(dotenv.get("MY_SECRET_KEY")));
    }
    @Override
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return JWT.create()
                .withIssuer(username)
                .withSubject(username)
                .withClaim(dotenv.get("JWT_ISSUER"),extraClaims)
                .withIssuedAt(Instant.now())
                .sign(Algorithm.HMAC256(dotenv.get("MY_SECRET_KEY")));
    }
    @Override
    public DecodedJWT isTokenValid(String token) {
        //Throws UnChecked Exception
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(dotenv.get("MY_SECRET_KEY")))
                .withIssuer(dotenv.get("JWT_ISSUER"))
                .build();
        return verifier.verify(token);
    }
}
