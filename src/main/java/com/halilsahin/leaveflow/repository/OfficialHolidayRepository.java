package com.halilsahin.leaveflow.repository;

import com.halilsahin.leaveflow.model.OfficialHoliday;
import com.halilsahin.leaveflow.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OfficialHolidayRepository {
    public List<OfficialHoliday> getAll() {
        List<OfficialHoliday> holidays = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM official_holiday")) {
            while (rs.next()) {
                holidays.add(new OfficialHoliday(
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("description")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return holidays;
    }

    public void add(OfficialHoliday holiday) {
        String sql = "INSERT OR IGNORE INTO official_holiday (date, description) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, holiday.getDate().toString());
            pstmt.setString(2, holiday.getDescription());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAll() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM official_holiday");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 