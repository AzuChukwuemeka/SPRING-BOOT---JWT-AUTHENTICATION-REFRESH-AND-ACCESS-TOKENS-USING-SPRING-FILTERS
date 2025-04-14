package github.projects.authentication.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
public class ProtectedController {
    @GetMapping("/protected")
    public String protectedRoute(){
        return "This route is protected";
    }
}
