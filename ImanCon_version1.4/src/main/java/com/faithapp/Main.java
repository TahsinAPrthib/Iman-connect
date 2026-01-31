package com.faithapp;

import com.faithapp.database.DatabaseHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread: " + thread.getName(), throwable);
        });

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        primaryStage.setTitle("ImanConnect");
        primaryStage.setScene(new Scene(root));
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
        
        logger.info("Application started successfully in full screen mode");
    }
    
    @Override
    public void stop() {
        // Perform cleanup when the application is closing
        DatabaseHelper.shutdown();
        logger.info("Application shutdown completed");
    }

    public static void main(String[] args) {
        launch(args);
    }
} 