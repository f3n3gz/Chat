package com.geekbrains.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;

public class ControllerMainVBox implements Initializable {
    @FXML
    public ListView<String> clientsListView;
    @FXML
    public TextField passwordField;
    @FXML
    public TextField loginField;
    final int CHAT_HISTORY_SIZE = 4;
    @FXML
    public VBox msgPanel;
    @FXML
    public HBox authPanel;
    @FXML
    TextArea chatHistory;
    @FXML
    Button buttonEnter;
    @FXML
    TextField textFieldInput;
    @FXML
    Button buttonSwitchPanels;
    @FXML
    public Label labelNickname;
    private String nickname;
    private HashSet<String> clientsList;
    private String login;

    public void setAuthentificate(boolean authenticated) {
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
    }

    public void sendMessage(ActionEvent actionEvent) {
        String msg = textFieldInput.getText();
        if (msg.equalsIgnoreCase("/close")) {
            ChatHistory.close();
            return;
        }
        if (Network.sendMessage(msg)) {
            textFieldInput.clear();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthentificate(Network.isAuthenticated());
        linkCallbacks();
    }

    public void textFieldKey(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            sendMessage(new ActionEvent());
        }
    }

    private void sendAuth() {
        Network.authorization(loginField.getText(), passwordField.getText());
        login = loginField.getText();
        loginField.clear();
        passwordField.clear();
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

    public void linkCallbacks() {
        Network.setCallOnException(args -> showAlert((String) args[0]));

        Network.setCallOnCloseConnection(args -> setAuthentificate(false));

        Network.setCallOnAuthenticated(args -> {
            try {
                System.out.println("CallOnAuthenticated");

                nickname = args[0].toString();
                ChatHistory.openChatHistory(login);
                // берем из сообения из истории
                ArrayList<String> messages = ChatHistory.getLastMessages(CHAT_HISTORY_SIZE);
                if (messages != null) {
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        chatHistory.appendText(messages.get(i)); // ? - /n
                    }
                }
                setAuthentificate(true);
                System.out.println(nickname);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        Network.setCallOnMsgReceived(args -> {
            String msg = args[0].toString();
            chatHistory.appendText(msg + "\n");
            ChatHistory.add(msg);
        });

        Network.setCallOnClientsListReceive(args -> {
            clientsList = (HashSet<String>) args[0];
            Platform.runLater(() -> {
                clientsListView.getItems().clear();
                clientsListView.getItems().setAll(clientsList);
            });
        });
        Network.setCallOnNickChange(args -> {
            this.nickname = args[1].toString();
        });
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
            alert.showAndWait();
        });

    }

    public void connectToServer(ActionEvent actionEvent) {
        if (!Network.isConnected()) {
            Network.initializeConnection();
        }
        if (Network.isConnected()) sendAuth();
    }
}
