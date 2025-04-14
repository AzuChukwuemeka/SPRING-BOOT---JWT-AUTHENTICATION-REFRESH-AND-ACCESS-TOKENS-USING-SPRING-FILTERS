package github.projects.authentication.controllers;

import github.projects.authentication.dataClasses.UserData;
import github.projects.authentication.services.UserServiceI;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/auth")
public class UserController {

    private final UserServiceI userServiceImpl;
    public UserController(UserServiceI userService) {
        this.userServiceImpl = userService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request){
        return userServiceImpl.getCookie(request.getCookies(), "refreshtoken");
    }
    @PostMapping("/register")
    public UserData createUser(@RequestBody UserData userData){
        userData.setRole("USER");
        return userServiceImpl.createUser(userData);
    };
    @GetMapping("/user/{id}")
    public UserData getUserById(@PathVariable UUID id){
        LoggerFactory.getLogger(this.getClass()).info("The UUID reached".concat(id.toString()));
        return userServiceImpl.getUserById(id);
    };
}
