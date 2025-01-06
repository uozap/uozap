package uozap.server;

import uozap.auth.services.AuthService;
import uozap.auth.users.User;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

/**
 * manages socket connections and chat rooms for the messaging server.
 * handles client authentication and connection routing to appropriate chats.
 */
public class SocketManager extends Thread {
    /** maps chat names to their corresponding Chat instances */
    private HashMap<String, Chat> chats;
    
    /** service for authenticating users and validating tokens */
    private final AuthService authService;

    /**
     * initializes the socket manager and starts listening for connections.
     * creates new chats or routes clients to existing ones based on requests.
     *
     * @param authService service used to validate user tokens
     */
    public SocketManager(AuthService authService) {
        this.chats = new HashMap<String, Chat>();
        this.authService = authService;
    }

    /**
     * starts the socket manager and listens for incoming connections.
     * creates a new chat for each new connection.
     */
    @Override
    public void run() {
        System.out.println("multiServer started...");

        // start listening on port 7878.
        try (ServerSocket serverSocket = new ServerSocket(7878)) {
            while(true) {
                /*
                 * the server accepts a client connection and creates a new socket for it.
                 */
                Socket clientSocket = serverSocket.accept();
                DataInputStream din = new DataInputStream(clientSocket.getInputStream());
                ObjectOutputStream oout = new ObjectOutputStream(clientSocket.getOutputStream());

                String chatName = din.readUTF();
                String token = din.readUTF();

                try {
                    User user = authService.getTokenService().validateToken(token);

                    /*
                     * the client is then routed to the appropriate chat room based on the request.
                     */
                    Chat chat;
                    if(!chats.containsKey(chatName)) {
                        chat = new Chat(chatName, UUID.randomUUID());
                        chats.put(chatName, chat);
                        chat.start();
                    } else {
                        chat = chats.get(chatName);
                    }

                    /*
                     * the client handler is started in a new thread to handle the client connection.
                     * the client handler is responsible for sending and receiving messages.
                     */
                    ClientHandler clientHandler = new ClientHandler(user, chat, clientSocket, din, oout);
                    chat.addClientHandler(clientHandler);
                    clientHandler.start();

                    System.out.println("user: " + user.getUsername() + " joined chat: " + chatName);

                } catch (Exception e) {
                    System.err.println("error: " + e.getMessage());
                    try {
                        clientSocket.close();
                    } catch (IOException ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }

}

