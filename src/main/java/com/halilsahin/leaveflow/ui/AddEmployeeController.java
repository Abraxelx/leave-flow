package com.halilsahin.leaveflow.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.halilsahin.leaveflow.model.Employee;
import com.halilsahin.leaveflow.repository.EmployeeRepository;

import java.time.LocalDate;

public class AddEmployeeController {
    @FXML private TextField nameField;
    @FXML private DatePicker hireDatePicker;
    @FXML private Label serviceYearsLabel;
    @FXML private Label minLeaveLabel;
    @FXML private TextField annualLeaveField;

    private final EmployeeRepository employeeRepo = new EmployeeRepository();

    @FXML
    public void initialize() {
        // İşe giriş tarihi değiştiğinde otomatik hesaplama yap
        hireDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateCalculations();
        });
        
        // Varsayılan olarak bugünün tarihini seç
        hireDatePicker.setValue(LocalDate.now());
    }

    private void updateCalculations() {
        LocalDate hireDate = hireDatePicker.getValue();
        if (hireDate != null) {
            Employee tempEmployee = new Employee(0, "", hireDate, 0);
            int serviceYears = tempEmployee.getServiceYears();
            int minLeave = tempEmployee.calculateMinimumAnnualLeave();
            
            serviceYearsLabel.setText(serviceYears + " yıl");
            minLeaveLabel.setText(minLeave + " gün");
            
            // Yıllık izin alanını minimum değerle doldur
            annualLeaveField.setText(String.valueOf(minLeave));
        }
    }

    @FXML
    private void onSave() {
        String name = nameField.getText().trim();
        LocalDate hireDate = hireDatePicker.getValue();
        String annualLeaveStr = annualLeaveField.getText().trim();

        if (name.isEmpty() || hireDate == null || annualLeaveStr.isEmpty()) {
            showAlert("Lütfen tüm alanları doldurun.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int annualLeave = Integer.parseInt(annualLeaveStr);
            if (annualLeave <= 0) {
                showAlert("Yıllık izin hakkı 0'dan büyük olmalıdır.", Alert.AlertType.WARNING);
                return;
            }

            // Minimum izin kontrolü
            Employee tempEmployee = new Employee(0, name, hireDate, annualLeave);
            int minRequired = tempEmployee.calculateMinimumAnnualLeave();
            if (annualLeave < minRequired) {
                showAlert("Yıllık izin hakkı minimum " + minRequired + " gün olmalıdır.", Alert.AlertType.WARNING);
                return;
            }

            Employee employee = new Employee(0, name, hireDate, annualLeave);
            employeeRepo.add(employee);

            showAlert("Çalışan başarıyla eklendi.", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Yıllık izin hakkı geçerli bir sayı olmalıdır.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Hata: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
} 