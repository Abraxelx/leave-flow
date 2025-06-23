package com.halilsahin.leaveflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.halilsahin.leaveflow.model.OfficialHoliday;

public class LeaveCalculator {
    
    private static final Map<DayOfWeek, String> TURKISH_DAYS = new HashMap<>();
    
    static {
        TURKISH_DAYS.put(DayOfWeek.MONDAY, "Pazartesi");
        TURKISH_DAYS.put(DayOfWeek.TUESDAY, "Salı");
        TURKISH_DAYS.put(DayOfWeek.WEDNESDAY, "Çarşamba");
        TURKISH_DAYS.put(DayOfWeek.THURSDAY, "Perşembe");
        TURKISH_DAYS.put(DayOfWeek.FRIDAY, "Cuma");
        TURKISH_DAYS.put(DayOfWeek.SATURDAY, "Cumartesi");
        TURKISH_DAYS.put(DayOfWeek.SUNDAY, "Pazar");
    }
    
    public int calculateLeaveDays(LocalDate start, LocalDate end, List<OfficialHoliday> holidays) {
        int days = 0;
        List<LocalDate> holidayDates = holidays.stream().map(OfficialHoliday::getDate).toList();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
                continue;
            if (holidayDates.contains(date))
                continue;
            days++;
        }
        return days;
    }
    
    public String calculateLeaveDaysWithDetails(LocalDate start, LocalDate end, List<OfficialHoliday> holidays) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode daysArray = mapper.createArrayNode();
        int totalDays = 0;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            ObjectNode dayNode = mapper.createObjectNode();
            dayNode.put("date", date.format(formatter));
            dayNode.put("dayOfWeek", date.getDayOfWeek().toString());
            dayNode.put("turkishDay", TURKISH_DAYS.getOrDefault(date.getDayOfWeek(), date.getDayOfWeek().toString()));
            
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            final LocalDate currentDate = date;
            Optional<OfficialHoliday> holiday = holidays.stream()
                .filter(h -> h.getDate().equals(currentDate))
                .findFirst();
            
            if (isWeekend) {
                dayNode.put("type", "weekend");
                dayNode.put("reason", "Hafta Sonu");
                dayNode.put("counted", false);
            } else if (holiday.isPresent()) {
                dayNode.put("type", "holiday");
                dayNode.put("reason", holiday.get().getDescription());
                dayNode.put("counted", false);
            } else {
                dayNode.put("type", "workday");
                dayNode.put("reason", "İş Günü");
                dayNode.put("counted", true);
                totalDays++;
            }
            
            daysArray.add(dayNode);
        }
        
        ObjectNode result = mapper.createObjectNode();
        result.put("totalDays", totalDays);
        result.set("days", daysArray);
        
        return result.toString();
    }
} 