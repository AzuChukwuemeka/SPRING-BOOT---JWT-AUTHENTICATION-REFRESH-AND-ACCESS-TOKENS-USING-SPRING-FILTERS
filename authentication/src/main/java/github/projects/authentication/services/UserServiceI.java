package github.projects.authentication.services;

import github.projects.authentication.dataClasses.UserData;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface UserServiceI {
    UserData createUser(UserData userData);
    UserData getUserById(UUID id);

    ResponseEntity<?> getCookie(Cookie[] cookies, String name);
}
