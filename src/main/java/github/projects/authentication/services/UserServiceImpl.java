package github.projects.authentication.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.repositories.UserRepositoryI;
import github.projects.authentication.utils.JwtServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserServiceI{
    private final UserRepositoryI userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Dotenv dotenv;
    private final JwtServiceImpl jwtService;
    public UserServiceImpl(UserRepositoryI userRepository, PasswordEncoder passwordEncoder, Dotenv dotenv, JwtServiceImpl jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dotenv = dotenv;
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
        return userRepository.getUserById(id);
    }

    @Override
    public ResponseEntity<?> getCookie(Cookie[] cookies, String name) {
        try{
            for(Cookie cookie : cookies){
                if(name.equalsIgnoreCase(cookie.getName())){
                    String value = cookie.getValue();
                    jwtService.isTokenValid(value);
                    DecodedJWT decode = JWT.decode(value);
                    String newToken = jwtService.generateToken(decode.getSubject());
                    return ResponseEntity.ok("New Access Token-".concat(newToken));
                }
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token not found");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh token not found / or is invalid");
    }
}
