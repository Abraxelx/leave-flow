package com.halilsahin.leaveflow.ui;

import com.halilsahin.leaveflow.model.Employee;
import com.halilsahin.leaveflow.repository.EmployeeRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EditEmployeeController {
    @FXML private TextField nameField;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField annualLeaveField;

    private Employee employee;
    private final EmployeeRepository employeeRepo = new EmployeeRepository();

    public void setEmployee(Employee employee) {
        this.employee = employee;
        nameField.setText(employee.getName());
        hireDatePicker.setValue(employee.getHireDate());
        annualLeaveField.setText(String.valueOf(employee.getAnnualLeaveDays()));
    }

    @FXML
    private void onSave() {
        String name = nameField.getText().trim();
        LocalDate hireDate = hireDatePicker.getValue();
        String annualLeaveStr = annualLeaveField.getText().trim();

        if (name.isEmpty() || hireDate == null || annualLeaveStr.isEmpty()) {
            showAlert("Tüm alanları doldurun.", Alert.AlertType.WARNING);
            return;
        }
        int annualLeave;
        try {
            annualLeave = Integer.parseInt(annualLeaveStr);
        } catch (NumberFormatException e) {
            showAlert("Yıllık izin hakkı sayı olmalı.", Alert.AlertType.ERROR);
            return;
        }

        employee.setName(name);
        employee.setHireDate(hireDate);
        employee.setAnnualLeaveDays(annualLeave);
        employeeRepo.update(employee);
        showAlert("Çalışan bilgileri güncellendi.", Alert.AlertType.INFORMATION);
        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
} 