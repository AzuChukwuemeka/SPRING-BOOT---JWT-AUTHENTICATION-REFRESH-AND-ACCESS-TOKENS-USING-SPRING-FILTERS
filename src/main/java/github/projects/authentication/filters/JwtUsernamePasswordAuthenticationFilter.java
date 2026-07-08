package github.projects.authentication.filters;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.utils.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtService;
    private final ObjectMapper objectMapper;

    public JwtUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, JwtServiceImpl jwtService, ObjectMapper objectMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (request.getMethod().equals("POST")) {
            try {
                UserData userData = objectMapper.readValue(request.getInputStream(), UserData.class);
                UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(userData.getUsername(), userData.getPassword());
                return authenticationManager.authenticate(authResult);
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).warn("Error converting request input stream to json");
            }
        }
        return null;
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain, Authentication
                    authResult
    ) throws IOException, ServletException {
        try {
            String token = jwtService.generateAccessToken(authResult.getName());
            String refresh = jwtService.generateRefreshToken(authResult.getName());
            Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refresh);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            response.setHeader("Authorization", "Bearer ".concat(token));
            response.addCookie(refreshTokenCookie);
            response.getWriter().write("Login Successful");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JWTCreationException ex) {
            unsuccessfulAuthentication(request, response, new AuthenticationException("Bad Credentials") {
            });
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(failed.getMessage());
    }
}
