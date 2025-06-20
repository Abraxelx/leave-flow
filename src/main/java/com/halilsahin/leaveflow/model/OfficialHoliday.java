package com.halilsahin.leaveflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

public class OfficialHoliday {
    private LocalDate date;
    private String description;

    public OfficialHoliday(LocalDate date, String description) {
        this.date = date;
        this.description = description;
    }

    // JSON array formatından okumak için özel constructor
    @JsonCreator
    public OfficialHoliday(@JsonProperty("date") List<Integer> dateArray, 
                          @JsonProperty("description") String description) {
        if (dateArray != null && dateArray.size() >= 3) {
            this.date = LocalDate.of(dateArray.get(0), dateArray.get(1), dateArray.get(2));
        } else {
            this.date = LocalDate.now(); // Fallback
        }
        this.description = description;
    }

    // Alternatif constructor - JsonNode ile
    public OfficialHoliday(JsonNode node) {
        if (node.has("date") && node.get("date").isArray()) {
            JsonNode dateNode = node.get("date");
            int year = dateNode.get(0).asInt();
            int month = dateNode.get(1).asInt();
            int day = dateNode.get(2).asInt();
            this.date = LocalDate.of(year, month, day);
        } else {
            this.date = LocalDate.now(); // Fallback
        }
        this.description = node.has("description") ? node.get("description").asText() : "Resmi Tatil";
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
} 