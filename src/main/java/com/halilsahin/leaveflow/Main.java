package com.halilsahin.leaveflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.halilsahin.leaveflow.util.DatabaseHelper;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Veritabanı tablolarını oluştur
        DatabaseHelper.initializeDatabase();

        // Ana ekranı yükle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Çalışan İzin Modülü");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 