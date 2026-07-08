package github.projects.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.projects.authentication.dataClasses.UserData;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boots the full Spring context (real security filter chain, real H2 database)
 * and drives the authentication flow the way a client actually would.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String uniqueUsername() {
        return "flow-user-" + UUID.randomUUID();
    }

    @Test
    void protectedRouteRejectsRequestsWithoutAToken() throws Exception {
        // No httpBasic()/formLogin() is configured, so Spring Security's default
        // entry point for an unauthenticated request is a plain 403, not 401.
        mockMvc.perform(get("/api/v1/protected"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registeredUserCanLogInAndAccessTheProtectedRoute() throws Exception {
        String username = uniqueUsername();
        UserData registration = new UserData(null, username, "correct-horse-battery-staple", null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andReturn();

        String authHeader = loginResult.getResponse().getHeader("Authorization");
        assertThat(authHeader).startsWith("Bearer ");

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();

        mockMvc.perform(get("/api/v1/protected").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string("This route is protected"));
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        String username = uniqueUsername();
        UserData registration = new UserData(null, username, "correct-horse-battery-staple", null);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)));

        UserData badLogin = new UserData(null, username, "totally-wrong-password", null);
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshEndpointIssuesANewAccessTokenForAValidRefreshCookie() throws Exception {
        String username = uniqueUsername();
        UserData registration = new UserData(null, username, "correct-horse-battery-staple", null);
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)));

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("New Access Token-")));
    }

    @Test
    void refreshEndpointRejectsRequestsWithNoCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateUsernameRegistrationIsRejected() throws Exception {
        String username = uniqueUsername();
        UserData registration = new UserData(null, username, "correct-horse-battery-staple", null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isConflict());
    }

    @Test
    void gettingAnUnknownUserIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/auth/user/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void swaggerUiAndApiDocsAreReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
