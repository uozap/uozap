package uozap.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * this class handles timestamp management for messages and other time-sensitive operations.
 */
public class Time {
    private LocalDateTime timestamp;

    /**
     * creates a new Time instance with current date and time.
     */
    public Time() {
        // Initialize with the current date and time
        this.timestamp = LocalDateTime.now();
    }

    /**
     * creates a new Time instance with a specific timestamp.
     *
     * @param timestamp the LocalDateTime to initialize with
     */
    public Time(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * retrieves the stored timestamp.
     *
     * @return the LocalDateTime timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * updates the stored timestamp.
     *
     * @param timestamp the new LocalDateTime to store
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * converts the timestamp to a formatted string.
     *
     * @return string representation of the timestamp in "yyyy-MM-dd HH:mm:ss" format
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
}

