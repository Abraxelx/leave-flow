package com.halilsahin.leaveflow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:leaveflow.db";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS employee (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "hire_date TEXT," +
                    "annual_leave_days INTEGER NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS leave_record (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "employee_id INTEGER NOT NULL," +
                    "leave_type TEXT NOT NULL," +
                    "start_date TEXT NOT NULL," +
                    "end_date TEXT NOT NULL," +
                    "description TEXT," +
                    "calculated_days INTEGER DEFAULT 0," +
                    "day_details TEXT," +
                    "FOREIGN KEY(employee_id) REFERENCES employee(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS official_holiday (" +
                    "date TEXT PRIMARY KEY," +
                    "description TEXT)");
            
            // Mevcut tabloyu güncelle (yeni sütunları ekle)
            updateExistingTables(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void updateExistingTables(Statement stmt) {
        try {
            // calculated_days sütunu yoksa ekle
            stmt.execute("ALTER TABLE leave_record ADD COLUMN calculated_days INTEGER DEFAULT 0");
        } catch (Exception e) {
            // Sütun zaten varsa hata verir, bu normal
        }
        
        try {
            // day_details sütunu yoksa ekle
            stmt.execute("ALTER TABLE leave_record ADD COLUMN day_details TEXT");
        } catch (Exception e) {
            // Sütun zaten varsa hata verir, bu normal
        }
    }
} 