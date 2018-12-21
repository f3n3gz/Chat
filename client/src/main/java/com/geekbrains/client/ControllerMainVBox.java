package com.geekbrains.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ControllerMainVBox implements Initializable {
    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 1000;
    public Label labelNickname;
    @FXML
    public TextField fieldPassword;
    @FXML
    public TextField fieldLogin;
    @FXML
    public VBox msgPanel;
    @FXML
    public HBox authPanel;
    Thread currentThread;
    @FXML
    TextArea chatHistory;
    @FXML
    Button buttonEnter;
    @FXML
    TextField textFieldInput;
    @FXML
    Button buttonSwitchPanels;
    private boolean authentificated;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private Socket sock = new Socket();
    private String nickName;

    public void setAuthentificated(boolean authentificated) {
        this.authentificated = authentificated;
        authPanel.setVisible(!authentificated);
        authPanel.setManaged(!authentificated);
        msgPanel.setVisible(authentificated);
        msgPanel.setManaged(authentificated);
    }

    private void initializeConnection() {
        try {
            sock = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
            System.out.println("connected to " + SERVER_ADDR + ":" + SERVER_PORT + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentThread = new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    //убрать
                    System.out.println(msg);

                    if (msg.startsWith("/authok")) {
                        // /authok authorizedNickName
                        this.nickName = msg.split("\\s")[1];
                        Platform.runLater(() -> {
                            labelNickname.setText(nickName);
                            labelNickname.setPrefWidth(chatHistory.getWidth());
                            labelNickname.setPrefHeight(25);
                        });
                        setAuthentificated(true);
                        chatHistory.appendText("Logged in as " + nickName + " on " + SERVER_ADDR + ":" + SERVER_PORT + "\n");
                        break;
                    } else if (msg.startsWith("/autherror")) {
                        throw new AuthErrorException("Authorization error");
                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    if (msg.equalsIgnoreCase("/end")) break;
                    chatHistory.appendText(" " + msg);
                    chatHistory.appendText("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnection();
                setAuthentificated(false);
            }
        });
        currentThread.setDaemon(true);
        currentThread.start();

    }

    public void sendMessage(String text) {
        try {
            out.writeUTF(text);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToChatHistory(ActionEvent actionEvent) {
        String msg = textFieldInput.getText();
//        if (!msg.isEmpty()) {
//            chatHistory.appendText(msg);
//        }
        sendMessage(msg);
        textFieldInput.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthentificated(false);
    }

    public void textFieldKey(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            addToChatHistory(new ActionEvent());
        }
    }

    public void switchPanel(ActionEvent actionEvent) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("scrollPanel.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 300, 400);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {

        }


    }

    public void connectToServer(ActionEvent actionEvent) {

        if (sock.isConnected()) {
            closeConnection();
        }
        initializeConnection();
        sendAuth();
    }

    public void sendAuth() {
        try {
            out.writeUTF("/auth " + fieldLogin.getText() + " " + fieldPassword.getText());
            fieldPassword.clear();
            fieldLogin.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        currentThread.interrupt();
        this.nickName = null;
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
