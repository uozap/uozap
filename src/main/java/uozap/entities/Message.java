package uozap.entities;

import java.io.Serial;
import java.io.Serializable;
import uozap.auth.users.User;
import uozap.utils.State;
import uozap.utils.Time;

/**
 * represents a message in the chat system.
 * implements Serializable to allow transmission over network streams.
 */
public class Message implements Serializable {
    /** serialization version ID */
    @Serial // i have no idea about what this @Serial is, it is used at compile-time...
    private static final long serialVersionUID = 1L;
    
    /** timestamp when the message was created. */
    private final Time time;
    
    /** actual text content of the message. */
    private final String content;
    
    /** user who sent the message. */
    private final User sender;
    
    /** current delivery state of the message. */
    private State state;

    /**
     * creates a new message with the given content and sender.
     * automatically sets creation time and initial state to SENT.
     *
     * @param content the text content of the message
     * @param sender the user who sent the message
     */
    public Message(String content, User sender) {
        this.state = State.SENT;
        this.content = content;
        this.sender = sender;
        this.time = new Time();
    }

    /** @return the text content of the message. */
    public String getContent() { return content; }

    /** @return the user who sent the message. */
    public User getSender() { return sender; }

    /** @return the timestamp when message was created. */
    public Time getTime() { return time; }

    /** @return current delivery state of the message. */
    public State getState() { return state; }

    /** 
     * updates the message delivery state.
     * @param state new state to set.
     */
    public void setState(State state) { this.state = state; }
}