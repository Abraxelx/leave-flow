package com.halilsahin.leaveflow.ui;

import com.halilsahin.leaveflow.model.Employee;
import com.halilsahin.leaveflow.model.LeaveRecord;
import com.halilsahin.leaveflow.model.OfficialHoliday;
import com.halilsahin.leaveflow.repository.EmployeeRepository;
import com.halilsahin.leaveflow.repository.LeaveRecordRepository;
import com.halilsahin.leaveflow.repository.OfficialHolidayRepository;
import com.halilsahin.leaveflow.service.LeaveCalculator;
import com.halilsahin.leaveflow.service.PdfExportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeaveReportController {
    @FXML private ComboBox<Employee> employeeFilterCombo;
    @FXML private ComboBox<String> leaveTypeFilterCombo;
    @FXML private DatePicker startDateFilter;
    @FXML private DatePicker endDateFilter;
    
    @FXML private Label totalDaysLabel;
    @FXML private Label avgDaysLabel;
    @FXML private Label topEmployeeLabel;
    @FXML private Label totalRecordsLabel;
    
    @FXML private TableView<LeaveRecord> leaveTable;
    @FXML private TableColumn<LeaveRecord, Integer> colId;
    @FXML private TableColumn<LeaveRecord, String> colEmployee;
    @FXML private TableColumn<LeaveRecord, String> colLeaveType;
    @FXML private TableColumn<LeaveRecord, String> colStartDate;
    @FXML private TableColumn<LeaveRecord, String> colEndDate;
    @FXML private TableColumn<LeaveRecord, String> colDuration;
    @FXML private TableColumn<LeaveRecord, String> colRemainingLeave;
    @FXML private TableColumn<LeaveRecord, String> colDescription;
    @FXML private TableColumn<LeaveRecord, Void> colActions;

    private final EmployeeRepository employeeRepository = new EmployeeRepository();
    private final LeaveRecordRepository leaveRecordRepository = new LeaveRecordRepository();
    private final OfficialHolidayRepository holidayRepository = new OfficialHolidayRepository();
    private final LeaveCalculator leaveCalculator = new LeaveCalculator();
    private final PdfExportService pdfExportService = new PdfExportService();
    
    private final ObservableList<LeaveRecord> leaveList = FXCollections.observableArrayList();
    private final ObservableList<LeaveRecord> filteredList = FXCollections.observableArrayList();
    private List<LocalDate> officialHolidays;
    private Map<Integer, Employee> employeeMap;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        loadData();
        setupFilters();
        setupTable();
        loadAllLeaves();
        updateStatistics();
    }

    private void loadData() {
        // Çalışanları yükle
        List<Employee> employees = employeeRepository.getAll();
        employeeMap = employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
        
        // Resmi tatilleri yükle
        officialHolidays = holidayRepository.getAll().stream()
                .map(OfficialHoliday::getDate)
                .collect(Collectors.toList());
    }

    private void setupFilters() {
        // Çalışan filtresi
        employeeFilterCombo.getItems().addAll(employeeRepository.getAll());
        employeeFilterCombo.getItems().add(0, null); // "Tüm Çalışanlar" seçeneği
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

        // İzin türü filtresi
        leaveTypeFilterCombo.getItems().addAll("Tüm İzin Türleri", "Yıllık İzin", "Hastalık Raporu", "Mazeret İzni", "Diğer");
        leaveTypeFilterCombo.setValue("Tüm İzin Türleri");
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployee.setCellValueFactory(cellData -> {
            Employee employee = employeeMap.get(cellData.getValue().getEmployeeId());
            return new SimpleStringProperty(employee != null ? employee.getName() : "Bilinmeyen");
        });
        colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveType"));
        colStartDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStartDate().format(dateFormatter)));
        colEndDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEndDate().format(dateFormatter)));
        colDuration.setCellValueFactory(cellData -> {
            LeaveRecord record = cellData.getValue();
            int days = leaveCalculator.calculateLeaveDays(record.getStartDate(), record.getEndDate(), officialHolidays);
            return new SimpleStringProperty(days + " gün");
        });
        colRemainingLeave.setCellValueFactory(cellData -> {
            LeaveRecord record = cellData.getValue();
            Employee employee = employeeMap.get(record.getEmployeeId());
            if (employee != null) {
                int remainingLeave = calculateRemainingLeave(employee);
                return new SimpleStringProperty(remainingLeave + " gün");
            } else {
                return new SimpleStringProperty("Bilinmeyen");
            }
        });
        colDescription.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription() != null ? 
                cellData.getValue().getDescription() : ""));

        // İşlem butonları
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button detailButton = new Button("Detay");
            private final Button deleteButton = new Button("Sil");

            {
                detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                detailButton.setTooltip(new Tooltip("İzin Detaylarını Görüntüle"));
                detailButton.setOnAction(event -> {
                    LeaveRecord record = getTableView().getItems().get(getIndex());
                    handleShowDetails(record);
                });
                
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                deleteButton.setTooltip(new Tooltip("İzin Kaydını Sil"));
                deleteButton.setOnAction(event -> {
                    LeaveRecord record = getTableView().getItems().get(getIndex());
                    handleDelete(record);
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

        leaveTable.setItems(filteredList);
    }

    private void loadAllLeaves() {
        leaveList.setAll(leaveRecordRepository.getAll());
        applyFilters();
    }

    @FXML
    private void onFilter() {
        applyFilters();
        updateStatistics();
    }

    @FXML
    private void onClearFilters() {
        employeeFilterCombo.setValue(null);
        leaveTypeFilterCombo.setValue("Tüm İzin Türleri");
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        applyFilters();
        updateStatistics();
    }

    private void applyFilters() {
        List<LeaveRecord> filtered = leaveList.stream()
                .filter(this::matchesEmployeeFilter)
                .filter(this::matchesLeaveTypeFilter)
                .filter(this::matchesDateFilter)
                .collect(Collectors.toList());
        
        filteredList.setAll(filtered);
    }

    private boolean matchesEmployeeFilter(LeaveRecord record) {
        Employee selectedEmployee = employeeFilterCombo.getValue();
        return selectedEmployee == null || record.getEmployeeId() == selectedEmployee.getId();
    }

    private boolean matchesLeaveTypeFilter(LeaveRecord record) {
        String selectedType = leaveTypeFilterCombo.getValue();
        return "Tüm İzin Türleri".equals(selectedType) || record.getLeaveType().equals(selectedType);
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
        if (filteredList.isEmpty()) {
            totalDaysLabel.setText("0");
            avgDaysLabel.setText("0 gün");
            topEmployeeLabel.setText("-");
            totalRecordsLabel.setText("0");
            return;
        }

        // Toplam gün hesaplama
        int totalDays = filteredList.stream()
                .mapToInt(record -> leaveCalculator.calculateLeaveDays(
                    record.getStartDate(), record.getEndDate(), officialHolidays))
                .sum();

        // Ortalama gün hesaplama
        double avgDays = (double) totalDays / filteredList.size();

        // En çok izin kullanan çalışan
        Map<Integer, Integer> employeeLeaveDays = filteredList.stream()
                .collect(Collectors.groupingBy(
                    LeaveRecord::getEmployeeId,
                    Collectors.summingInt(record -> leaveCalculator.calculateLeaveDays(
                        record.getStartDate(), record.getEndDate(), officialHolidays))
                ));

        Optional<Map.Entry<Integer, Integer>> topEmployee = employeeLeaveDays.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        String topEmployeeName = topEmployee.map(entry -> {
            Employee employee = employeeMap.get(entry.getKey());
            return employee != null ? employee.getName() + " (" + entry.getValue() + " gün)" : "Bilinmeyen";
        }).orElse("-");

        totalDaysLabel.setText(String.valueOf(totalDays));
        avgDaysLabel.setText(String.format("%.1f gün", avgDays));
        topEmployeeLabel.setText(topEmployeeName);
        totalRecordsLabel.setText(String.valueOf(filteredList.size()));
    }

    private void handleDelete(LeaveRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("İzin Kaydı Sil");
        alert.setHeaderText("Bu izin kaydını silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            leaveRecordRepository.delete(record.getId());
            loadAllLeaves();
            updateStatistics();
            showAlert("İzin kaydı başarıyla silindi.", Alert.AlertType.INFORMATION);
        }
    }

    private void handleShowDetails(LeaveRecord record) {
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

    @FXML
    private void onExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("İzin Raporu Export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                pdfExportService.exportLeaveReport(filteredList, employeeMap, file.getAbsolutePath());
                showAlert("İzin raporu başarıyla dışa aktarıldı.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("PDF oluşturulurken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) leaveTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }

    // Çalışanın kalan izin gününü hesapla
    private int calculateRemainingLeave(Employee employee) {
        int totalUsed = leaveRecordRepository.getByEmployeeId(employee.getId()).stream()
                .filter(record -> "Yıllık İzin".equals(record.getLeaveType()))
                .mapToInt(record -> leaveCalculator.calculateLeaveDays(record.getStartDate(), record.getEndDate(), officialHolidays))
                .sum();
        return employee.getAnnualLeaveDays() - totalUsed;
    }
} 