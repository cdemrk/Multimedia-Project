package com.multimedia_project;

/**
 * Hello world!
 *
 */
public class MainApp 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}




package com.medialab.project;

import com.medialab.project.managers.SystemState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    
    // Κεντρική αναφορά στην κατάσταση του συστήματος
    private SystemState systemState;
    private static Stage primaryStage;

    @Override
    public void init() throws Exception {
        super.init();
        // 1. Αρχικοποίηση SystemState
        // Αυτόματα καλείται ο DataManager.loadState()
        this.systemState = new SystemState();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // 2. Ρύθμιση τίτλου και χειριστή τερματισμού
        stage.setTitle("MediaLab Document Management - Login");
        
        // Καθορίζουμε τι θα γίνει όταν ο χρήστης κλείσει το παράθυρο με το 'X'
        stage.setOnCloseRequest(e -> {
            // Καλείται η μέθοδος τερματισμού
            shutdownApplication();
        });

        // 3. Φόρτωση της Login View
        showLoginView();
    }

    public static void showLoginView() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/medialab/project/fxml/LoginView.fxml")
        );
        Parent root = loader.load();
        
        // Πρέπει να περάσουμε το systemState στον Controller
        LoginController controller = loader.getController();
        controller.setSystemState(getSystemState()); 
        
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }
    
    // [ΝΕΑ ΜΕΘΟΔΟΣ] Φόρτωση του Κεντρικού Παραθύρου μετά την επιτυχή σύνδεση
    public static void showMainView(User loggedInUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/medialab/project/fxml/MainView.fxml")
        );
        Parent root = loader.load();
        
        MainController controller = loader.getController();
        controller.initializeData(getSystemState(), loggedInUser);
        
        primaryStage.setTitle("MediaLab Documents - Welcome " + loggedInUser.getFirstName());
        primaryStage.setScene(new Scene(root, 1000, 700)); // Μεγαλύτερες διαστάσεις για το κεντρικό παράθυρο
        primaryStage.show();
    }


    // 4. Λογική Τερματισμού (Καλείται από το setOnCloseRequest)
    private void shutdownApplication() {
        System.out.println("Application is shutting down...");
        // Καλούμε την αποθήκευση της κατάστασης πριν τον τερματισμό!
        systemState.saveSystemState(); 
        
        // Επιβεβαιώνουμε ότι η εφαρμογή τερματίζεται
        Platform.exit();
        System.exit(0);
    }
    
    public static SystemState getSystemState() {
        // Αυτή η μέθοδος χρειάζεται να υλοποιηθεί σωστά αν καλείται στατικά
        // Για την απλότητα, θα κάνουμε το systemState προσβάσιμο
        // (Στην πράξη, θα το περνούσαμε ως παράμετρο παντού)
        return ((MainApp) Application.getInstance()).systemState;
    }


    public static void main(String[] args) {
        launch(args);
    }
}