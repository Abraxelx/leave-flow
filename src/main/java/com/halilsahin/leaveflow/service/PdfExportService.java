package com.halilsahin.leaveflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.halilsahin.leaveflow.model.Employee;
import com.halilsahin.leaveflow.model.LeaveRecord;
import com.halilsahin.leaveflow.model.OfficialHoliday;
import com.halilsahin.leaveflow.repository.LeaveRecordRepository;
import com.halilsahin.leaveflow.repository.OfficialHolidayRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.io.File;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PdfExportService {
    
    private static final Map<DayOfWeek, String> TURKISH_DAYS = new HashMap<>();
    private static final Map<String, String> TURKISH_MONTHS = new HashMap<>();
    
    static {
        // Türkçe gün isimleri
        TURKISH_DAYS.put(DayOfWeek.MONDAY, "Pazartesi");
        TURKISH_DAYS.put(DayOfWeek.TUESDAY, "Salı");
        TURKISH_DAYS.put(DayOfWeek.WEDNESDAY, "Çarşamba");
        TURKISH_DAYS.put(DayOfWeek.THURSDAY, "Perşembe");
        TURKISH_DAYS.put(DayOfWeek.FRIDAY, "Cuma");
        TURKISH_DAYS.put(DayOfWeek.SATURDAY, "Cumartesi");
        TURKISH_DAYS.put(DayOfWeek.SUNDAY, "Pazar");
        
        // Türkçe ay isimleri
        TURKISH_MONTHS.put("JANUARY", "Ocak");
        TURKISH_MONTHS.put("FEBRUARY", "Şubat");
        TURKISH_MONTHS.put("MARCH", "Mart");
        TURKISH_MONTHS.put("APRIL", "Nisan");
        TURKISH_MONTHS.put("MAY", "Mayıs");
        TURKISH_MONTHS.put("JUNE", "Haziran");
        TURKISH_MONTHS.put("JULY", "Temmuz");
        TURKISH_MONTHS.put("AUGUST", "Ağustos");
        TURKISH_MONTHS.put("SEPTEMBER", "Eylül");
        TURKISH_MONTHS.put("OCTOBER", "Ekim");
        TURKISH_MONTHS.put("NOVEMBER", "Kasım");
        TURKISH_MONTHS.put("DECEMBER", "Aralık");
    }
    
    // Noto Sans fontu ile Türkçe karakter ve simge desteği
    private PdfFont getRobotoFont() throws Exception {
        try {
            // Noto Sans fontunu resources klasöründen yükle (simge desteği daha iyi)
            String fontPath = Objects.requireNonNull(getClass().getClassLoader().getResource("fonts/NotoSansSymbols2-Regular.ttf")).getPath();
            return PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
        } catch (Exception e) {
            System.err.println("Noto Sans font yüklenemedi: " + e.getMessage());
            try {
                // Roboto fontunu dene
                String robotoPath = Objects.requireNonNull(getClass().getClassLoader().getResource("fonts/Roboto-Regular.ttf")).getPath();
                return PdfFontFactory.createFont(robotoPath, PdfEncodings.IDENTITY_H);
            } catch (Exception e2) {
                System.err.println("Roboto font yüklenemedi: " + e2.getMessage());
                try {
                    // Fallback: Sistem fontlarını dene
                    return PdfFontFactory.createFont("Arial Unicode MS", PdfEncodings.UTF8);
                } catch (Exception e3) {
                    try {
                        return PdfFontFactory.createFont("Helvetica", PdfEncodings.UTF8);
                    } catch (Exception e4) {
                        // Son çare: Varsayılan font
                        return PdfFontFactory.createFont();
                    }
                }
            }
        }
    }
    
    // Sayısal kısımlar için ayrı font (daha okunaklı)
    private PdfFont getNumericFont() throws Exception {
        try {
            // Roboto Light fontunu kullan (sayılar için daha ince ve okunaklı)
            String fontPath = Objects.requireNonNull(getClass().getClassLoader().getResource("fonts/Roboto-Light.ttf")).getPath();
            return PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
        } catch (Exception e) {
            try {
                // Courier New dene (monospace font)
                return PdfFontFactory.createFont("Courier New", PdfEncodings.UTF8);
            } catch (Exception e2) {
                try {
                    // Consolas dene
                    return PdfFontFactory.createFont("Consolas", PdfEncodings.UTF8);
                } catch (Exception e3) {
                    // Son çare: Ana fontu kullan
                    return getRobotoFont();
                }
            }
        }
    }
    
    // Eski font metodunu kaldır
    private PdfFont getEmbeddedTurkishFont() throws Exception {
        return getRobotoFont();
    }
    
    private String getTurkishDayName(DayOfWeek dayOfWeek) {
        return TURKISH_DAYS.getOrDefault(dayOfWeek, dayOfWeek.toString());
    }
    
    // Çalışanın kalan izin gününü hesapla
    private int calculateRemainingLeave(Employee employee, List<LeaveRecord> allRecords, List<LocalDate> holidays) {
        int totalUsed = allRecords.stream()
                .filter(record -> record.getEmployeeId() == employee.getId() && "Yıllık İzin".equals(record.getLeaveType()))
                .mapToInt(LeaveRecord::getCalculatedDays)
                .sum();
        return employee.getAnnualLeaveDays() - totalUsed;
    }
    
    public void exportLeaveReport(List<LeaveRecord> records, Map<Integer, Employee> employeeMap, String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        PdfFont font = getEmbeddedTurkishFont();
        PdfFont numericFont = getNumericFont(); // Sayısal kısımlar için ayrı font
        
        // Resmi tatilleri yükle (kalan izin hesaplaması için)
        OfficialHolidayRepository holidayRepo = new OfficialHolidayRepository();
        List<LocalDate> holidays = holidayRepo.getAll().stream()
                .map(OfficialHoliday::getDate)
                .collect(Collectors.toList());
        
        // Tüm izin kayıtlarını yükle (kalan izin hesaplaması için)
        LeaveRecordRepository leaveRepo = new LeaveRecordRepository();
        List<LeaveRecord> allRecords = leaveRepo.getAll();
        
        // Başlık
        Paragraph title = new Paragraph("İzin Raporu")
                .setFont(font)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        
        document.add(new Paragraph(" ").setFont(font)); // Boşluk
        
        // Tarih
        Paragraph date = new Paragraph("Rapor Tarihi: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .setFont(numericFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(date);
        
        document.add(new Paragraph(" ").setFont(font)); // Boşluk
        
        // Tablo
        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 18, 12, 12, 12, 10, 10, 8}))
                .useAllAvailableWidth();
        
        // Tablo başlıkları
        table.addHeaderCell(new Cell().add(new Paragraph("ID").setFont(font)).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Çalışan").setFont(font)).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("İzin Türü").setFont(font)).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Başlangıç").setFont(font)).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Bitiş").setFont(font)).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Süre").setFont(font)).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Kalan").setFont(font)).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Detay").setFont(font)).setBold());
        
        // Tablo verileri (en yeni izin en üstte olacak şekilde sırala)
        List<LeaveRecord> sortedRecords = records.stream()
            .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
            .collect(Collectors.toList());
        Map<Integer, Integer> employeeUsedDays = new java.util.HashMap<>();
        for (LeaveRecord record : sortedRecords) {
            Employee employee = employeeMap.get(record.getEmployeeId());
            String employeeName = employee != null ? employee.getName() : "Bilinmeyen";
            int totalLeave = employee != null ? employee.getAnnualLeaveDays() : 0;
            int used = employeeUsedDays.getOrDefault(record.getEmployeeId(), 0);
            int thisLeave = record.getCalculatedDays();
            int remainingLeave = totalLeave - used;
            table.addCell(new Cell().add(new Paragraph(String.valueOf(record.getId())).setFont(numericFont)));
            table.addCell(new Cell().add(new Paragraph(employeeName).setFont(font)));
            table.addCell(new Cell().add(new Paragraph(record.getLeaveType()).setFont(font)));
            table.addCell(new Cell().add(new Paragraph(record.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).setFont(numericFont)));
            table.addCell(new Cell().add(new Paragraph(record.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).setFont(numericFont)));
            table.addCell(new Cell().add(new Paragraph(record.getCalculatedDays() + " gün").setFont(numericFont)));
            table.addCell(new Cell().add(new Paragraph(remainingLeave + " gün").setFont(numericFont)));
            // Detay butonu yerine "Görüntüle" yazısı
            Cell detailCell = new Cell().add(new Paragraph("Görüntüle").setFont(font));
            detailCell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
            table.addCell(detailCell);
            // Sonraki kayıtlar için kullanılan günleri güncelle
            employeeUsedDays.put(record.getEmployeeId(), used + thisLeave);
        }
        
        document.add(table);
        
        // İstatistikler
        document.add(new Paragraph(" ").setFont(font)); // Boşluk
        
        Paragraph statsTitle = new Paragraph("İstatistikler")
                .setFont(font)
                .setFontSize(16)
                .setBold();
        document.add(statsTitle);
        
        int totalDays = records.stream().mapToInt(LeaveRecord::getCalculatedDays).sum();
        double avgDays = records.isEmpty() ? 0 : (double) totalDays / records.size();
        
        document.add(new Paragraph("Toplam İzin Günü: " + totalDays).setFont(numericFont)); // Sayı için sayısal font
        document.add(new Paragraph("Ortalama İzin Süresi: " + String.format("%.1f gün", avgDays)).setFont(numericFont)); // Sayı için sayısal font
        document.add(new Paragraph("Toplam Kayıt Sayısı: " + records.size()).setFont(numericFont)); // Sayı için sayısal font
        
        // Footer ekle
        for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
            com.itextpdf.kernel.pdf.PdfPage page = pdf.getPage(i);
            com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas = new com.itextpdf.kernel.pdf.canvas.PdfCanvas(page);
            canvas.beginText();
            canvas.setFontAndSize(font, 8);
            canvas.moveText(40, 20);
            canvas.showText("© 2025 Halil Şahin • halilsahin.dev@gmail.com");
            canvas.endText();
            canvas.release();
        }
        document.close();
    }
    
    public void exportLeaveDetails(LeaveRecord record, Employee employee, String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        PdfFont font = getEmbeddedTurkishFont();
        PdfFont numericFont = getNumericFont(); // Sayısal kısımlar için ayrı font
        
        // Resmi tatilleri yükle
        OfficialHolidayRepository holidayRepo = new OfficialHolidayRepository();
        List<LocalDate> holidays = holidayRepo.getAll().stream()
                .map(OfficialHoliday::getDate)
                .collect(Collectors.toList());
        
        // Tüm izin kayıtlarını yükle
        LeaveRecordRepository leaveRepo = new LeaveRecordRepository();
        List<LeaveRecord> allRecords = leaveRepo.getAll();
        
        // Kalan izin hesapla
        int remainingLeave = calculateRemainingLeave(employee, allRecords, holidays);
        
        // Başlık
        Paragraph title = new Paragraph("İzin Detay Raporu")
                .setFont(font)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        
        document.add(new Paragraph(" ").setFont(font)); // Boşluk
        
        // İzin Bilgileri
        Paragraph infoTitle = new Paragraph("İzin Bilgileri")
                .setFont(font)
                .setFontSize(16)
                .setBold();
        document.add(infoTitle);
        
        document.add(new Paragraph("Çalışan: " + employee.getName()).setFont(font));
        document.add(new Paragraph("İzin Türü: " + record.getLeaveType()).setFont(font));
        document.add(new Paragraph("Başlangıç Tarihi: " + record.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).setFont(numericFont)); // Tarih için sayısal font
        document.add(new Paragraph("Bitiş Tarihi: " + record.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).setFont(numericFont)); // Tarih için sayısal font
        document.add(new Paragraph("Toplam İzin Günü: " + record.getCalculatedDays() + " gün").setFont(numericFont)); // Sayı için sayısal font
        document.add(new Paragraph("Kalan İzin Hakkı: " + remainingLeave + " gün").setFont(numericFont)); // Sayı için sayısal font
        if (record.getDescription() != null && !record.getDescription().isEmpty()) {
            document.add(new Paragraph("Açıklama: " + record.getDescription()).setFont(font));
        }
        
        document.add(new Paragraph(" ").setFont(font)); // Boşluk
        
        // Gün Detayları
        if (record.getDayDetails() != null && !record.getDayDetails().isEmpty()) {
            Paragraph detailsTitle = new Paragraph("Gün Detayları")
                    .setFont(font)
                    .setFontSize(16)
                    .setBold();
            document.add(detailsTitle);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode details = mapper.readTree(record.getDayDetails());
            
            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                    .useAllAvailableWidth();
            
            detailsTable.addHeaderCell(new Cell().add(new Paragraph("Tarih").setFont(font)).setBold());
            detailsTable.addHeaderCell(new Cell().add(new Paragraph("Gün").setFont(font)).setBold());
            detailsTable.addHeaderCell(new Cell().add(new Paragraph("Durum").setFont(font)).setBold());
            detailsTable.addHeaderCell(new Cell().add(new Paragraph("Açıklama").setFont(font)).setBold());
            
            JsonNode days = details.get("days");
            for (JsonNode day : days) {
                detailsTable.addCell(new Cell().add(new Paragraph(day.get("date").asText()).setFont(numericFont))); // Tarih için sayısal font
                
                // Gün adını Türkçe'ye çevir
                String dayOfWeek = day.get("dayOfWeek").asText();
                String turkishDay = getTurkishDayName(DayOfWeek.valueOf(dayOfWeek));
                
                // Eğer JSON'da turkishDay varsa onu kullan
                if (day.has("turkishDay")) {
                    turkishDay = day.get("turkishDay").asText();
                }
                
                detailsTable.addCell(new Cell().add(new Paragraph(turkishDay).setFont(font)));
                
                String type = day.get("type").asText();
                String reason = day.get("reason").asText();
                boolean counted = day.get("counted").asBoolean();
                
                Cell statusCell = new Cell().add(new Paragraph(counted ? "✓ Sayıldı" : "✗ Sayılmadı").setFont(font));
                if (!counted) {
                    statusCell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
                }
                detailsTable.addCell(statusCell);
                
                detailsTable.addCell(new Cell().add(new Paragraph(reason).setFont(font)));
            }
            
            document.add(detailsTable);
        }

        // Footer ekle
        for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
            com.itextpdf.kernel.pdf.PdfPage page = pdf.getPage(i);
            com.itextpdf.kernel.pdf.canvas.PdfCanvas canvas = new com.itextpdf.kernel.pdf.canvas.PdfCanvas(page);
            canvas.beginText();
            canvas.setFontAndSize(font, 8);
            canvas.moveText(40, 20);
            canvas.showText("© 2025 Halil Şahin • halilsahin.dev@gmail.com");
            canvas.endText();
            canvas.release();
        }
        document.close();
    }
    
    public void exportGeneralReport(List<Employee> employees, List<LeaveRecord> allRecords, List<OfficialHoliday> holidays, String filePath) throws Exception {
        // ... existing code ...
    }
} 