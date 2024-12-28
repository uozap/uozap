package uozap.auth.users;

import java.util.UUID;

/**
 * this class represents a user in the system.
 */
public class User {
    private UUID id;
    private String username;
    private String email;
    private String hashedPassword;
    private byte[] salt;
    // private Role role;

    public User(UUID id, String username, String email, String hashedPassword, byte[] salt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
        // this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    
    public byte[] getSalt() {
        return salt;
    }
    
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    // public Role getRole(){
    //    return role;
    //}

    //public void setRole(Role role){
    //    this.role = role;
    //}
}
