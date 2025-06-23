package com.halilsahin.leaveflow.ui;

import com.halilsahin.leaveflow.model.Employee;
import com.halilsahin.leaveflow.model.LeaveRecord;
import com.halilsahin.leaveflow.model.OfficialHoliday;
import com.halilsahin.leaveflow.repository.EmployeeRepository;
import com.halilsahin.leaveflow.repository.LeaveRecordRepository;
import com.halilsahin.leaveflow.repository.OfficialHolidayRepository;
import com.halilsahin.leaveflow.service.LeaveCalculator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AddLeaveController {
    @FXML private ComboBox<Employee> employeeCombo;
    @FXML private ComboBox<String> leaveTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label calculatedDaysLabel;
    @FXML private TextArea descriptionArea;

    private final EmployeeRepository employeeRepo = new EmployeeRepository();
    private final LeaveRecordRepository leaveRepo = new LeaveRecordRepository();
    private final OfficialHolidayRepository holidayRepo = new OfficialHolidayRepository();
    private final LeaveCalculator calculator = new LeaveCalculator();
    private List<OfficialHoliday> officialHolidays;

    @FXML
    public void initialize() {
        // Veritabanından verileri yükle
        loadEmployees();
        loadLeaveTypes();
        loadHolidays();

        // Tarih seçicileri yapılandır
        setupDatePickers();
        setupDatePickerLocale(startDatePicker);
        setupDatePickerLocale(endDatePicker);

        // Değişiklikleri dinle ve izin gününü yeniden hesapla
        startDatePicker.valueProperty().addListener((obs, old, aNew) -> recalculateLeaveDays());
        endDatePicker.valueProperty().addListener((obs, old, aNew) -> recalculateLeaveDays());
    }

    private void loadEmployees() {
        employeeCombo.getItems().setAll(employeeRepo.getAll());
        // ComboBox'ta çalışan isminin görünmesini sağla
        employeeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee employee) {
                return employee == null ? "" : employee.getName();
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });
    }

    private void loadLeaveTypes() {
        leaveTypeCombo.getItems().addAll("Yıllık İzin", "Hastalık Raporu", "Mazeret İzni", "Diğer");
    }

    private void loadHolidays() {
        this.officialHolidays = holidayRepo.getAll();
    }

    private void setupDatePickers() {
        // Geçmiş tarihleri devre dışı bırak
        LocalDate today = LocalDate.now();
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate start = startDatePicker.getValue();
                setDisable(empty || (start != null && date.isBefore(start)));
            }
        });
    }

    private void setupDatePickerLocale(DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(java.util.Locale.forLanguageTag("tr-TR"));
        datePicker.setConverter(new javafx.util.StringConverter<java.time.LocalDate>() {
            @Override
            public String toString(java.time.LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }
            @Override
            public java.time.LocalDate fromString(String string) {
                return (string == null || string.isEmpty()) ? null : java.time.LocalDate.parse(string, formatter);
            }
        });
    }

    private void recalculateLeaveDays() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start != null && end != null && !end.isBefore(start)) {
            String detailsJson = calculator.calculateLeaveDaysWithDetails(start, end, officialHolidays);
            try {
                com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(detailsJson);
                int days = rootNode.get("totalDays").asInt();
                calculatedDaysLabel.setText(days + " gün");
            } catch (Exception e) {
                e.printStackTrace();
                calculatedDaysLabel.setText("0 gün");
            }
        } else {
            calculatedDaysLabel.setText("0 gün");
        }
    }

    @FXML
    private void onSave() {
        // Gerekli alanların kontrolü
        Employee selectedEmployee = employeeCombo.getValue();
        String leaveType = leaveTypeCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (selectedEmployee == null || leaveType == null || startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Eksik Bilgi", "Lütfen tüm zorunlu alanları doldurun.");
            return;
        }

        // İzin süresini hesapla
        String dayDetails = calculator.calculateLeaveDaysWithDetails(startDate, endDate, officialHolidays);
        int leaveDays = 0;
        try {
            com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(dayDetails);
            leaveDays = rootNode.get("totalDays").asInt();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hesaplama Hatası", "İzin günleri hesaplanırken bir hata oluştu.");
            return;
        }

        if (leaveDays <= 0) {
            showAlert(Alert.AlertType.WARNING, "Geçersiz Süre", "İzin süresi 0 günden fazla olmalıdır.");
            return;
        }

        // Yıllık izin kontrolü
        if (leaveType.equals("Yıllık İzin")) {
            int remainingLeave = getRemainingAnnualLeave(selectedEmployee);
            if (leaveDays > remainingLeave) {
                showAlert(Alert.AlertType.ERROR, "Yetersiz Bakiye", "Çalışanın kalan yıllık izin hakkı (" + remainingLeave + " gün) bu izin için yetersiz.");
                return;
            }
        }
        
        // Veritabanına kaydet
        LeaveRecord newRecord = new LeaveRecord(0, selectedEmployee.getId(), leaveType, startDate, endDate,
            descriptionArea.getText(), leaveDays, dayDetails, null); // Kalan izin sonradan hesaplanacak
        leaveRepo.add(newRecord);
        
        showAlert(Alert.AlertType.INFORMATION, "Başarılı", "İzin kaydı başarıyla oluşturuldu.");
        closeWindow();
    }

    private int getRemainingAnnualLeave(Employee employee) {
        int totalUsed = leaveRepo.getByEmployeeId(employee.getId()).stream()
                .filter(record -> "Yıllık İzin".equals(record.getLeaveType()))
                .mapToInt(LeaveRecord::getCalculatedDays)
                .sum();
        return employee.getAnnualLeaveDays() - totalUsed;
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) employeeCombo.getScene().getWindow();
        stage.close();
    }
} 