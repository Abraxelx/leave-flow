package com.halilsahin.leaveflow.repository;

import com.halilsahin.leaveflow.model.LeaveRecord;
import com.halilsahin.leaveflow.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveRecordRepository {
    public List<LeaveRecord> getAll() {
        List<LeaveRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM leave_record")) {
            while (rs.next()) {
                records.add(new LeaveRecord(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("leave_type"),
                        LocalDate.parse(rs.getString("start_date")),
                        LocalDate.parse(rs.getString("end_date")),
                        rs.getString("description"),
                        rs.getInt("calculated_days"),
                        rs.getString("day_details")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

    public List<LeaveRecord> getByEmployeeId(int employeeId) {
        List<LeaveRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM leave_record WHERE employee_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(new LeaveRecord(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getString("leave_type"),
                        LocalDate.parse(rs.getString("start_date")),
                        LocalDate.parse(rs.getString("end_date")),
                        rs.getString("description"),
                        rs.getInt("calculated_days"),
                        rs.getString("day_details")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

    public void add(LeaveRecord record) {
        String sql = "INSERT INTO leave_record (employee_id, leave_type, start_date, end_date, description, calculated_days, day_details) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, record.getEmployeeId());
            pstmt.setString(2, record.getLeaveType());
            pstmt.setString(3, record.getStartDate().toString());
            pstmt.setString(4, record.getEndDate().toString());
            pstmt.setString(5, record.getDescription());
            pstmt.setInt(6, record.getCalculatedDays());
            pstmt.setString(7, record.getDayDetails());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM leave_record WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Diğer CRUD işlemleri eklenebilir.
} 