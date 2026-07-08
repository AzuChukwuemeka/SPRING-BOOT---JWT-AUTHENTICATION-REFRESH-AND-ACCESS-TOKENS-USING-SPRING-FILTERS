package github.projects.authentication.filters;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import github.projects.authentication.configurations.JwtProperties;
import github.projects.authentication.dataClasses.LoginResponse;
import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.utils.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtService;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public JwtUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtServiceImpl jwtService,
            JwtProperties jwtProperties,
            ObjectMapper objectMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
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
            String accessToken = jwtService.generateAccessToken(authResult.getName());
            String refreshToken = jwtService.generateRefreshToken(authResult.getName());

            Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);

            // Kept for clients/tools that prefer reading the token straight off the header...
            response.setHeader("Authorization", "Bearer ".concat(accessToken));

            // ...but the JSON body is what makes this demoable: the access token is right there.
            long expiresInSeconds = Duration.ofMinutes(jwtProperties.getAccessTokenExpirationMinutes()).getSeconds();
            LoginResponse body = new LoginResponse(accessToken, "Bearer", expiresInSeconds);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), body);
        } catch (JWTCreationException ex) {
            unsuccessfulAuthentication(request, response, new AuthenticationException("Bad Credentials") {
            });
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("error", failed.getMessage()));
    }
}
