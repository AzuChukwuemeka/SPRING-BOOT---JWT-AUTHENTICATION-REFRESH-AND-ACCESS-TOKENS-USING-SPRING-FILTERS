package github.projects.authentication.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.repositories.UserRepositoryI;
import github.projects.authentication.utils.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepositoryI userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtService;

    public JwtAuthenticationFilter(UserRepositoryI userRepository, AuthenticationManager authenticationManager, JwtServiceImpl jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            DecodedJWT decodedJWT = jwtService.verifyToken(token.substring(7));
            UserData userByUsername = userRepository.getUserByUsername(decodedJWT.getSubject());
            if (userByUsername == null) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    decodedJWT.getSubject(), null, Collections.singleton(new SimpleGrantedAuthority(userByUsername.getRole())));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (JWTVerificationException ex) {
            LoggerFactory.getLogger(this.getClass()).debug("Rejected invalid or expired JWT: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
}
