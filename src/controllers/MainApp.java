package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application launcher. Loads MainView.fxml and displays the primary stage.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
        // Set a larger initial scene so the app shows all content by default
        double initialWidth = 1200;
        double initialHeight = 800;
        Scene scene = new Scene(root, initialWidth, initialHeight);
        primaryStage.setTitle("Digital Library");
        primaryStage.setScene(scene);
        // Optional: ensure the window is centered and visible
        primaryStage.centerOnScreen();
        // Optional: start not-maximized but large; comment out if you prefer maximized
        primaryStage.setMaximized(false);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
