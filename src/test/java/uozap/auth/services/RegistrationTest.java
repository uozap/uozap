package uozap.auth.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uozap.auth.users.User;

/**
 * test suite for validating the registration process
 * and associated functionality in AuthService.
 */
public class RegistrationTest {
    private AuthService authService;
    private UserService userService;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        tokenService = new TokenService();
        authService = new AuthService(userService, tokenService);
    }

    /**
     * ensures that the user registration flow executes correctly
     * and the created user can be retrieved by username and email.
     */
    @Test
    void full_registration_correct() throws Exception {
        userService.registerUser("username", "example@example.com", "Password123");

        /*
         * first things first check that the user is the same no matter of how you get it
         */
        User userByUsername = userService.getUserByUsername("username");
        User userByEmail = userService.getUserByEmail("example@example.com");
        assertNotNull(userByEmail, "User by email should not be null");
        assertNotNull(userByUsername, "User by username should not be null");
        assertEquals(userByEmail, userByUsername, "Users retrieved by username and email should match");

        /*
         * then check that the hashed password is the same as well (you never know)
         */
        assertEquals(userByEmail.getHashedPassword(), userByUsername.getHashedPassword(),
                "Hashed passwords should be identical");
        assertNotEquals("Password123", userByUsername.getHashedPassword(),
                "Hashed password should not match raw password");

        /*
         * and finally check the tokens are authenticated and validated correctly
         */
        String token = authService.authenticate("username", "Password123");
        assertNotNull(token, "Authentication token should not be null");
        User userByToken = tokenService.validateToken(token);
        assertEquals(userByToken, userByUsername, "User by token should match the original user");
    }

    /**
     * ensures that the user registration flow throws the right exceptions when needed (the message might be used as is in the gui)
     */
    @Test
    void duplicate_registration_incorrect() throws Exception {
        /*
         * try registering the same user twice (email-email and uname-uname)
         */
        userService.registerUser("username", "example@example.com", "password");

        try {
            authService.authenticate("different", "password");
            fail("Expected \"Invalid username/email or password.\" to be thrown");
        } catch (Exception e) {
            assertEquals("Invalid username/email or password.", e.getMessage());
        }

        try {
            authService.authenticate("different@example.com", "password");
            fail("Expected \"Invalid username/email or password.\" to be thrown");
        } catch (Exception e) {
            assertEquals("Invalid username/email or password.", e.getMessage());
        }
    }
}
