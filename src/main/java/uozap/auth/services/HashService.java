package uozap.auth.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * this class handles password hashing and salt generation.
 * (it uses PBKDF2 with HMAC-SHA1 for secure password hashing)
 */
public class HashService {

    /**
     * generate a random salt for password hashing.
     */
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * hash a password using PBKDF2 with the provided salt.
     *
     * @param password the password to hash
     * @param salt the salt to use in hashing
     * @return Base64 encoded string of the hashed password
     * @throws RuntimeException if hashing algorithm is not available
     * @throws IllegalStateException if the key specification is invalid
     */
    public static String hashPassword(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Invalid SecretKeyFactory", ex);
        }
    }
}