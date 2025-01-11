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

    public Client() {
        setTitle("Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void setupUI() {
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

        // Email field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailInput = new JTextField(20);
        mainPanel.add(emailInput, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passInput = new JPasswordField(20);
        mainPanel.add(passInput, gbc);

        // Register button
        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> handleRegistration());
        mainPanel.add(registerButton, gbc);

        add(mainPanel);
    }

    private void handleRegistration() {
        String username = userInput.getText();
        String email = emailInput.getText();
        String password = new String(passInput.getPassword());

        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 7878);
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                DataInputStream din = new DataInputStream(socket.getInputStream());

                dout.writeUTF("/register-" + username + "-" + email + "-" + password);
                dout.flush();
                System.out.println(din.readUTF());

                dout.writeUTF("/token-" + username + "-" + password);
                dout.flush();
                String tokenResponse = din.readUTF();
                if (!tokenResponse.startsWith("Token: ")) {
                    System.out.println("Authentication failed: " + tokenResponse);
                    socket.close();
                    return;
                }
                String token = tokenResponse.substring(7);

                dout.writeUTF("/joinChat-" + username);
                dout.flush();
                String joinResponse = din.readUTF();
                if (joinResponse.startsWith("Chat joined successfully")) {
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        openChatWindow(socket, dout, din, username);
                    });
                } else {
                    System.out.println("Join chat failed: " + joinResponse);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void openChatWindow(Socket socket, DataOutputStream dout, DataInputStream din, String username) {
        JFrame chatFrame = new JFrame("Chat - " + username);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setLayout(new BorderLayout());

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

        // Message receiving thread
        new Thread(() -> {
            try {
                while (true) {
                    String incomingMessage = din.readUTF();
                    SwingUtilities.invokeLater(() -> 
                        chatArea.append(incomingMessage + "\n")
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        // Send button action
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

        // Enter key listener for message field
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
            }
        });

        // Window closing handler
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
        SwingUtilities.invokeLater(() -> {
            new Client().setVisible(true);
        });
    }
}