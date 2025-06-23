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
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

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
    @FXML private Tab employeesTab;
    @FXML private Tab leavesTab;
    @FXML private Tab reportsTab;
    @FXML private Tab settingsTab;
    @FXML private Button quickAddButton;
    @FXML private Button addEmployeeButton;
    @FXML private Button exportLeavesButton;
    @FXML private Button updateHolidaysButton;
    @FXML private Button backupDatabaseButton;
    @FXML private Button restoreDatabaseButton;
    @FXML private Button resetDatabaseButton;
    @FXML private Button updateAnnualLeavesButton;
    @FXML private Button aboutButton;
    
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
    @FXML private TableColumn<LeaveRecord, String> colLeaveId;
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
    @FXML private TableView<OfficialHoliday> holidayTable;
    @FXML private TableColumn<OfficialHoliday, String> colHolidayDate;
    @FXML private TableColumn<OfficialHoliday, String> colHolidayDay;
    @FXML private TableColumn<OfficialHoliday, String> colHolidayDesc;
    
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
        setupTabIcons();
        setupButtonIcons();
        loadData();
        setupEmployeeTable();
        setupLeaveTable();
        setupFilters();
        setupSettings();
        setupDatePickerLocale(startDateFilter);
        setupDatePickerLocale(endDateFilter);
        loadHolidaysTable();
        
        // Tab değişikliklerini dinle
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().contains("İzinler")) {
                loadLeaves();
                updateStatistics();
            }
            if (newTab.getText().contains("Ayarlar")) {
                loadHolidaysTable();
            }
        });
    }
    
    private void setupTabIcons() {
        employeesTab.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.USERS));
        leavesTab.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CALENDAR));
        reportsTab.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.BAR_CHART));
        settingsTab.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.COG));
    }
    
    private void setupButtonIcons() {
        quickAddButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ROCKET));
        addEmployeeButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.USER_PLUS));
        exportLeavesButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.FILE));
        updateHolidaysButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.REFRESH));
        backupDatabaseButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD));
        restoreDatabaseButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.UPLOAD));
        resetDatabaseButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.ERASER));
        updateAnnualLeavesButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CALENDAR));
        aboutButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.INFO_CIRCLE));
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
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
            private final Button editButton = createIconButton(FontAwesomeIcon.PENCIL, "edit-button");
            private final Button deleteButton = createIconButton(FontAwesomeIcon.TRASH, "delete-button");
            
            {
                editButton.setTooltip(new Tooltip("Çalışanı Düzenle"));
                editButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleEditEmployee(employee);
                });

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
        leaveTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colLeaveId.setCellValueFactory(cellData -> {
            int index = leaveTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
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
        
        // Sıralı izin listesi (en yeni en üstte)
        List<LeaveRecord> sortedLeaves = filteredLeaveList.stream()
            .sorted((a, b) -> {
                int cmp = Integer.compare(a.getEmployeeId(), b.getEmployeeId());
                if (cmp == 0) {
                    return b.getStartDate().compareTo(a.getStartDate());
                }
                return cmp;
            })
            .collect(Collectors.toList());
        leaveTable.getItems().setAll(sortedLeaves);
        colLeaveRemaining.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    LeaveRecord record = getTableRow().getItem();
                    Integer remaining = record.getRemainingLeave();
                    setText((remaining != null ? remaining : 0) + " gün");
                }
            }
        });

        // İşlem butonları
        colLeaveActions.setCellFactory(column -> new TableCell<>() {
            private final Button detailButton = createIconButton(FontAwesomeIcon.EYE, "detail-button");
            private final Button deleteButton = createIconButton(FontAwesomeIcon.TRASH, "delete-button");

            {
                detailButton.setTooltip(new Tooltip("İzin Detayını Görüntüle"));
                detailButton.setOnAction(event -> {
                    LeaveRecord record = getTableView().getItems().get(getIndex());
                    handleShowLeaveDetails(record);
                });

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
        loadHolidaysTable();
    }
    
    @FXML
    private void onBackupDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Veritabanı Yedeğini Kaydet");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB", "*.db"));
        fileChooser.setInitialFileName("leaveflow_backup.db");
        File dest = fileChooser.showSaveDialog(null);
        if (dest != null) {
            try {
                java.nio.file.Files.copy(
                    new File("leaveflow.db").toPath(),
                    dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                showAlert("Veritabanı başarıyla yedeklendi!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Yedekleme sırasında hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void onRestoreDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Yedekten Veritabanı Geri Yükle");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB", "*.db"));
        File src = fileChooser.showOpenDialog(null);
        if (src != null) {
            try {
                java.nio.file.Files.copy(
                    src.toPath(),
                    new File("leaveflow.db").toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                loadData();
                showAlert("Veritabanı yedekten başarıyla geri yüklendi!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Geri yükleme sırasında hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void onResetDatabase() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Veritabanı Sıfırla");
        alert.setHeaderText("Tüm verileri silmek istediğinizden emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                java.nio.file.Files.deleteIfExists(new File("leaveflow.db").toPath());
                // Boş veritabanı otomatik oluşacak (uygulama açıldığında)
                loadData();
                showAlert("Veritabanı sıfırlandı. Uygulamayı yeniden başlatmanız önerilir.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Sıfırlama sırasında hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void onUpdateAnnualLeaves() {
        for (Employee employee : employeeList) {
            int newAnnualLeave = employee.calculateMinimumAnnualLeave();
            employee.setAnnualLeaveDays(newAnnualLeave);
            employeeRepository.update(employee);
        }
        loadData();
        showAlert("Tüm çalışanların yıllık izin hakları hizmet süresine göre güncellendi!", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void onShowAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hakkında");
        alert.setHeaderText("İzin Yönetim Sistemi");
        alert.setContentText("Geliştirici: Halil Şahin\nE-posta: halilsahin.dev@gmail.com\n© 2025");
        alert.showAndWait();
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditEmployeeView.fxml"));
            Scene scene = new Scene(loader.load());
            EditEmployeeController controller = loader.getController();
            controller.setEmployee(employee);
            Stage stage = new Stage();
            stage.setTitle("Çalışan Bilgilerini Düzenle");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setOnHidden(e -> loadData());
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Hata: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Genel İzin Raporu Export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("genel_izin_raporu.pdf");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<LeaveRecord> allRecords = leaveRecordRepository.getAll();
                List<Employee> allEmployees = employeeRepository.getAll();
                Map<Integer, Employee> employeeMap = allEmployees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
                pdfExportService.exportLeaveReport(allRecords, employeeMap, file.getAbsolutePath());
                showAlert("Genel izin raporu başarıyla dışa aktarıldı.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("PDF oluşturulurken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    
    private void exportEmployeeReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Çalışan Bazlı İzin Raporu Export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("calisan_izin_raporu.pdf");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<LeaveRecord> allRecords = leaveRecordRepository.getAll();
                List<Employee> allEmployees = employeeRepository.getAll();
                Map<Integer, Employee> employeeMap = allEmployees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
                pdfExportService.exportLeaveReport(allRecords, employeeMap, file.getAbsolutePath());
                showAlert("Çalışan bazlı izin raporu başarıyla dışa aktarıldı.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("PDF oluşturulurken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    
    private void exportMonthlyReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Aylık İzin Raporu Export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("aylik_izin_raporu.pdf");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<LeaveRecord> allRecords = leaveRecordRepository.getAll();
                List<Employee> allEmployees = employeeRepository.getAll();
                Map<Integer, Employee> employeeMap = allEmployees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
                int currentMonth = LocalDate.now().getMonthValue();
                int currentYear = LocalDate.now().getYear();
                List<LeaveRecord> monthly = allRecords.stream()
                    .filter(r -> r.getStartDate().getMonthValue() == currentMonth && r.getStartDate().getYear() == currentYear)
                    .collect(Collectors.toList());
                pdfExportService.exportLeaveReport(monthly, employeeMap, file.getAbsolutePath());
                showAlert("Aylık izin raporu başarıyla dışa aktarıldı.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("PDF oluşturulurken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    
    private void exportStatsReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("İstatistik Raporu Export");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("istatistik_izin_raporu.pdf");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<LeaveRecord> allRecords = leaveRecordRepository.getAll();
                List<Employee> allEmployees = employeeRepository.getAll();
                Map<Integer, Employee> employeeMap = allEmployees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
                pdfExportService.exportLeaveReport(allRecords, employeeMap, file.getAbsolutePath());
                showAlert("İstatistik raporu başarıyla dışa aktarıldı.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("PDF oluşturulurken hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
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
    
    private void loadHolidaysTable() {
        try {
            int year = Integer.parseInt(holidayYearField.getText().trim());
            List<OfficialHoliday> holidays = holidayRepository.getAll();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
            java.util.Locale tr = new java.util.Locale("tr", "TR");
            colHolidayDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().format(formatter)));
            colHolidayDay.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, tr)));
            colHolidayDesc.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
            // Sadece seçili yılın tatilleri
            List<OfficialHoliday> filtered = holidays.stream().filter(h -> h.getDate().getYear() == year).toList();
            holidayTable.getItems().setAll(filtered);
        } catch (Exception e) {
            holidayTable.getItems().clear();
        }
    }
    
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.showAndWait();
    }

    private Button createIconButton(FontAwesomeIcon icon, String styleClass) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16px");
        iconView.setStyleClass("icon");
        Button button = new Button();
        button.setGraphic(iconView);
        button.getStyleClass().add(styleClass);
        button.setStyle("-fx-background-radius: 16; -fx-padding: 4 10; -fx-cursor: hand;");
        return button;
    }
} 