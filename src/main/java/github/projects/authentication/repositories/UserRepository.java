package github.projects.authentication.repositories;

import github.projects.authentication.dataClasses.UserData;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class UserRepository implements UserRepositoryI {

    private static final String USER_CREATION_SQL =
            "INSERT INTO tbl_user (id, username, password, role) VALUES (?, ?, ?, ?)";
    private static final String GET_USER_BY_ID_SQL =
            "SELECT id, username, password, role FROM tbl_user WHERE id = ?";
    private static final String GET_USER_BY_USERNAME_SQL =
            "SELECT id, username, password, role FROM tbl_user WHERE username = ?";

    private static final RowMapper<UserData> USER_ROW_MAPPER = (rs, rowNum) -> new UserData(
            rs.getObject("id", UUID.class),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserData createUser(UserData userData) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(USER_CREATION_SQL, id, userData.getUsername(), userData.getPassword(), userData.getRole());
        return new UserData(id, userData.getUsername(), userData.getPassword(), userData.getRole());
    }

    @Override
    public UserData getUserById(UUID id) {
        try {
            return jdbcTemplate.queryForObject(GET_USER_BY_ID_SQL, USER_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    @Override
    public UserData getUserByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject(GET_USER_BY_USERNAME_SQL, USER_ROW_MAPPER, username);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }
}
