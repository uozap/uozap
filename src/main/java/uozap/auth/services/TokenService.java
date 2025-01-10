package uozap.auth.services;

import java.sql.SQLException;
import java.util.UUID;

import uozap.auth.users.User;
import uozap.database.DatabaseService;

/**
 * this class is used for the management of authentication tokens.
 */
public class TokenService {

    /**
     * generates a new authentication token for a user.
     */
    public String generateToken(User user) throws SQLException {
        String token = UUID.randomUUID().toString();
        DatabaseService.addToken(token, user.getId());
        return token;
    }

    /**
     * validates a token and returns the associated user.
     */
    public User validateToken(String token) throws SQLException {
        UUID userId = DatabaseService.getUserIdByToken(token);
        if (userId != null) {
            return DatabaseService.getUserById(userId);
        }
        throw new SQLException("Invalid token.");
    }
}