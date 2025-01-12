package uozap.auth.services;

import uozap.auth.users.User;

/**
 * this class is responsible for user authentication and token generation.
 */
public class AuthService {
    private UserService userService;
    private TokenService tokenService;

    /**
     * shall use the required dependencies.
     *
     * @param userService service for user operations
     * @param tokenService service for token management
     */
    public AuthService(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * authenticate a user using username/email and password.
     *
     * @param usernameOrEmail the username or email of the user
     * @param password the user's password
     * @return authentication token if successful
     * @throws Exception if credentials are invalid
     */
    public String authenticate(String usernameOrEmail, String password) throws Exception {
        User user = userService.getUserByUsername(usernameOrEmail);
        if (user == null) {
            user = userService.getUserByEmail(usernameOrEmail);
        }

        if (user == null || !user.getHashedPassword().equals(HashService.hashPassword(password, user.getSalt()))) {
            throw new Exception("Invalid username/email or password."); // shall not be too specific on the error cause, for security measures
        }

        return tokenService.generateToken(user);
    }

    /**
     *
     * @return the token service object
     */
    public TokenService getTokenService() {
        return tokenService;
    }

    /**
     *
     * @return the user service object
     */
    public UserService getUserService() {
        return userService;
    }
}