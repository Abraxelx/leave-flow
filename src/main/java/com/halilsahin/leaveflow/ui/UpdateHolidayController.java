package com.halilsahin.leaveflow.ui;

import com.halilsahin.leaveflow.model.OfficialHoliday;
import com.halilsahin.leaveflow.util.HolidayFetcher;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class UpdateHolidayController {
    @FXML private TextField yearField;

    @FXML
    private void onUpdate() {
        String yearStr = yearField.getText().trim();
        if (yearStr.isEmpty()) {
            showAlert("Yıl alanı zorunludur!", Alert.AlertType.WARNING);
            return;
        }
        try {
            int year = Integer.parseInt(yearStr);
            HolidayFetcher fetcher = new HolidayFetcher();

            // API'den tatilleri çek
            List<OfficialHoliday> holidays = fetcher.fetchHolidays(year);

            // JSON'a ve veritabanına kaydet
            fetcher.saveToJson(holidays, year);
            fetcher.saveToDatabase(holidays);

            showAlert("Tatil günleri başarıyla güncellendi.", Alert.AlertType.INFORMATION);
            closeWindow(); // Başarılı olunca pencereyi kapat
        } catch (NumberFormatException e) {
            showAlert("Yıl geçerli bir sayı olmalıdır.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Tatiller alınamadı. 'config.properties' dosyasındaki API anahtarını veya internet bağlantınızı kontrol edin.\nHata: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) yearField.getScene().getWindow();
        stage.close();
    }
} 