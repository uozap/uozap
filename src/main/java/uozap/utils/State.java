package uozap.utils;

/**
 * this enum represents the different states a message can be in.
 * used to track message delivery and read status.
 */
public enum State {
    SENT,           // The message has been sent by the sender
    DELIVERED,      // The message has been delivered to the recipient(s)
    SEEN;           // The message has been seen by all recipients

    /**
     * converts the state enum to a human-readable string format.
     *
     * @return string representation of the message state
     * @throws IllegalArgumentException if the state is invalid
     */
    @Override
    public String toString() {
        return switch (this) {
            case SENT -> "Sent";
            case DELIVERED -> "Delivered";
            case SEEN -> "Seen";
            default -> throw new IllegalArgumentException("Unexpected value: " + this);
        };
    }
}
