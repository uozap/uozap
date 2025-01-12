package uozap.UI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * client-side implementation of the chat application.
 * handles user interface and network communication with the server.
 */
public class Client extends JFrame {

    /** text field for username input during login/registration. */
    private JTextField userInput;

    /** text field for email input during registration. */
    private JTextField emailInput;
    
    /** password field for secure password input. */
    private JPasswordField passInput;
    
    /** indicates whether the login window is in login or register mode. */
    private boolean isLoginMode = false;
    
    /** socket connection to the server. */
    private Socket socket;
    
    /** input stream for receiving server messages. */
    private DataInputStream din;
    
    /** output stream for sending messages to server. */
    private DataOutputStream dout;
    
    /** current user's username. */
    private String username;
    
    /** name of the current chat room. */
    private String currentChat;
    
    /** flag to control message handling thread. */
    private volatile boolean running = true;
    
    /** main chat window frame. */
    private JFrame chatFrame;
    
    /** text area for displaying chat messages. */
    private JTextArea chatArea;
    
    /** text field for composing messages. */
    private JTextField messageField;
    
    /** text field for entering chat room name. */
    private JTextField joinChatField;
    
    /** button for joining chat rooms. */
    private JButton joinChatButton;

    /**
     * creates a new client instance and initializes the login window.
     * establishes connection to the server.
     */
    public Client() {
        setupLoginWindow();
        connectToServer();
    }

