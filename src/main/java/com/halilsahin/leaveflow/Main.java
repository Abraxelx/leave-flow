package com.halilsahin.leaveflow;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.halilsahin.leaveflow.util.DatabaseHelper;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Splash screen göster
        showSplashScreen();
        
        // Veritabanı tablolarını oluştur
        DatabaseHelper.initializeDatabase();

        // Logo ikonunu yükle
        try {
            Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            try {
                // PNG yüklenemezse SVG'yi dene
                Image icon = new Image(getClass().getResourceAsStream("/logo.svg"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e2) {
                System.err.println("Logo yüklenemedi: " + e2.getMessage());
            }
        }

        // Ana ekranı yükle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("LeaveFlow - Çalışan İzin Yönetim Sistemi");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void showSplashScreen() {
        try {
            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/splash.fxml"));
            Scene splashScene = new Scene(loader.load());
            
            // Logo yükle
            ImageView logoView = (ImageView) splashScene.lookup("#splashLogo");
            if (logoView != null) {
                try {
                    Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
                    logoView.setImage(logo);
                } catch (Exception e) {
                    try {
                        // PNG yüklenemezse SVG'yi dene
                        Image logo = new Image(getClass().getResourceAsStream("/logo.svg"));
                        logoView.setImage(logo);
                    } catch (Exception e2) {
                        System.err.println("Splash logo yüklenemedi: " + e2.getMessage());
                    }
                }
            }
            
            splashStage.setScene(splashScene);
            splashStage.centerOnScreen();
            splashStage.show();
            
            // 2 saniye sonra splash screen'i kapat
            CompletableFuture.delayedExecutor(2000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .execute(() -> Platform.runLater(() -> splashStage.close()));
                
        } catch (Exception e) {
            System.err.println("Splash screen yüklenemedi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(new Locale("tr", "TR"));
        launch(args);
    }
} 