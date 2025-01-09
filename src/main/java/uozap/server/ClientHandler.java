package uozap.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import uozap.auth.users.User;
import uozap.entities.Message;

/**
 * handles individual client connections in a separate thread.
 * manages message reception and transmission for a single client.
 * extends Thread to allow concurrent client handling.
 */
class ClientHandler extends Thread {
    /** the authenticated user associated with this connection */
    private final User user;
    
    /** the chat room this client is connected to */
    private final Chat chat;
    
    /** socket connection to the client */
    private final Socket clientSocket;
    
    /** input stream for receiving messages from client */
    private final DataInputStream din;
    
    /** output stream for sending messages to client */
    private final ObjectOutputStream oout;
    
    /** flag indicating if the handler thread should continue running */
    private boolean running;

    /**
     * creates a new client handler for a connected user.
     *
     * @param user the authenticated user
     * @param chat the chat room the user is joining
     * @param clientSocket the socket connection to the client
     * @param din the input stream for receiving messages
     * @throws Exception if output stream creation fails
     */
    public ClientHandler(User user, Chat chat, Socket clientSocket, DataInputStream din, ObjectOutputStream oout) throws Exception {
        this.user = user;
        this.chat = chat;
        this.clientSocket = clientSocket;
        this.din = din;
        this.oout = oout;
        this.running = true;
    }

    /**
     * continuously listens for incoming messages from the client.
     * creates Message objects and broadcasts them to the chat.
     * runs until connection is closed or error occurs.
     */
    @Override
    public void run() {
        try {
            while (running) {
                String message = din.readUTF();
                Message chatMessage = new Message(message, user);
                System.out.println("a message was sent by: " + chatMessage.getSender().getUsername() + ": " + chatMessage.getContent());
                chat.broadcastMessage(chatMessage, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * sends a message to the connected client using object serialization.
     * automatically flushes the output stream after writing.
     * 
     * @param message the Message object to send to the client
     */
    public void sendMessage(Message message) {
        try {
            oout.writeObject(message);
            oout.flush();
            System.out.println("a message was bradcasted by: " + message.getSender().getUsername() + ": " + message.getContent());
        } catch (IOException e) {
            System.err.println("error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * performs cleanup when client disconnects or connection errors occur.
     * stops the handler thread, removes user from chat, and closes socket.
     */
    private void cleanup() {
        running = false;
        try {
            chat.removeUser(user);
            clientSocket.close();
        } catch (Exception e) {
            System.err.println("error during cleanup: " + e.getMessage());
        }
    }
}