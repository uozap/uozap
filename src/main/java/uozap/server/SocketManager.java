package uozap.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import uozap.auth.services.AuthService;
import uozap.auth.users.User;
import uozap.entities.Message;

/**
 * Manages socket connections and chat rooms for the messaging server.
 * Handles client authentication and connection routing to appropriate chats.
 */
public class SocketManager extends Thread {
    private final ConcurrentHashMap<String, DataOutputStream> activeClients;
    private final AuthService authService;
    private final ConcurrentHashMap<String, Chat> chatRooms;
    private final ConcurrentHashMap<String, String> userChatMap;

    public SocketManager(AuthService authService) {
        this.activeClients = new ConcurrentHashMap<>();
        this.chatRooms = new ConcurrentHashMap<>();
        this.userChatMap = new ConcurrentHashMap<>();
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

    private Chat getOrCreateChat(String chatName) {
        return chatRooms.computeIfAbsent(chatName, 
            name -> new Chat(name, UUID.randomUUID()));
    }

    private void handleJoinChat(String chatName, String username, Socket socket, 
                              DataInputStream din, DataOutputStream dout) throws Exception {
        Chat chat = getOrCreateChat(chatName);
        User user = authService.getUserService().getUserByUsername(username);
        
        if (user != null) {
            chat.addUser(user);
            ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
            ClientHandler handler = new ClientHandler(user, chat, socket, din, oout);
            chat.addClientHandler(handler);
            
            handler.start();

            userChatMap.put(username, chatName);
            activeClients.put(username, dout);
            dout.writeUTF("Chat joined successfully");
        } else {
            dout.writeUTF("Failed to join chat");
        }
    }

    private void handleClient(Socket clientSocket) {
        String username = null;
        try (DataInputStream din = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream())) {

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
                    
                    // /joinChat-{chatname}
                    String[] parts = command.split("-");
                    if (parts.length == 3) {
                        String chatName = parts[1];
                        handleJoinChat(chatName, username, clientSocket, din, dout);
                    } else {
                        dout.writeUTF("Invalid joinChat command format");
                    }

                } else if (authenticated && command.startsWith("/message")) {

                    // /message-{content}
                    String message = command.substring(9);
                    String chatName = userChatMap.get(username);
                    Chat chat = chatRooms.get(chatName);
                    if (chat != null) {
                        User sender = authService.getUserService().getUserByUsername(username);
                        Message chatMessage = new Message(message, sender);
                        chat.broadcastMessage(chatMessage, null);
                    }

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
            if (username != null) {
                activeClients.remove(username);
                userChatMap.remove(username);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastMessage(String sender, String message) {
        String fullMessage = sender + ": " + message;
        System.out.println("Broadcasting message: " + fullMessage); // Debug line

        String chatName = userChatMap.get(sender);
        if (chatName != null) {
            System.out.println("Sending to chat: " + chatName); // Debug line
            activeClients.forEach((username, dout) -> {
                if (chatName.equals(userChatMap.get(username)) && !username.equals(sender)) {
                    try {
                        dout.writeUTF(fullMessage);
                        dout.flush();
                        System.out.println("Message sent to: " + username); // Debug line
                    } catch (IOException e) {
                        System.err.println("Failed to send message to " + username);
                    }
                }
            });
        }
    }
}
