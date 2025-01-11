package uozap.server;

import java.util.ArrayList;
import java.util.UUID;

import uozap.auth.users.User;
import uozap.entities.Message;

/**
 * represents a chat room in the messaging system.
 * manages users, messages and client connections.
 */
public class Chat extends Thread {
     /** unique name identifier for the chat */
     public final String name;
    
     /** unique identifier for the chat instance */
     private final UUID chatID;
     
     /** list of users currently in the chat */
     private final ArrayList<User> users;
     
     /** history of messages in the chat */
     private final ArrayList<Message> messages;
     
     /** list of active client connections */
     private final ArrayList<ClientHandler> clientHandlers;

    /**
     * creates a new chat room with specified name and ID.
     *
     * @param name the chat room name
     * @param chatID unique identifier for this chat
     */
    public Chat(String name, UUID chatID) {
        this.name = name;
        this.chatID = chatID;
        this.users = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.clientHandlers = new ArrayList<>();
    }

     /**
     * adds a new client connection handler to the chat.
     */
    public void addClientHandler(ClientHandler handler) {
        clientHandlers.add(handler);
    }

    /**
     * simply add a new user to the chat.
     */
    public void addUser(User u) throws Exception {
        if (isUserInChat(u)) {
            throw new Exception("user already present in the chat");
        }
        users.add(u);
    }

    /**
     * simply remove a user from the chat.
     */
    public void removeUser(User u) throws Exception{
        users.remove(u);
    }

    /**
     * simply add the new message to the list.
     */
    private void addMessage(Message m) throws Exception{
        messages.add(m);
    }

    /**
     * send the message to all the connected clients but the one which sent it.
     * @param message the message to send
     * @param sender the client who sent it
     */
    public void broadcastMessage(Message message, ClientHandler sender) {
        try {
            addMessage(message);
            for (ClientHandler handler : clientHandlers) {
                if (handler != sender) {
                    handler.sendMessage(message);
                }
            }
        } catch (Exception e) {
            System.err.println("error broadcasting message: " + e.getMessage());
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        try {
            addMessage(new Message(message, sender.getUser()));
            String formattedMessage = sender.getName() + ": " + message;
            for (ClientHandler handler : clientHandlers) {
                if (handler != sender) {
                    handler.sendMessage(formattedMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting message: " + e.getMessage());
        }
    }

    // remove message later on

    /**
     * check if the user is already logged into the chat.
     */
    public boolean isUserInChat(User u){
        return users.contains(u);
    }

    /**
     * @return the chat's UUID.
     */
    public UUID getChatID() { return chatID; }

    /**
     * @return the messages array list.
     */
    public ArrayList<Message> getMessages() { return messages; }

    /**
     * @return the list of users connected to the chat.
     */
    public ArrayList<User> getUsers() { return users; }

    /**
     * @return the whole message history (but creating a new object).
     */
    public ArrayList<Message> getMessageHistory() { return new ArrayList<>(messages); }
}


