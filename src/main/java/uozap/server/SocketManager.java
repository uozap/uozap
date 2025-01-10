package uozap.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import uozap.auth.services.AuthService;

/**
 * Manages socket connections and chat rooms for the messaging server.
 * Handles client authentication and connection routing to appropriate chats.
 */
public class SocketManager extends Thread {
    private final ConcurrentHashMap<String, DataOutputStream> activeClients;
    private final AuthService authService;

    public SocketManager(AuthService authService) {
        this.activeClients = new ConcurrentHashMap<>();
        this.authService = authService;
    }

    @Override
    public void run() {
        System.out.println("Server started on port 7878...");

        try (ServerSocket serverSocket = new ServerSocket(7878)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Handle the client in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (DataInputStream din = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream())) {

            String username = null;
            boolean authenticated = false;

            while (true) {
                String command = din.readUTF();
                System.out.println("Received command: " + command);

                if (command.startsWith("/register")) {

                    // /register-{username}-{email}-{password}
                    String[] parts = command.split("-");
                    if (parts.length == 4) {
                        username = parts[1];
                        String email = parts[2];
                        String password = parts[3];
                        authService.getUserService().registerUser(username, email, password);
                        dout.writeUTF("User registered successfully");
                    } else {
                        dout.writeUTF("Invalid register command format");
                    }
                    
                } else if (command.startsWith("/token")) {

                    // /token-{username}-{password}
                    String[] parts = command.split("-");
                    if (parts.length == 3) {
                        username = parts[1];
                        String password = parts[2];
                        String token = authService.authenticate(username, password);
                        if (token != null) {
                            authenticated = true;
                            dout.writeUTF("Token: " + token);
                        } else {
                            dout.writeUTF("Invalid credentials");
                        }
                    } else {
                        dout.writeUTF("Invalid token command format");
                    }

                } else if (authenticated && command.startsWith("/joinChat")) {

                    // /joinChat-{username}
                    String[] parts = command.split("-");
                    if (parts.length == 2) {
                        username = parts[1];
                        activeClients.put(username, dout);
                        dout.writeUTF("Chat joined successfully");
                    } else {
                        dout.writeUTF("Invalid joinChat command format");
                    }

                } else if (authenticated && command.startsWith("/message")) {

                    // /message-{content}
                    String message = command.substring(9);
                    broadcastMessage(username, message);

                } else if (!authenticated) {

                    dout.writeUTF("Please authenticate first");

                } else {

                    dout.writeUTF("Unknown command");

                }
                dout.flush();
            }

        } catch (Exception e) {
            System.err.println("Client disconnected: " + clientSocket);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastMessage(String sender, String message) {
        String fullMessage = sender + ": " + message;
        System.out.println("Broadcasting message: " + fullMessage);

        activeClients.forEach((username, dout) -> {
            try {
                dout.writeUTF(fullMessage);
                dout.flush();
            } catch (IOException e) {
                System.err.println("Failed to send message to " + username);
            }
        });
    }
}
