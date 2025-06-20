package com.halilsahin.leaveflow.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                System.err.println("Üzgünüz, config.properties dosyası bulunamadı.");
            } else {
                properties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getApiKey() {
        String apiKey = properties.getProperty("google.calendar.api.key");
        if (apiKey == null || apiKey.equals("YOUR_API_KEY_HERE") || apiKey.trim().isEmpty()) {
            System.err.println("API anahtarı 'config.properties' dosyasında ayarlanmamış.");
            return null;
        }
        return apiKey;
    }
} 