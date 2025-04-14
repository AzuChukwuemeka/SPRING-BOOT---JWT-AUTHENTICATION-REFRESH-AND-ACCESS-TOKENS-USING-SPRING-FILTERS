package github.projects.authentication.repositories;

import github.projects.authentication.dataClasses.UserData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.UUID;

@Repository
public class UserRepository implements  UserRepositoryI{
    String USER_CREATION_SQL = "INSERT INTO tbl_user(username, password, role)" +
            "VALUES (?, ?, ?);";
    String GET_USER_BY_ID_SQL = "select * from tbl_user where id = ?";
    String GET_USER_BY_ID_USERNAME = "select * from tbl_user where username = ?";
    private final JdbcTemplate jdbcTemplate;
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public UserData createUser(UserData userData) {
        KeyHolder keyholder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(USER_CREATION_SQL,PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, userData.getUsername());
                    ps.setString(2,userData.getPassword());
                    ps.setString(3,userData.getRole());
                    return ps;
        },keyholder);
        Map<String, Object> keys = keyholder.getKeys();
        UserData user = new UserData(
                UUID.fromString(String.valueOf(keys.get("id"))),
                (String) keys.get("username"),
                (String) keys.get("password"),
                (String) keys.get("role")
        );
        return user;
    };

    @Override
    public UserData getUserById(UUID id) {
        return jdbcTemplate.queryForObject(
                GET_USER_BY_ID_SQL,
                new Object[]{id},
                (rs, rowNum) -> new UserData(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                )
        );
    }

    @Override
    public UserData getUserByUsername(String username) {
        return jdbcTemplate.queryForObject(
                GET_USER_BY_ID_USERNAME,
                new Object[]{username},
                (rs, rowNum) -> new UserData(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                )
        );
    }
}
