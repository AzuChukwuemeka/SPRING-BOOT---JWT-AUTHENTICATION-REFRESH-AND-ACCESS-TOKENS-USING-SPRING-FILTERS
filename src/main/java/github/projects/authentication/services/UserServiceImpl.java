package github.projects.authentication.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.exceptions.UserNotFoundException;
import github.projects.authentication.repositories.UserRepositoryI;
import github.projects.authentication.utils.JwtServiceImpl;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserServiceI {
    private final UserRepositoryI userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtService;

    public UserServiceImpl(UserRepositoryI userRepository, PasswordEncoder passwordEncoder, JwtServiceImpl jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserData createUser(UserData userData) {
        String password = userData.getPassword();
        userData.setPassword(passwordEncoder.encode(password));
        return userRepository.createUser(userData);
    }

    @Override
    public UserData getUserById(UUID id) {
        UserData userData = userRepository.getUserById(id);
        if (userData == null) {
            throw new UserNotFoundException(id);
        }
        return userData;
    }

    @Override
    public ResponseEntity<?> getCookie(Cookie[] cookies, String name) {
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token not found");
        }
        for (Cookie cookie : cookies) {
            if (name.equalsIgnoreCase(cookie.getName())) {
                try {
                    String value = cookie.getValue();
                    jwtService.verifyToken(value);
                    DecodedJWT decode = JWT.decode(value);
                    String newToken = jwtService.generateAccessToken(decode.getSubject());
                    return ResponseEntity.ok("New Access Token-".concat(newToken));
                } catch (JWTVerificationException ex) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is invalid or expired");
                }
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token not found");
    }
}
