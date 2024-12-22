package com.weatherapi.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties;
    private static final String CONFIG_FILE = "config.properties";


    static {
        properties = new Properties();
        try {
            FileInputStream input = new FileInputStream(CONFIG_FILE);
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config file: " + CONFIG_FILE, e);
        }
    }

    public static String getApiKey() {
        return properties.getProperty("weather.api.key");
    }

    public static String getBaseUrl() {
        return properties.getProperty("weather.api.base.url");
    }

    public static String getWeatherEndpoint() {
        return properties.getProperty("weather.api.current.endpoint");
    }

    public static String getForecastEndpoint() {
        return properties.getProperty("weather.api.forecast.endpoint");
    }
}
