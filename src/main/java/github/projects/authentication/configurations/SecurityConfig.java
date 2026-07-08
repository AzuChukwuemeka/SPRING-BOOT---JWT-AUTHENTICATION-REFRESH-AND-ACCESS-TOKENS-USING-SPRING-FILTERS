package github.projects.authentication.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.projects.authentication.filters.JwtAuthenticationFilter;
import github.projects.authentication.filters.JwtUsernamePasswordAuthenticationFilter;
import github.projects.authentication.repositories.UserRepositoryI;
import github.projects.authentication.utils.CustomUserDetailsService;
import github.projects.authentication.utils.JwtServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // Publicly reachable documentation / dev-tooling endpoints. The H2 console is included
    // here for local/demo convenience - disable it (and the frameOptions relaxation below)
    // before pointing this app at a real production database.
    private static final String[] PUBLIC_ENDPOINTS = {
            "/login",
            "/api/v1/auth/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**",
            "/h2-console/**",
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity httpSecurity,
            PasswordEncoder passwordEncoder,
            CustomUserDetailsService userDetailsService
    ) {
        try {
            AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
            DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
            daoAuthenticationProvider.setUserDetailsService(userDetailsService);
            authenticationManagerBuilder.authenticationProvider(daoAuthenticationProvider);
            return authenticationManagerBuilder.build();
        } catch (Exception e) {
            LoggerFactory.getLogger(this.getClass()).warn("Couldn't Create authentication Object");
            throw new RuntimeException("Error Creating AuthenticationManager Object");
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            AuthenticationManager authenticationManager,
            JwtServiceImpl jwtService,
            JwtProperties jwtProperties,
            ObjectMapper objectMapper,
            UserRepositoryI userRepository
    ) {
        try {
            httpSecurity
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(PUBLIC_ENDPOINTS)
                            .permitAll()
                            .anyRequest()
                            .authenticated());
            httpSecurity.authenticationManager(authenticationManager);
            httpSecurity.cors(Customizer.withDefaults());
            httpSecurity.csrf(AbstractHttpConfigurer::disable);
            httpSecurity.sessionManagement(AbstractHttpConfigurer::disable);
            // The H2 console renders itself in a frame; relax frame-options only for it.
            httpSecurity.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
            httpSecurity.addFilterBefore(
                    new JwtAuthenticationFilter(
                            userRepository,
                            authenticationManager,
                            jwtService
                    ),
                    UsernamePasswordAuthenticationFilter.class
            );
            httpSecurity.addFilterBefore(
                    new JwtUsernamePasswordAuthenticationFilter(authenticationManager, jwtService, jwtProperties, objectMapper),
                    UsernamePasswordAuthenticationFilter.class
            );
            return httpSecurity.build();
        } catch (Exception e) {
            LoggerFactory.getLogger(this.getClass()).warn("Couldn't Create SecurityFilterChain Object");
            throw new RuntimeException("Error Creating SecurityFilterChain Object");
        }
    }
}