    /**
     * sets up the initial login/registration window with input fields and buttons.
     */
    private void setupLoginWindow() {
        setTitle("UoZap Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        userInput = new JTextField();
        emailInput = new JTextField();
        passInput = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(userInput);
        panel.add(new JLabel("Email:"));
        panel.add(emailInput);
        panel.add(new JLabel("Password:"));
        panel.add(passInput);

        JButton submitButton = new JButton("Register");
        JButton toggleButton = new JButton("Switch to Login");

        toggleButton.addActionListener(e -> {
            isLoginMode = !isLoginMode;
            submitButton.setText(isLoginMode ? "Login" : "Register");
            toggleButton.setText(isLoginMode ? "Switch to Register" : "Switch to Login");
            emailInput.setEnabled(!isLoginMode);
        });

        submitButton.addActionListener(e -> handleSubmit());

        panel.add(toggleButton);
        panel.add(submitButton);

        add(panel);
        setVisible(true);
    }

    /**
     * handles form submission for both login and registration.
     * sends appropriate commands to server and processes responses.
     */
    private void handleSubmit() {
        try {
            String user = userInput.getText();
            // Checking if the field username is correct
            if (!user.matches("^[a-zA-Z][a-zA-Z0-9._]{2,19}$")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Username not valid. It must be 3-20 characters long, start with a letter, and can include letters, digits, '.', or '_'.",
                        "Invalid Username",
                        JOptionPane.WARNING_MESSAGE
                );
                 // Stop processing further if username is invalid
                throw new IOException("Username field not valid");
            }

            // Checking if the field password is correct
            String pass = new String(passInput.getPassword());
            System.out.println(pass);
            if (!pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&+=])[A-Za-z\\d@#$!%^&+=]{8,20}$")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Password not valid. It must meet the following criteria:\n" +
                                "- Between 8 and 20 characters long\n" +
                                "- At least one lowercase letter (a-z)\n" +
                                "- At least one uppercase letter (A-Z)\n" +
                                "- At least one digit (0-9)\n" +
                                "- At least one special character from the set: !@#$%^&+=\n" +
                                "- Can only include letters, digits, and these special characters",
                        "Invalid Password",
                        JOptionPane.WARNING_MESSAGE
                );
                throw new IOException("Password field not valid");
            }
            if (isLoginMode) {
                dout.writeUTF("/token-" + user + "-" + pass);
                dout.flush();
                String response = din.readUTF();
                
                if (response.startsWith("Token:")) {
                    username = user;
                    this.setVisible(false);
                    createChatWindow();
                } else {
                    JOptionPane.showMessageDialog(this, "Login failed: " + response);
                }
            } else {
                //Checking if the field email is correct
                String email = emailInput.getText();
                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Email not valid. It must meet the following criteria:\n" +
                                    "- Must include an '@' symbol separating the username and domain\n" +
                                    "- The username can contain letters, digits, dots (.), underscores (_), and hyphens (-)\n" +
                                    "- The domain must include at least one '.' and only letters or digits\n" +
                                    "- No spaces or special characters outside of the allowed ones\n" +
                                    "- Example: user@example.com",
                            "Invalid Email",
                            JOptionPane.WARNING_MESSAGE
                    );
                    throw new IOException("Email field not valid");
                }
                dout.writeUTF("/register-" + user + "-" + email + "-" + pass);
                dout.flush();

                String response = din.readUTF();
                if (response.equals("User registered successfully")) {
                    JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                    isLoginMode = true;
                    emailInput.setEnabled(false);
                    userInput.setText("");
                    passInput.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed: " + response);
                }

            }
            dout.flush();

            // String response = din.readUTF();
            // if (response.startsWith("Token:") || response.equals("User registered successfully")) {
            //     username = user;
            //     this.setVisible(false);
            //     createChatWindow();
            // } else {
            //     JOptionPane.showMessageDialog(this, response);
            // }
        } catch (IOException ex) {
            System.err.println("Error sending login/register request: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * creates and configures the main chat window interface.
     * sets up message input, chat display, and chat room joining components.
     */
    private void createChatWindow() {
        chatFrame = new JFrame("UoZap Chat");
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setSize(600, 400);
        chatFrame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new BorderLayout());
        joinChatField = new JTextField();
        joinChatButton = new JButton("Join Chat");
        topPanel.add(joinChatField, BorderLayout.CENTER);
        topPanel.add(joinChatButton, BorderLayout.EAST);
        chatFrame.add(topPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatFrame.add(inputPanel, BorderLayout.SOUTH);

        joinChatButton.addActionListener(e -> {
            try {
                String chatName = joinChatField.getText();
                if (!chatName.trim().isEmpty()) {
                    dout.writeUTF("/joinChat-" + chatName);
                    dout.flush();

                    joinChatField.setEnabled(false);
                    joinChatButton.setEnabled(false);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        sendButton.addActionListener(e -> {
            try {
                String message = messageField.getText();
                if (!message.trim().isEmpty()) {
                    dout.writeUTF("/message-" + message);
                    dout.flush();
                    messageField.setText("");

                    chatArea.append("you: " + message + "\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                } else {
                    System.out.println("error sending the message");                    
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                chatArea.append("Error sending message\n");
            }
        });

        startMessageHandler();
        chatFrame.setVisible(true);
    }

    /**
     * starts a background thread to handle incoming messages from the server.
     * updates the chat display when new messages arrive.
     */
    private void startMessageHandler() {
        new Thread(() -> {
            try {
                System.out.println("waiting for a message");
                while (running) {
                    
                    System.out.println("running");
                    String message = din.readUTF();
                    System.out.println("Client received: " + message);
                    if (message != null && !message.isEmpty()) {

                        // avoid sending the message part back to the sender
                        String cleanMessage = message.replace("/message", "");
                        String cleanMessage1 = cleanMessage.replace("/message-", "");
                        String cleanMessage2 = cleanMessage1.replace("essage-", "");
                        String cleanMessage3 = cleanMessage2.replace("essage", "");
                        
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(cleanMessage3 + "\n");
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        });
                    }
                }
                System.err.println("while closed");
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> 
                        chatArea.append("Lost connection to server\n"));
                }
            }
        }).start();
    }

    /**
     * establishes connection to the chat server.
     * initializes input and output streams for communication.
     */
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 7878);
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * performs cleanup of resources when client is shutting down.
     * closes network connections and streams.
     */
    public void cleanup() {
        running = false;
        try {
            if (din != null) din.close();
            if (dout != null) dout.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * entry point for the client application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}