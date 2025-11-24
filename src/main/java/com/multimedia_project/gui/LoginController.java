package com.multimedia_project.gui;

public class LoginController {
    
}






package com.medialab.project.gui;

import com.medialab.project.MainApp;
import com.medialab.project.managers.SystemState;
import com.medialab.project.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    private SystemState systemState;
    
    // Χρησιμοποιείται από την MainApp για να περάσει την κατάσταση
    public void setSystemState(SystemState state) {
        this.systemState = state;
    }
    
    @FXML
    private void handleLoginButton() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // 1. Έλεγχος Αυθεντικοποίησης
        Optional<User> userOpt = systemState.getUserManager().authenticate(username, password);
        
        if (userOpt.isPresent()) {
            User loggedInUser = userOpt.get();
            System.out.println("Login successful for user: " + loggedInUser.getUsername());
            
            try {
                // 2. Φόρτωση Κεντρικού Παραθύρου
                MainApp.showMainView(loggedInUser);
                
                // 3. Εμφάνιση ειδοποίησης για παρακολουθήσεις (λογική που θα προστεθεί στον MainController)
                // Εδώ θα καλούσατε τον FollowManager για να ελέγξει αν υπάρχουν νέα versions
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Could not load main application view.", Alert.AlertType.ERROR);
            }
            
        } else {
            // Αποτυχία σύνδεσης
            showAlert("Login Failed", "Invalid username or password.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}