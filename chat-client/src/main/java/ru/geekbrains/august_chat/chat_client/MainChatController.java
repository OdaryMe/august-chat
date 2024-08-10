package ru.geekbrains.august_chat.chat_client;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainChatController implements Initializable {
    @FXML
    public VBox mainChatPanel;

    @FXML
    public TextArea mainChatArea;

    @FXML
    public ListView contactList;

    @FXML
    public TextField inputField;

    public void connectToServer(ActionEvent actionEvent) {
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void mockAction(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(1);
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void showAbout(ActionEvent actionEvent) {
    }

    public void sendMessage(ActionEvent actionEvent) {
        var message = inputField.getText();
        String prefix = "All: ";
        var selectedItem = contactList.getSelectionModel().getSelectedItem();

        if(message.isBlank()) {
            return;
        }
        if(selectedItem != null) {
            prefix = "to " + selectedItem + ": ";
        }
        mainChatArea.appendText(prefix + 0 + System.lineSeparator());
        inputField.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var contacts = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            contacts.add("Contact#" + (i + 1));
        }
        contactList.setItems(FXCollections.observableList(contacts));
    }
}
