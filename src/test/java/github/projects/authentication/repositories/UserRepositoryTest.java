package github.projects.authentication.repositories;

import github.projects.authentication.dataClasses.UserData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises UserRepository against a real (embedded, in-memory) H2 database,
 * with schema.sql applied automatically by Spring Boot's test slice.
 */
@JdbcTest
@Import(UserRepository.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createUserPersistsAndAssignsAnId() {
        UserData toCreate = new UserData(null, "repo-user-" + UUID.randomUUID(), "hashed-pw", "USER");

        UserData created = userRepository.createUser(toCreate);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo(toCreate.getUsername());
    }

    @Test
    void getUserByIdReturnsThePersistedUser() {
        UserData toCreate = new UserData(null, "repo-user-" + UUID.randomUUID(), "hashed-pw", "USER");
        UserData created = userRepository.createUser(toCreate);

        UserData found = userRepository.getUserById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo(created.getUsername());
        assertThat(found.getRole()).isEqualTo("USER");
    }

    @Test
    void getUserByIdReturnsNullWhenMissing() {
        UserData found = userRepository.getUserById(UUID.randomUUID());

        assertThat(found).isNull();
    }

    @Test
    void getUserByUsernameReturnsNullWhenMissing() {
        UserData found = userRepository.getUserByUsername("does-not-exist-" + UUID.randomUUID());

        assertThat(found).isNull();
    }

    @Test
    void getUserByUsernameFindsTheRightUser() {
        String username = "repo-user-" + UUID.randomUUID();
        userRepository.createUser(new UserData(null, username, "hashed-pw", "ADMIN"));

        UserData found = userRepository.getUserByUsername(username);

        assertThat(found).isNotNull();
        assertThat(found.getRole()).isEqualTo("ADMIN");
    }
}
