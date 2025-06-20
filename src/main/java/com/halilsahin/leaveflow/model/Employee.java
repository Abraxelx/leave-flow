package com.halilsahin.leaveflow.model;

import java.time.LocalDate;
import java.time.Period;

public class Employee {
    private int id;
    private String name;
    private LocalDate hireDate;
    private int annualLeaveDays;

    public Employee(int id, String name, LocalDate hireDate, int annualLeaveDays) {
        this.id = id;
        this.name = name;
        this.hireDate = hireDate;
        this.annualLeaveDays = annualLeaveDays;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public int getAnnualLeaveDays() { return annualLeaveDays; }
    public void setAnnualLeaveDays(int annualLeaveDays) { this.annualLeaveDays = annualLeaveDays; }

    // Hizmet süresini hesapla (yıl olarak)
    public int getServiceYears() {
        if (hireDate == null) return 0;
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    // Hizmet süresine göre minimum yıllık izin hesapla
    public int calculateMinimumAnnualLeave() {
        int serviceYears = getServiceYears();
        
        if (serviceYears >= 15) {
            return 26; // 15 yıl ve üzeri: 26 gün
        } else if (serviceYears > 5) {
            return 20; // 5-15 yıl arası: 20 gün
        } else {
            return 14; // 1-5 yıl arası: 14 gün
        }
    }
} 