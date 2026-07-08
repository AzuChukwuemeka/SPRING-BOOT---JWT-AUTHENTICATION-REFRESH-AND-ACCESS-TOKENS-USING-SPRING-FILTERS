package github.projects.authentication.services;

import github.projects.authentication.configurations.JwtProperties;
import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.exceptions.UserNotFoundException;
import github.projects.authentication.repositories.UserRepositoryI;
import github.projects.authentication.utils.JwtServiceImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepositoryI userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;
    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("test-issuer");
        properties.setSecret("unit-test-secret-key-please-ignore");
        properties.setAccessTokenExpirationMinutes(15);
        properties.setRefreshTokenExpirationDays(7);
        jwtService = new JwtServiceImpl(properties);
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void createUserEncodesPasswordBeforePersisting() {
        UserData input = new UserData(null, "alice", "plaintext", "USER");
        when(passwordEncoder.encode("plaintext")).thenReturn("hashed-password");
        when(userRepository.createUser(any(UserData.class))).thenAnswer(inv -> inv.getArgument(0));

        UserData result = userService.createUser(input);

        assertThat(result.getPassword()).isEqualTo("hashed-password");
        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).createUser(input);
    }

    @Test
    void getUserByIdReturnsUserWhenFound() {
        UUID id = UUID.randomUUID();
        UserData stored = new UserData(id, "alice", "hashed", "USER");
        when(userRepository.getUserById(id)).thenReturn(stored);

        UserData result = userService.getUserById(id);

        assertThat(result).isEqualTo(stored);
    }

    @Test
    void getUserByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.getUserById(id)).thenReturn(null);

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getCookieReturnsBadRequestWhenNoCookiesPresent() {
        ResponseEntity<?> response = userService.getCookie(null, "refreshToken");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getCookieReturnsBadRequestWhenRefreshCookieMissing() {
        Cookie[] cookies = {new Cookie("otherCookie", "value")};

        ResponseEntity<?> response = userService.getCookie(cookies, "refreshToken");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getCookieReturnsNewAccessTokenForValidRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken("alice");
        Cookie[] cookies = {new Cookie("refreshToken", refreshToken)};

        ResponseEntity<?> response = userService.getCookie(cookies, "refreshToken");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().toString()).contains("New Access Token-");
    }

    @Test
    void getCookieRejectsInvalidRefreshToken() {
        Cookie[] cookies = {new Cookie("refreshToken", "not-a-real-jwt")};

        ResponseEntity<?> response = userService.getCookie(cookies, "refreshToken");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
