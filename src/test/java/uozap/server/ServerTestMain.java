package uozap.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import uozap.auth.services.AuthService;
import uozap.auth.services.TokenService;
import uozap.auth.services.UserService;
import uozap.auth.users.User;

/**
 * Main class for testing the server functionality.
 * Initializes services, starts the server, and simulates client connections.
 */
public class ServerTestMain {

    public static final String RESET = "\033[0m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String YELLOW = "\033[0;33m";
    public static final String CYAN = "\033[0;36m";

    /**
     * method to start the server and simulate client connections.
     */
    public static void main(String[] args) throws Exception {

        System.out.println(CYAN + "Server starting..." + RESET);

        System.out.println(YELLOW + "Creating services:" + RESET);

        System.out.print("- token service: ");
        TokenService ts = new TokenService();
        System.out.println(GREEN + "DONE" + RESET);

        System.out.print("- user service: ");
        UserService us = new UserService();
        System.out.println(GREEN + "DONE" + RESET);

        System.out.print("- auth service: ");
        AuthService as = new AuthService(us, ts);
        System.out.println(GREEN + "DONE" + RESET);

        System.out.print("- socket manager: ");
        SocketManager sm = new SocketManager(as);
        System.out.println(GREEN + "DONE" + RESET);

        // start the server in a separate thread
        new Thread(() -> {
            try {
                sm.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // wait for the server to be fully initialized
        Thread.sleep(2000);

        System.out.println(GREEN + "Server is up and running." + RESET);

        // register users
        us.registerUser("username1", "email1@example.com", "password1");
        us.registerUser("username2", "email2@example.com", "password2");

        // authenticate users
        User user1 = us.getUserByUsername("username1");
        User user2 = us.getUserByUsername("username2");
        String token1 = as.authenticate("username1", "password1");
        String token2 = as.authenticate("username2", "password2");

        // simulate multiple clients connecting to the chat
        Thread client1 = new Thread(() -> simulateClientConnection("TestChat", token1, user1));
        Thread client2 = new Thread(() -> simulateClientConnection("TestChat", token2, user2));

        client1.start();
        client2.start();

        client1.join();
        client2.join();
    }

    /**
     * simulates a client connection to the chat server.
     *
     * @param chatName the name of the chat room
     * @param token the authentication token for the user
     */
    private static void simulateClientConnection(String chatName, String token, User user) {
        try (Socket socket = new Socket("localhost", 7878);
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
             ObjectInputStream oin = new ObjectInputStream(socket.getInputStream())) {

            // send chat name and token to the server
            dout.writeUTF(chatName);
            dout.writeUTF(token);

            // simulate sending a message
            dout.writeUTF("Hello, World!, This is: " + user.getUsername());

            while (true) {
                // read messages from the server
                Object message = oin.readObject();
                System.out.println("Message: " + message + " recived by user " + user.getUsername());
                
            }

            // note: In a real client, you would have a separate thread to read messages from the server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}