package uozap.auth.services;

import java.sql.SQLException;
import java.util.UUID;
import uozap.auth.users.User;
import uozap.database.DatabaseService;

/**
 * this class is used for handling basic user management operations including registration and retrieval.
 */
public class UserService {

    /**
     * registers a new user in the system.
     *
     * @param username the unique username for the new user
     * @param email the email address for the new user
     * @param password the user's password (will be hashed)
     * @throws Exception if a user with the given username or email already exists
     */
    public void registerUser(String username, String email, String password) throws Exception {
        if (getUserByUsername(username) != null || getUserByEmail(email) != null) {
            throw new Exception("User already exists.");
        }
        byte[] salt = HashService.generateSalt();
        String hashedPassword = HashService.hashPassword(password, salt);
        UUID id = generateUserId();
        User user = new User(id, username, email, hashedPassword, salt);

        DatabaseService.addUser(user);
    }

    /**
     * generates a unique user id.
     */
    private UUID generateUserId() {
        return UUID.randomUUID();
    }

    /**
     * simply retrieves a user by their username.
     */
    public User getUserByUsername(String username) {
        try {
            return DatabaseService.getUserByUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * simply retrieves a user by their email address.
     */
    public User getUserByEmail(String email) {
        try {
            return DatabaseService.getUserByEmail(email);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}