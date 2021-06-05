package org.sociopath.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class EnterNameDialogController implements Initializable {
    public TextField nameTextField;
    public Label errorMessageLabel;

    public static boolean stopAddStudent;
    public static String studentName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stopAddStudent = false;
        studentName = null;
    }

    public void confirmName(ActionEvent actionEvent) {
        if (nameTextField.getText().isEmpty()) {
            errorMessageLabel.setText("The fields can't be empty");
        } else if (nameTextField.getText().contains(" ")) {
            errorMessageLabel.setText("The fields can't contain white space");
        } else {
            errorMessageLabel.setText("");
            studentName = nameTextField.getText();
            exit(actionEvent);
        }
    }

    public void cancelName(ActionEvent actionEvent) {
        exit(actionEvent);
    }

    private void exit(ActionEvent event) {
        Stage enterStudentDetailsWindow = (Stage) ((Node) event.getSource()).getScene().getWindow();
        enterStudentDetailsWindow.close();
    }


}
