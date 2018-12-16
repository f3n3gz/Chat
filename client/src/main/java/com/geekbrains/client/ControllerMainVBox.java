package com.geekbrains.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML
    TextArea chatHistory;
    @FXML
    Button buttonEnter;
    @FXML
    TextField textFieldInput;
    @FXML
    Button buttonSwitchPanels;
    private Socket sock = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private void initializeConnection() {
        try {
            sock = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
            chatHistory.appendText("connected to " + SERVER_ADDR + ":" + SERVER_PORT + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String s = in.readUTF();
                    if (s.equalsIgnoreCase("end")) break;
                    chatHistory.appendText(" " + s);
                    chatHistory.appendText("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        t.setDaemon(true);
        t.start();

    }


    public void addToChatHistory(ActionEvent actionEvent) {
        if (textFieldInput.getText().length() > 0) {
            chatHistory.appendText(textFieldInput.getText());
            int i = chatHistory.getPrefRowCount();
        }
        try {
            out.writeUTF(textFieldInput.getText());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textFieldInput.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //textFieldInput.requestFocus();
        Platform.runLater(() -> textFieldInput.requestFocus());
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
        if (sock == null)
            initializeConnection();
    }

    public void closeConnection() {
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
