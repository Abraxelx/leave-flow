package com.halilsahin.leaveflow.ui;

import com.halilsahin.leaveflow.model.Employee;
import com.halilsahin.leaveflow.model.LeaveRecord;
import com.halilsahin.leaveflow.model.OfficialHoliday;
import com.halilsahin.leaveflow.repository.EmployeeRepository;
import com.halilsahin.leaveflow.repository.LeaveRecordRepository;
import com.halilsahin.leaveflow.repository.OfficialHolidayRepository;
import com.halilsahin.leaveflow.service.LeaveCalculator;
import com.halilsahin.leaveflow.service.PdfExportService;
import com.halilsahin.leaveflow.util.HolidayFetcher;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {
    
    // Ana Tab Pane
    @FXML private TabPane mainTabPane;
    @FXML private Button quickAddButton;
    
    // Çalışanlar Tab
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> hireDateColumn;
    @FXML private TableColumn<Employee, Integer> colServiceYears;
    @FXML private TableColumn<Employee, String> totalLeaveColumn;
    @FXML private TableColumn<Employee, String> remainingLeaveColumn;
    @FXML private TableColumn<Employee, Void> actionsColumn;
    
    // İzinler Tab
    @FXML private ComboBox<Employee> employeeFilterCombo;
    @FXML private DatePicker startDateFilter;
    @FXML private DatePicker endDateFilter;
    @FXML private TableView<LeaveRecord> leaveTable;
    @FXML private TableColumn<LeaveRecord, Integer> colLeaveId;
    @FXML private TableColumn<LeaveRecord, String> colLeaveEmployee;
    @FXML private TableColumn<LeaveRecord, String> colLeaveType;
    @FXML private TableColumn<LeaveRecord, String> colLeaveStart;
    @FXML private TableColumn<LeaveRecord, String> colLeaveEnd;
    @FXML private TableColumn<LeaveRecord, String> colLeaveDuration;
    @FXML private TableColumn<LeaveRecord, String> colLeaveRemaining;
    @FXML private TableColumn<LeaveRecord, Void> colLeaveActions;
    @FXML private Label totalDaysLabel;
    @FXML private Label avgDaysLabel;
    @FXML private Label totalRecordsLabel;
    
    // Ayarlar Tab
    @FXML private TextField holidayYearField;
    @FXML private CheckBox includeDetailsCheckBox;
    @FXML private CheckBox includeStatsCheckBox;
    
    // Repositories
    private final EmployeeRepository employeeRepository = new EmployeeRepository();
    private final LeaveRecordRepository leaveRecordRepository = new LeaveRecordRepository();
    private final OfficialHolidayRepository holidayRepository = new OfficialHolidayRepository();
    private final LeaveCalculator leaveCalculator = new LeaveCalculator();
    private final PdfExportService pdfExportService = new PdfExportService();
    
    // Data
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private final ObservableList<LeaveRecord> leaveList = FXCollections.observableArrayList();
    private final ObservableList<LeaveRecord> filteredLeaveList = FXCollections.observableArrayList();
    private List<LocalDate> officialHolidays;
    private Map<Integer, Employee> employeeMap;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    @FXML
    public void initialize() {
        loadData();
        setupEmployeeTable();
        setupLeaveTable();
        setupFilters();
        setupSettings();
        
        // Tab değişikliklerini dinle
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().contains("İzinler")) {
                loadLeaves();
                updateStatistics();
            }
        });
    }
    
    private void loadData() {
        // Resmi tatilleri yükle
        this.officialHolidays = holidayRepository.getAll().stream()
                .map(OfficialHoliday::getDate)
                .collect(Collectors.toList());
        
        // Çalışanları yükle
        employeeList.setAll(employeeRepository.getAll());
        employeeMap = employeeList.stream().collect(Collectors.toMap(Employee::getId, e -> e));
        
        // İzinleri yükle
        loadLeaves();
    }
    
    private void setupEmployeeTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // İşe giriş tarihi
        hireDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHireDate().format(dateFormatter)));
        
        // Hizmet süresi
        colServiceYears.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Employee employee = getTableRow().getItem();
                    setText(employee.getServiceYears() + " yıl");
                }
            }
        });
        
        // Yıllık izin hakkı
        totalLeaveColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getAnnualLeaveDays())));
        
        // Kalan izin hesaplama
        remainingLeaveColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(calculateRemainingLeave(cellData.getValue()))));

        // İşlem butonları
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
                editButton.setTooltip(new Tooltip("Çalışanı Düzenle"));
                editButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleEditEmployee(employee);
                });
                
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
                deleteButton.setTooltip(new Tooltip("Çalışanı Sil"));
                deleteButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleDeleteEmployee(employee);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.setStyle("-fx-alignment: center;");
                    buttons.getChildren().addAll(editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        employeeTable.setItems(employeeList);
    }
    
    private void setupLeaveTable() {
        colLeaveId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLeaveEmployee.setCellValueFactory(cellData -> {
            Employee employee = employeeMap.get(cellData.getValue().getEmployeeId());
            return new SimpleStringProperty(employee != null ? employee.getName() : "Bilinmeyen");
        });
        colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveType"));
        colLeaveStart.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStartDate().format(dateFormatter)));
        colLeaveEnd.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEndDate().format(dateFormatter)));
        colLeaveDuration.setCellValueFactory(cellData -> {
            LeaveRecord record = cellData.getValue();
            int days = leaveCalculator.calculateLeaveDays(record.getStartDate(), record.getEndDate(), officialHolidays);
            return new SimpleStringProperty(days + " gün");
        });
        colLeaveRemaining.setCellValueFactory(cellData -> {
            LeaveRecord record = cellData.getValue();
            Employee employee = employeeMap.get(record.getEmployeeId());
            if (employee != null) {
                int remainingLeave = calculateRemainingLeave(employee);
                return new SimpleStringProperty(remainingLeave + " gün");
            } else {
                return new SimpleStringProperty("Bilinmeyen");
            }
        });

        // İşlem butonları
        colLeaveActions.setCellFactory(column -> new TableCell<>() {
            private final Button detailButton = new Button("👁️");
            private final Button deleteButton = new Button("🗑️");

            {
                detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
                detailButton.setTooltip(new Tooltip("İzin Detayları"));
                detailButton.setOnAction(event -> {
                    LeaveRecord record = getTableView().getItems().get(getIndex());
                    handleShowLeaveDetails(record);
                });
                
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand;");
                deleteButton.setTooltip(new Tooltip("İzin Kaydını Sil"));
                deleteButton.setOnAction(event -> {
                    LeaveRecord record = getTableView().getItems().get(getIndex());
                    handleDeleteLeave(record);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.setStyle("-fx-alignment: center;");
                    buttons.getChildren().addAll(detailButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        leaveTable.setItems(filteredLeaveList);
    }
    
    private void setupFilters() {
        // Çalışan filtresi
        employeeFilterCombo.getItems().addAll(employeeRepository.getAll());
        employeeFilterCombo.getItems().add(0, null);
        employeeFilterCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee employee) {
                return employee == null ? "Tüm Çalışanlar" : employee.getName();
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });
    }
    
    private void setupSettings() {
        holidayYearField.setText(String.valueOf(LocalDate.now().getYear()));
        includeDetailsCheckBox.setSelected(true);
        includeStatsCheckBox.setSelected(true);
    }
    
    private void loadLeaves() {
        leaveList.setAll(leaveRecordRepository.getAll());
        applyFilters();
    }
    
    private void applyFilters() {
        List<LeaveRecord> filtered = leaveList.stream()
                .filter(this::matchesEmployeeFilter)
                .filter(this::matchesDateFilter)
                .collect(Collectors.toList());
        
        filteredLeaveList.setAll(filtered);
    }
    
    private boolean matchesEmployeeFilter(LeaveRecord record) {
        Employee selectedEmployee = employeeFilterCombo.getValue();
        return selectedEmployee == null || record.getEmployeeId() == selectedEmployee.getId();
    }
    
    private boolean matchesDateFilter(LeaveRecord record) {
        LocalDate startFilter = startDateFilter.getValue();
        LocalDate endFilter = endDateFilter.getValue();
        
        if (startFilter != null && record.getStartDate().isBefore(startFilter)) {
            return false;
        }
        if (endFilter != null && record.getEndDate().isAfter(endFilter)) {
            return false;
        }
        return true;
    }
    
    private void updateStatistics() {
        if (filteredLeaveList.isEmpty()) {
            totalDaysLabel.setText("0");
            avgDaysLabel.setText("0 gün");
            totalRecordsLabel.setText("0");
            return;
        }

        int totalDays = filteredLeaveList.stream()
                .mapToInt(record -> leaveCalculator.calculateLeaveDays(
                    record.getStartDate(), record.getEndDate(), officialHolidays))
                .sum();

        double avgDays = (double) totalDays / filteredLeaveList.size();

        totalDaysLabel.setText(String.valueOf(totalDays));
        avgDaysLabel.setText(String.format("%.1f gün", avgDays));
        totalRecordsLabel.setText(String.valueOf(filteredLeaveList.size()));
    }
    
    private int calculateRemainingLeave(Employee employee) {
        int totalUsed = leaveRecordRepository.getByEmployeeId(employee.getId()).stream()
                .filter(record -> "Yıllık İzin".equals(record.getLeaveType()))
                .mapToInt(record -> leaveCalculator.calculateLeaveDays(record.getStartDate(), record.getEndDate(), officialHolidays))
                .sum();
        return employee.getAnnualLeaveDays() - totalUsed;
    }
    
    // Event Handlers
    @FXML
    private void onQuickAddLeave() {
        openQuickAddDialog();
    }
    
    @FXML
    private void onAddEmployee() {
        openWindow("/AddEmployeeView.fxml", "Yeni Çalışan Ekle", this::loadData);
    }
    
    @FXML
    private void onFilterLeaves() {
        applyFilters();
        updateStatistics();
    }
    
    @FXML
    private void onClearFilters() {
        employeeFilterCombo.setValue(null);
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        applyFilters();
        updateStatistics();
    }
    
    @FXML
    private void onExportLeaves() {
        exportLeaveReport();
    }
    
    @FXML
    private void onExportGeneralReport() {
        exportGeneralReport();
    }
    
    @FXML
    private void onExportEmployeeReport() {
        exportEmployeeReport();
    }
    
    @FXML
    private void onExportMonthlyReport() {
        exportMonthlyReport();
    }
    
    @FXML
    private void onExportStatsReport() {
        exportStatsReport();
    }
    
    @FXML
    private void onUpdateHolidays() {
        updateHolidays();
    }
    
    @FXML
    private void onBackupDatabase() {
        backupDatabase();
    }
    
    @FXML
    private void onRestoreDatabase() {
        restoreDatabase();
    }
    
    @FXML
    private void onResetDatabase() {
        resetDatabase();
    }
    
    // Helper Methods
    private void openQuickAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuickAddLeaveView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Hızlı İzin Ekleme");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setOnHidden(e -> {
                loadData();
                loadLeaves();
                updateStatistics();
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Hata: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void openWindow(String fxmlPath, String title, Runnable onHiddenAction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setOnHidden(e -> onHiddenAction.run());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Hata: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void handleEditEmployee(Employee employee) {
        showAlert("Çalışan düzenleme özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
    }
    
    private void handleDeleteEmployee(Employee employee) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Çalışan Sil");
        alert.setHeaderText("Çalışanı silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            employeeRepository.delete(employee.getId());
            loadData();
            showAlert("Çalışan başarıyla silindi.", Alert.AlertType.INFORMATION);
        }
    }
    
    private void handleShowLeaveDetails(LeaveRecord record) {
        Employee employee = employeeMap.get(record.getEmployeeId());
        if (employee == null) {
            showAlert("Çalışan bilgisi bulunamadı.", Alert.AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("İzin Detay Raporu Export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(employee.getName() + "_izin_detay.pdf");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                pdfExportService.exportLeaveDetails(record, employee, file.getAbsolutePath());
                showAlert("İzin detay raporu başarıyla dışa aktarıldı.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("PDF oluşturulurken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    
    private void handleDeleteLeave(LeaveRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("İzin Kaydı Sil");
        alert.setHeaderText("Bu izin kaydını silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            leaveRecordRepository.delete(record.getId());
            loadLeaves();
            updateStatistics();
            showAlert("İzin kaydı başarıyla silindi.", Alert.AlertType.INFORMATION);
        }
    }
    
    private void exportLeaveReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("İzin Raporu Export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("izin_raporu.pdf");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                pdfExportService.exportLeaveReport(filteredLeaveList, employeeMap, file.getAbsolutePath());
                showAlert("İzin raporu başarıyla dışa aktarıldı.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("PDF oluşturulurken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    
    private void exportGeneralReport() {
        showAlert("Genel rapor özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
    }
    
    private void exportEmployeeReport() {
        showAlert("Çalışan raporu özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
    }
    
    private void exportMonthlyReport() {
        showAlert("Aylık rapor özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
    }
    
    private void exportStatsReport() {
        showAlert("İstatistik raporu özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
    }
    
    private void updateHolidays() {
        String yearStr = holidayYearField.getText().trim();
        if (yearStr.isEmpty()) {
            showAlert("Yıl alanı zorunludur!", Alert.AlertType.WARNING);
            return;
        }
        try {
            int year = Integer.parseInt(yearStr);
            HolidayFetcher fetcher = new HolidayFetcher();
            List<OfficialHoliday> holidays = fetcher.fetchHolidays(year);
            fetcher.saveToJson(holidays, year);
            fetcher.saveToDatabase(holidays);
            loadData();
            showAlert("Tatil günleri başarıyla güncellendi.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Tatiller alınamadı: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void backupDatabase() {
        showAlert("Veritabanı yedekleme özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
    }
    
    private void restoreDatabase() {
        showAlert("Veritabanı geri yükleme özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
    }
    
    private void resetDatabase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Veritabanı Sıfırla");
        alert.setHeaderText("Tüm verileri silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showAlert("Veritabanı sıfırlama özelliği yakında eklenecek.", Alert.AlertType.INFORMATION);
        }
    }
    
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }
} 