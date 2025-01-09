package uozap.auth.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import uozap.auth.users.User;

/**
 * this class is used for handling basic user management operations including registration and retrieval.
 */
public class UserService {
    /**
     * NOTE: for the sake of simplicity, this class does not interact 
     * with a database and stores all users in memory, this should be changes. 
     */
    private Map<String, User> usersByUsername = new HashMap<>();
    /**
     * NOTE: for the sake of simplicity, this class does not interact 
     * with a database and stores all users in memory, this should be changes. 
     */
    private Map<String, User> usersByEmail = new HashMap<>();

    /**
     * registers a new user in the system.
     *
     * @param username the unique username for the new user
     * @param email the email address for the new user
     * @param password the user's password (will be hashed)
     * @throws Exception if a user with the given username or email already exists
     */
    public void registerUser(String username, String email, String password) throws Exception {
        if (usersByUsername.containsKey(username) || usersByEmail.containsKey(email)) {
            throw new Exception("User already exists.");
        }
        byte[] salt = HashService.generateSalt();
        String hashedPassword = HashService.hashPassword(password, salt);
        UUID id = generateUserId();
        //User user = new User(id, username, email, hashedPassword, salt, Role.USER);
        User user = new User(id, username, email, hashedPassword, salt);

        // shall be replaced with db operations
        usersByUsername.put(username, user);
        usersByEmail.put(email, user);

    }

    /**
     * generates a unique user id.
     */
    private UUID generateUserId() {
        UUID uniqueKey = UUID.randomUUID();
        return uniqueKey;
    }

    /**
     * simply retrieves a user by their username.
     */
    public User getUserByUsername(String username) {
        return usersByUsername.get(username);
    }

    /**
     * simply retrieves a user by their email address.
     */
    public User getUserByEmail(String email) {
        return usersByEmail.get(email);
    }


}
