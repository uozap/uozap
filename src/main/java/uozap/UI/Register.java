package uozap.UI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Register extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("Register");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label userLabel = new Label("Username:");
        GridPane.setConstraints(userLabel, 0, 0);
        TextField userInput = new TextField();
        GridPane.setConstraints(userInput, 1, 0);

        Label emailLabel = new Label("Email:");
        GridPane.setConstraints(emailLabel, 0, 1);
        TextField emailInput = new TextField();
        GridPane.setConstraints(emailInput, 1, 1);

        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 2);
        PasswordField passInput = new PasswordField();
        GridPane.setConstraints(passInput, 1, 2);

        Button registerButton = new Button("Register");
        GridPane.setConstraints(registerButton, 1, 3);

        registerButton.setOnAction(e -> {
            String username = userInput.getText();
            String email = emailInput.getText();
            String password = passInput.getText();

            new Thread(() -> {
                try {
                    Socket socket = new Socket("localhost", 7878);
                    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                    DataInputStream din = new DataInputStream(socket.getInputStream());

                    dout.writeUTF("/register-" + username + "-" + email + "-" + password);
                    dout.flush();
                    System.out.println(din.readUTF());

                    dout.writeUTF("/token-" + username +  "-" + password);
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
                        Platform.runLater(() -> {
                            primaryStage.close();
                            openChatWindow(socket, dout, din, "username1");
                        });
                    } else {
                        System.out.println("Join chat failed: " + joinResponse);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        grid.getChildren().addAll(userLabel, userInput, emailLabel, emailInput, passLabel, passInput, registerButton);

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openChatWindow(Socket socket, DataOutputStream dout, DataInputStream din, String username) {
        Stage chatStage = new Stage();
        chatStage.setTitle("Chat - " + username);

        BorderPane chatLayout = new BorderPane();
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatLayout.setCenter(chatArea);

        HBox inputBox = new HBox();
        inputBox.setSpacing(10);

        TextField messageField = new TextField();
        Button sendButton = new Button("Send");

        inputBox.getChildren().addAll(messageField, sendButton);
        chatLayout.setBottom(inputBox);

        new Thread(() -> {
            try {
                while (true) {
                    String incomingMessage = din.readUTF();
                    Platform.runLater(() -> chatArea.appendText(incomingMessage + "\n"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        sendButton.setOnAction(event -> {
            try {
                String message = messageField.getText();
                dout.writeUTF("/message-" + message);
                dout.flush();
                messageField.clear();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Scene chatScene = new Scene(chatLayout, 400, 300);
        chatStage.setScene(chatScene);
        chatStage.setOnCloseRequest(event -> {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        chatStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
