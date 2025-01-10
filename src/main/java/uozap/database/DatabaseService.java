package uozap.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import uozap.auth.users.User;

public class DatabaseService {
    private static Connection connection;

    static {
        try {
            Properties properties = new Properties();
            properties.load(DatabaseService.class.getClassLoader().getResourceAsStream("database.properties"));
            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            connection = DriverManager.getConnection(url, username, password);
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() throws SQLException {
        try (PreparedStatement stmt1 = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS users (id VARCHAR(36) PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, email VARCHAR(100) NOT NULL UNIQUE, hashed_password VARCHAR(255) NOT NULL, salt BLOB NOT NULL)");
             PreparedStatement stmt2 = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS tokens (token VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36), FOREIGN KEY (user_id) REFERENCES users(id))")) {
            stmt1.execute();
            stmt2.execute();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties properties = new Properties();
            try {
                properties.load(DatabaseService.class.getClassLoader().getResourceAsStream("database.properties"));
                String url = properties.getProperty("db.url");
                String username = properties.getProperty("db.username");
                String password = properties.getProperty("db.password");
                connection = DriverManager.getConnection(url, username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void addUser(User user) throws SQLException {
        String query = "INSERT INTO users (id, username, email, hashed_password, salt) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, user.getId().toString());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getHashedPassword());
            stmt.setBytes(5, user.getSalt());
            stmt.executeUpdate();
        }
    }

    public static User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("hashed_password"),
                        rs.getBytes("salt")
                    );
                }
            }
        }
        return null;
    }

    public static User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("hashed_password"),
                        rs.getBytes("salt")
                    );
                }
            }
        }
        return null;
    }

    public static User getUserById(UUID id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("hashed_password"),
                        rs.getBytes("salt")
                    );
                }
            }
        }
        return null;
    }

    public static void addToken(String token, UUID userId) throws SQLException {
        String query = "INSERT INTO tokens (token, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, token);
            stmt.setString(2, userId.toString());
            stmt.executeUpdate();
        }
    }

    public static UUID getUserIdByToken(String token) throws SQLException {
        String query = "SELECT user_id FROM tokens WHERE token = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("user_id"));
                }
            }
        }
        return null;
    }
}