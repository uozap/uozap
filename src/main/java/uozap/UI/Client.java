package uozap.UI;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

    public Client() {
        showInitialChoice();
    }

    private void showInitialChoice() {
        // Initial choice window
        setTitle("Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel choicePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(e -> {
            isLoginMode = true;
            getContentPane().removeAll();
            setupUI();
            pack();
            setLocationRelativeTo(null);
        });

        registerButton.addActionListener(e -> {
            isLoginMode = false;
            getContentPane().removeAll();
            setupUI();
            pack();
            setLocationRelativeTo(null);
        });

        choicePanel.add(loginButton, gbc);
        gbc.gridx = 1;
        choicePanel.add(registerButton, gbc);

        add(choicePanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        setTitle(isLoginMode ? "Login" : "Register");
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        userInput = new JTextField(20);
        mainPanel.add(userInput, gbc);

        // Email field (only for register)
        if (!isLoginMode) {
            gbc.gridx = 0;
            gbc.gridy = 1;
            mainPanel.add(new JLabel("Email:"), gbc);

            gbc.gridx = 1;
            emailInput = new JTextField(20);
            mainPanel.add(emailInput, gbc);
        }

        // Password field
        gbc.gridx = 0;
        gbc.gridy = isLoginMode ? 1 : 2;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passInput = new JPasswordField(20);
        mainPanel.add(passInput, gbc);

        // Action button
        gbc.gridx = 1;
        gbc.gridy = isLoginMode ? 2 : 3;
        JButton actionButton = new JButton(isLoginMode ? "Login" : "Register");
        actionButton.addActionListener(e -> handleAction());
        mainPanel.add(actionButton, gbc);

        // Back button
        gbc.gridx = 0;
        gbc.gridy = isLoginMode ? 2 : 3;
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            getContentPane().removeAll();
            showInitialChoice();
            pack();
            setLocationRelativeTo(null);
        });
        mainPanel.add(backButton, gbc);

        add(mainPanel);
    }

    private void handleAction() {
        String username = userInput.getText();
        String password = new String(passInput.getPassword());
    
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 7878);
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                DataInputStream din = new DataInputStream(socket.getInputStream());
    
                if (isLoginMode) {
                    sendCommand(dout, "/token-" + username + "-" + password);
                } else {
                    String email = emailInput.getText();
                    sendCommand(dout, "/register-" + username + "-" + email + "-" + password);
                    System.out.println(din.readUTF());
                    sendCommand(dout, "/token-" + username + "-" + password);
                }
    
                String tokenResponse = din.readUTF();
                if (!tokenResponse.startsWith("Token: ")) {
                    System.out.println("Authentication failed: " + tokenResponse);
                    socket.close();
                    return;
                }
                String token = tokenResponse.substring(7);
    
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    openChatWindow(socket, dout, din, username);
                });
    
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void sendCommand(DataOutputStream dout, String command) throws IOException {
        dout.writeUTF(command);
        dout.flush();
    }
    
    private void openChatWindow(Socket socket, DataOutputStream dout, DataInputStream din, String username) {
        JFrame chatFrame = new JFrame("Chat - " + username);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JTextField joinChatField = new JTextField();
        JButton joinChatButton = new JButton("Join Chat");
        topPanel.add(joinChatField, BorderLayout.CENTER);
        topPanel.add(joinChatButton, BorderLayout.EAST);
        chatFrame.add(topPanel, BorderLayout.NORTH);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatFrame.add(inputPanel, BorderLayout.SOUTH);

        joinChatButton.addActionListener(e -> {
            try {
                String chatName = joinChatField.getText();
                dout.writeUTF("/joinChat-" + chatName);
                dout.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        new Thread(() -> {
            try {
                while (true) {
                    String incomingMessage = din.readUTF();
                    SwingUtilities.invokeLater(() -> chatArea.append(incomingMessage + "\n"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        sendButton.addActionListener(e -> {
            try {
                String message = messageField.getText();
                dout.writeUTF("/message-" + message);
                dout.flush();
                messageField.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
            }
        });

        chatFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        chatFrame.setSize(400, 300);
        chatFrame.setLocationRelativeTo(null);
        chatFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client().setVisible(true));
    }
}
