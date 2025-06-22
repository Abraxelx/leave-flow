package com.halilsahin.leaveflow.model;

import java.time.LocalDate;
import java.util.List;

public class LeaveRecord {
    private int id;
    private int employeeId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private int calculatedDays;
    private String dayDetails; // JSON formatında gün detayları
    private Integer remainingLeave;

    public LeaveRecord(int id, int employeeId, String leaveType, LocalDate startDate, LocalDate endDate, String description) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public LeaveRecord(int id, int employeeId, String leaveType, LocalDate startDate, LocalDate endDate, String description, int calculatedDays, String dayDetails) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.calculatedDays = calculatedDays;
        this.dayDetails = dayDetails;
    }

    public LeaveRecord(int id, int employeeId, String leaveType, LocalDate startDate, LocalDate endDate, String description, int calculatedDays, String dayDetails, Integer remainingLeave) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.calculatedDays = calculatedDays;
        this.dayDetails = dayDetails;
        this.remainingLeave = remainingLeave;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCalculatedDays() { return calculatedDays; }
    public void setCalculatedDays(int calculatedDays) { this.calculatedDays = calculatedDays; }
    public String getDayDetails() { return dayDetails; }
    public void setDayDetails(String dayDetails) { this.dayDetails = dayDetails; }
    public Integer getRemainingLeave() { return remainingLeave; }
    public void setRemainingLeave(Integer remainingLeave) { this.remainingLeave = remainingLeave; }
} 