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
    
    private static SystemState systemState; 
    private static Stage primaryStage;

    @Override
    public void init() throws Exception {
        super.init();
        systemState = new SystemState(); 
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        stage.setTitle("MediaLab Document Management - Login");
        
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                MainApp.class.getResourceAsStream("/com/multimedia_project/icons/app_icon.png")
            );
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("No icon found");
        }

        stage.setOnCloseRequest(e -> {
            if (systemState != null) {
                systemState.saveSystemState();
            }
            Platform.exit();
        });

        showLoginView();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (systemState != null) {
            systemState.saveSystemState();
        }
    }

    public static void showLoginView() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/multimedia_project/fxml/login_view.fxml"));
        Parent root = loader.load();
        
        LoginController controller = loader.getController();
        controller.setSystemState(systemState);

        primaryStage.setTitle("MediaLab Documents - Login");

        primaryStage.setMaximized(false);
        
        primaryStage.setWidth(400);
        primaryStage.setHeight(350);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(350);

        Scene scene = new Scene(root, 400, 350);
        primaryStage.setScene(scene);
        
        primaryStage.centerOnScreen();
        
        primaryStage.show();
    }

    public static void showMainView(User loggedInUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/multimedia_project/fxml/main_view.fxml")
        );
        Parent root = loader.load();
        
        MainController controller = loader.getController();
        controller.initializeData(getSystemState(), loggedInUser);
        
        primaryStage.setTitle("MediaLab Documents - Welcome " + loggedInUser.getFirstName());
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.setMaximized(true); 
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