package github.projects.authentication.controllers;

import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.filters.JwtUsernamePasswordAuthenticationFilter;
import github.projects.authentication.services.UserServiceI;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "Authentication", description = "Registration, login (via /login), token refresh, and user lookup")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserServiceI userServiceImpl;

    public UserController(UserServiceI userService) {
        this.userServiceImpl = userService;
    }

    @Operation(summary = "Exchange a valid refresh token cookie for a new access token")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        return userServiceImpl.getCookie(request.getCookies(), JwtUsernamePasswordAuthenticationFilter.REFRESH_TOKEN_COOKIE_NAME);
    }

    @Operation(summary = "Register a new user with the USER role")
    @PostMapping("/register")
    public UserData createUser(@RequestBody UserData userData) {
        userData.setRole("USER");
        return userServiceImpl.createUser(userData);
    }

    @Operation(summary = "Fetch a user by id")
    @GetMapping("/user/{id}")
    public UserData getUserById(@PathVariable UUID id) {
        log.info("Fetching user with id {}", id);
        return userServiceImpl.getUserById(id);
    }
}
