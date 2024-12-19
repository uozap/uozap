package uozap.auth.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import uozap.auth.users.User;

/**
 * this class is used for the management of authentication tokens.
 * (saves in-memory tokens, should be replaced in the db)
 */
public class TokenService {
    private Map<String, User> tokenStore = new HashMap<>();

     /**
      * generates a new authentication token for a user.
      */
    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user);
        return token;
    }

    /**
     * validates a token and returns the associated user.
     */
    public User validateToken(String token) throws Exception {
        User user = tokenStore.get(token);
        if (user == null) {
            throw new Exception("Invalid token.");
        }
        return user;
    }
}
