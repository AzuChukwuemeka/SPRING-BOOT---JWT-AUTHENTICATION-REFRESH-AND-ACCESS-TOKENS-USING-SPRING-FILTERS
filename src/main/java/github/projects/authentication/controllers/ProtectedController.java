package github.projects.authentication.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/")
@Tag(name = "Protected", description = "Example endpoint requiring a valid access token")
public class ProtectedController {

    @Operation(summary = "Sample route only reachable with a valid Bearer access token")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/protected")
    public String protectedRoute() {
        return "This route is protected";
    }
}
