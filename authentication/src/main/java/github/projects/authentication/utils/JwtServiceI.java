package github.projects.authentication.utils;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Map;

public interface JwtServiceI {
    String generateToken(String username);
    String generateToken(Map<String,Object> extraClaims, String username);
    DecodedJWT isTokenValid(String token);
}
