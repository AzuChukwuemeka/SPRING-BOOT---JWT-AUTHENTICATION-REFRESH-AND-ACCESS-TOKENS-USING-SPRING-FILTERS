package github.projects.authentication.configurations;

import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataBaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DataBaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @PostConstruct
    public void postgresInitializer(){
        if(databaseSchemaExists("tbl_user")){
            return;
        }
        LoggerFactory.getLogger(this.getClass()).info("Database Schema User Doesn't exist creating the schema");
        createDatabaseSchema();
    }
    private boolean databaseSchemaExists(String tablename) {
        try{
            String query = "SELECT to_regclass(?)";
            String result = jdbcTemplate.queryForObject(query,String.class, tablename);
            return result != null;
        }catch(DataAccessException e){
            LoggerFactory.getLogger(this.getClass()).warn("Couldn't determine if database tbl_user exists, skipping creation");
            return true;
        }
    }
    private void createDatabaseSchema() {
        String createTableSql = "CREATE EXTENSION IF NOT EXISTS 'pgcrypto'" + "CREATE TABLE tbl_user (" +
                "id uuid NOT NULL DEFAULT," +
                "username TEXT UNIQUE," +
                "password TEXT," +
                "role TEXT" +
                "CONSTRAINT tbl_user_pkey PRIMARY KEY (id)" + ")";
        jdbcTemplate.execute(createTableSql);
    }
}
