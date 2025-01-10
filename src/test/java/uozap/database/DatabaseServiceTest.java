package uozap.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uozap.auth.services.HashService;
import uozap.auth.users.User;

public class DatabaseServiceTest {
    private static Connection connection;

    @BeforeAll
    public static void setup() throws SQLException {
        connection = DatabaseService.getConnection();
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS users (id VARCHAR(36) PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, email VARCHAR(100) NOT NULL UNIQUE, hashed_password VARCHAR(255) NOT NULL, salt BLOB NOT NULL)");
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS tokens (token VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36), FOREIGN KEY (user_id) REFERENCES users(id))");
    }

    @AfterAll
    public static void teardown() throws SQLException {
        connection.createStatement().execute("DROP TABLE tokens");
        connection.createStatement().execute("DROP TABLE users");
        connection.close();
    }

    @Test
    public void testAddAndGetUser() throws SQLException {
        UUID userId = UUID.randomUUID();
        byte[] salt = HashService.generateSalt();
        String hashedPassword = HashService.hashPassword("password", salt);
        User user = new User(userId, "testuser", "testuser@example.com", hashedPassword, salt);

        DatabaseService.addUser(user);
        User retrievedUser = DatabaseService.getUserByUsername("testuser");

        assertNotNull(retrievedUser);
        assertEquals(userId, retrievedUser.getId());
        assertEquals("testuser", retrievedUser.getUsername());
        assertEquals("testuser@example.com", retrievedUser.getEmail());
    }

    @Test
    public void testAddAndGetToken() throws SQLException {
        UUID userId = UUID.randomUUID();
        byte[] salt = HashService.generateSalt();
        String hashedPassword = HashService.hashPassword("password", salt);
        User user = new User(userId, "tokenuser", "tokenuser@example.com", hashedPassword, salt);

        DatabaseService.addUser(user);
        String token = UUID.randomUUID().toString();
        DatabaseService.addToken(token, userId);

        UUID retrievedUserId = DatabaseService.getUserIdByToken(token);
        assertNotNull(retrievedUserId);
        assertEquals(userId, retrievedUserId);
    }
}