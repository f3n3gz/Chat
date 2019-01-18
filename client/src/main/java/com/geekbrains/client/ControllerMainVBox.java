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
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class ControllerMainVBox implements Initializable {
    @FXML
    public ListView<String> clientsListView;
    @FXML
    public TextField passwordField;
    @FXML
    public TextField loginField;

    public Label labelNickname;
    private String nickname;
    private HashSet<String> clientsList;
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

    public void setAuthentificate(boolean authenticated) {
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
    }

    public void sendMessage(ActionEvent actionEvent) {
        String msg = textFieldInput.getText();
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
            System.out.println("CallOnAuthenticated");
            nickname = args[0].toString();
            setAuthentificate(true);
            System.out.println(nickname);
        });

        Network.setCallOnMsgReceived(args -> {
            String msg = args[0].toString();
            chatHistory.appendText(msg + "\n");
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
