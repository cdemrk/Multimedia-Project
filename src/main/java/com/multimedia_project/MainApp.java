package com.multimedia_project;

import com.multimedia_project.gui.LoginController;
import com.multimedia_project.gui.MainController;
import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    
    private static SystemState systemState; // Κεντρική αναφορά στην κατάσταση του συστήματος
    private static Stage primaryStage;

    @Override
    public void init() throws Exception {
        super.init();
        // Αρχικοποίηση SystemState
        systemState = new SystemState(); 
        
        // Φόρτωση Κατάστασης από τα JSON
        // systemState.loadSystemState();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        stage.setTitle("MediaLab Document Management - Login");
        
        // ΠΡΟΣΘΗΚΗ ΕΙΚΟΝΙΔΙΟΥ ΕΦΑΡΜΟΓΗΣ
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                MainApp.class.getResourceAsStream("/com/multimedia_project/icons/app_icon.png")
            );
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("No icon found in path: /com/multimedia_project/icons/app_icon.png");
        }

        stage.setOnCloseRequest(e -> {
            Platform.exit();
        });

        // Φόρτωση της αρχικής Login View
        showLoginView();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("Application is stopping and saving state...");
        
        // Αποθήκευση της κατάστασης του συστήματος
        // Καλείται πάντα, ανεξάρτητα από τον τρόπο τερματισμού
        if (systemState != null) {
            systemState.saveSystemState();
        }
    }

    public static void showLoginView() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/multimedia_project/fxml/login_view.fxml"));
        Parent root = loader.load();
        
        // Πρέπει να περάσουμε το systemState στον Controller
        LoginController controller = loader.getController();
        controller.setSystemState(systemState);

        primaryStage.setTitle("Login");
        
        // ΕΠΑΝΑΦΟΡΑ ΜΕΓΕΘΟΥΣ ΠΑΡΑΘΥΡΟΥ
        primaryStage.setMaximized(false);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        primaryStage.setWidth(400);
        primaryStage.setHeight(350);

        // Δημιουργία της Σκηνής (Scene)
        Scene scene = new Scene(root, 400, 350);
        
        primaryStage.setScene(scene);
        
        // Κεντράρισμα του παραθύρου στην οθόνη
        primaryStage.centerOnScreen();
        
        primaryStage.show();
    }

    // Φόρτωση του Κεντρικού Παραθύρου μετά την επιτυχή σύνδεση
    public static void showMainView(User loggedInUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/multimedia_project/fxml/main_view.fxml")
        );
        Parent root = loader.load();
        
        MainController controller = loader.getController();
        controller.initializeData(getSystemState(), loggedInUser);
        
        primaryStage.setTitle("MediaLab Documents - Welcome " + loggedInUser.getFirstName());
        
        // Δημιουργούμε το Scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // ΑΥΤΟ ΕΙΝΑΙ ΤΟ ΚΛΕΙΔΙ: Μεγιστοποίηση παραθύρου
        primaryStage.setMaximized(true); 
        
        // Αν θέλεις να μην μπορεί ο χρήστης να το μικρύνει κάτω από ένα όριο:
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        primaryStage.show();
    }

    public static SystemState getSystemState() {
        return systemState;
    }

    public static void main(String[] args) {
        launch(args);
    }
}