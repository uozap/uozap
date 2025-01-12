package uozap.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import uozap.auth.services.AuthService;
import uozap.auth.users.User;
import uozap.entities.Message;

/**
 * manages socket connections and chat rooms for the messaging server.
 * handles client authentication and connection routing to appropriate chats.
 */
public class SocketManager extends Thread {
    /** stores output streams for all active client connections. */
    private final ConcurrentHashMap<String, DataOutputStream> activeClients;
    
    /** service handling user authentication and management. */
    private final AuthService authService;
    
    /** maps chat room names to their respective Chat instances. */
    private final ConcurrentHashMap<String, Chat> chatRooms;
    
    /** maps usernames to their current chat room names. */
    private final ConcurrentHashMap<String, String> userChatMap;
    
    /** maps usernames to their respective client handlers. */
    private final Map<String, ClientHandler> clientHandlers;

    /**
     * constructs a new SocketManager with the specified authentication service.
     * initializes all required collections for managing client connections and chat rooms.
     *
     * @param authService service to validate user credentials
     */
    public SocketManager(AuthService authService) {
        this.activeClients = new ConcurrentHashMap<>();
        this.chatRooms = new ConcurrentHashMap<>();
        this.userChatMap = new ConcurrentHashMap<>();
        this.clientHandlers = new ConcurrentHashMap<>();
        this.authService = authService;
    }

    /**
     * starts the socket server and handles incoming client connections.
     * runs in a separate thread and continuously accepts new client connections.
     * for each connection, creates a new handler thread to process client requests.
     */
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

    /**
     * retrieves the chat room with the specified name, creating a new one if it does not exist.
     */
    private Chat getOrCreateChat(String chatName) {
        return chatRooms.computeIfAbsent(chatName, 
            name -> new Chat(name, UUID.randomUUID()));
    }

    /**
     * handles the client's request to join a chat room.
     */
    private void handleJoinChat(String chatName, String username, Socket socket, 
                              DataInputStream din, DataOutputStream dout) throws Exception {
        Chat chat = getOrCreateChat(chatName);
        User user = authService.getUserService().getUserByUsername(username);
        
        if (user != null) {
            
            //ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
            // DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            System.out.println("Creating handler for user: " + username);
            ClientHandler handler = new ClientHandler(user, chat, socket, din, dout);
            chat.addClientHandler(handler);
            chat.addUser(user);
            
            

            clientHandlers.put(username, handler); 
            System.out.println("Registered handler for: " + username);
            handler.start();

            userChatMap.put(username, chatName);
            activeClients.put(username, dout);

            System.out.println("Handler setup complete for: " + username);
            dout.writeUTF("Chat joined successfully");
            dout.flush();
        } else {
            dout.writeUTF("Failed to join chat");
            dout.flush();
        }
    }

    /**
     * handles the client connection by processing incoming commands.
     * supports user registration, authentication, chat joining, and message broadcasting.
     */
    private void handleClient(Socket clientSocket) {
        String username = null;
        DataInputStream din = null;
        DataOutputStream dout = null;

        try{
            din = new DataInputStream(clientSocket.getInputStream());
            dout = new DataOutputStream(clientSocket.getOutputStream()); 

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
                        dout.flush();
                    } else {
                        dout.writeUTF("Invalid register command format");
                        dout.flush();
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
                            dout.flush();
                        } else {
                            dout.writeUTF("Invalid credentials");
                            dout.flush();
                        }
                    } else {
                        dout.writeUTF("Invalid token command format");
                        dout.flush();
                    }

                } else if (authenticated && command.startsWith("/joinChat")) {
                    
                    // /joinChat-{chatname}
                    String[] parts = command.split("-");
                    if (parts.length == 2) {
                        String chatName = parts[1];
                        handleJoinChat(chatName, username, clientSocket, din, dout);
                    } else {
                        dout.writeUTF("Invalid joinChat command format");
                        dout.flush();
                    }

                } else if (authenticated && (command.startsWith("/message") || command.startsWith("essage") )) {

                    // /message-{content}
                    // String message = command.substring(9);
                    String message = "error retriving this message";
                    if (command.startsWith("/message")){
                        message = command.replaceFirst("^/message-", "");
                    } else {
                        message = command.replaceFirst("^essage-", "");
                    }

                    if (message.startsWith("/message")){
                        message = message.replaceFirst("^/message-", ""); 
                        // fixing cases where removing the substring is not enought
                    }
                    
                    String chatName = userChatMap.get(username);
                    Chat chat = chatRooms.get(chatName);
                    if (chat != null) {
                        User sender = authService.getUserService().getUserByUsername(username);
                        Message chatMessage = new Message(message, sender);
                        ClientHandler handler = clientHandlers.get(username);
                        if (handler == null) {
                            System.err.println("No handler found for user: " + username);
                            // re-create handler if missing
                            handler = new ClientHandler(sender, chat, clientSocket, din, dout);
                            clientHandlers.put(username, handler);
                            chat.addClientHandler(handler);
                        }
                        chat.broadcastMessage(chatMessage, handler);
                    }

                } else if (!authenticated) {

                    dout.writeUTF("Please authenticate first");  
                    dout.flush();

                } else {

                    dout.writeUTF("Unknown command");
                    dout.flush();

                }
            }

            // System.out.println("HOW DID THE WHILE CLOSE WHIT NO EXCEPTION");

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

    // private void broadcastMessage(String sender, String message) {
    //     String fullMessage = sender + ": " + message;
    //     System.out.println("Broadcasting message: " + fullMessage); // Debug line
    //     String chatName = userChatMap.get(sender);
    //     if (chatName != null) {
    //         System.out.println("Sending to chat: " + chatName); // Debug line
    //         activeClients.forEach((username, dout) -> {
    //             if (chatName.equals(userChatMap.get(username)) && !username.equals(sender)) {
    //                 try {
    //                     dout.writeUTF(fullMessage);
    //                     dout.flush();
    //                     System.out.println("Message sent to: " + username); // Debug line
    //                 } catch (IOException e) {
    //                     System.err.println("Failed to send message to " + username);
    //                 }
    //             }
    //         });
    //     }
    // }

}
