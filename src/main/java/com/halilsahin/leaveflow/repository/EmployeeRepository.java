package com.halilsahin.leaveflow.repository;

import com.halilsahin.leaveflow.model.Employee;
import com.halilsahin.leaveflow.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {
    public List<Employee> getAll() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employee")) {
            while (rs.next()) {
                employees.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("hire_date") != null ? LocalDate.parse(rs.getString("hire_date")) : null,
                        rs.getInt("annual_leave_days")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employees;
    }

    public void add(Employee emp) {
        String sql = "INSERT INTO employee (name, hire_date, annual_leave_days) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getName());
            pstmt.setString(2, emp.getHireDate() != null ? emp.getHireDate().toString() : null);
            pstmt.setInt(3, emp.getAnnualLeaveDays());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(Employee emp) {
        String sql = "UPDATE employee SET name = ?, hire_date = ?, annual_leave_days = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getName());
            pstmt.setString(2, emp.getHireDate() != null ? emp.getHireDate().toString() : null);
            pstmt.setInt(3, emp.getAnnualLeaveDays());
            pstmt.setInt(4, emp.getId());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM employee WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Employee getById(int id) {
        String sql = "SELECT * FROM employee WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("hire_date") != null ? LocalDate.parse(rs.getString("hire_date")) : null,
                        rs.getInt("annual_leave_days")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
} 