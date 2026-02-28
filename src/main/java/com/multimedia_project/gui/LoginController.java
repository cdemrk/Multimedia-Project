package com.multimedia_project.gui;

import com.multimedia_project.MainApp;
import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    private SystemState systemState;
    
    public void setSystemState(SystemState state) {
        this.systemState = state;
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        User loggedInUser = systemState.getUserManager().authenticate(username, password);
        
        if (loggedInUser != null) {
            System.out.println("Login successful for user: " + loggedInUser.getUsername());
            
            try {
                Stage currentStage = (Stage) usernameField.getScene().getWindow();
                currentStage.close();
                MainApp.showMainView(loggedInUser);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Could not load main application view.", Alert.AlertType.ERROR);
            }
            
        } else {
            showAlert("Login Failed", "Invalid username or password.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        
        if (usernameField.getScene() != null && usernameField.getScene().getWindow() != null) {
            alert.initOwner(usernameField.getScene().getWindow());
        }
        
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}