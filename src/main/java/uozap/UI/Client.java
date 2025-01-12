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

public class Client extends JFrame {
    private JTextField userInput;
    private JTextField emailInput;
    private JPasswordField passInput;
    private boolean isLoginMode = false;
    
    private Socket socket;
    private DataInputStream din;
    private DataOutputStream dout;
    private String username;
    private String currentChat;
    private volatile boolean running = true;
    
    private JFrame chatFrame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField joinChatField;
    private JButton joinChatButton;

    public Client() {
        setupLoginWindow();
        connectToServer();
    }

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

    private void handleSubmit() {
        try {
            String user = userInput.getText();
            String pass = new String(passInput.getPassword());

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
                String email = emailInput.getText();
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
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                chatArea.append("Error sending message\n");
            }
        });

        startMessageHandler();
        chatFrame.setVisible(true);
    }

    private void startMessageHandler() {
        new Thread(() -> {
            try {
                System.out.println("waiting for a message");
                while (running) {
                    
                    System.out.println("running");
                    String message = din.readUTF();
                    System.out.println("Client received: " + message);
                    if (message != null && !message.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(message + "\n");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}