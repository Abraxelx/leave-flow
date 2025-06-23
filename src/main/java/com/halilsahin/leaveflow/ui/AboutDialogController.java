package com.halilsahin.leaveflow.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AboutDialogController {
    @FXML private ImageView aboutLogo;
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            aboutLogo.setImage(logo);
        } catch (Exception e) {
            aboutLogo.setVisible(false);
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
} 