package com.halilsahin.leaveflow.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.halilsahin.leaveflow.model.OfficialHoliday;
import com.halilsahin.leaveflow.repository.OfficialHolidayRepository;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HolidayFetcher {
    private static final String CALENDAR_ID = "tr.turkish%23holiday%40group.v.calendar.google.com";
    private static final String API_URL_TEMPLATE = "https://www.googleapis.com/calendar/v3/calendars/%s/events?key=%s&timeMin=%s-01-01T00:00:00Z&timeMax=%s-12-31T23:59:59Z&maxResults=50&singleEvents=true&orderBy=startTime";

    public List<OfficialHoliday> fetchHolidays(int year) throws Exception {
        String apiKey = ConfigLoader.getApiKey();
        if (apiKey == null) {
            throw new Exception("API anahtarı 'config.properties' dosyasında bulunamadı veya ayarlanmadı.");
        }

        String url = String.format(API_URL_TEMPLATE, CALENDAR_ID, apiKey, year, year);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("Google Calendar API'den geçersiz yanıt alındı. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JsonNode root = mapper.readTree(response.body());

        List<OfficialHoliday> holidays = new ArrayList<>();
        if (root.has("items")) {
            for (JsonNode item : root.get("items")) {
                if (item.has("start") && item.get("start").has("date")) {
                    String dateStr = item.get("start").get("date").asText();
                    String summary = item.get("summary").asText("Resmi Tatil");
                    holidays.add(new OfficialHoliday(LocalDate.parse(dateStr), summary));
                }
            }
        }
        return holidays;
    }

    public void saveToJson(List<OfficialHoliday> holidays, int year) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        File dir = new File("tatiller");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, year + ".json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, holidays);
    }

    public void saveToDatabase(List<OfficialHoliday> holidays) {
        OfficialHolidayRepository repo = new OfficialHolidayRepository();
        repo.clearAll();
        for (OfficialHoliday h : holidays) {
            repo.add(h);
        }
    }

    public List<OfficialHoliday> loadFromJson(int year) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        File file = new File("tatiller/" + year + ".json");
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        JsonNode root = mapper.readTree(file);
        List<OfficialHoliday> holidays = new ArrayList<>();
        
        if (root.isArray()) {
            for (JsonNode node : root) {
                holidays.add(new OfficialHoliday(node));
            }
        }
        
        return holidays;
    }
} 