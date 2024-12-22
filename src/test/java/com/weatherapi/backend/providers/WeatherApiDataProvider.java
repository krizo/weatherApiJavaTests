package com.weatherapi.backend.providers;

import org.testng.annotations.DataProvider;

public class WeatherApiDataProvider {
    @DataProvider(name = "citiesProvider")
    public static Object[][] getCitiesData() {
        return new Object[][]{
                {"London", "United Kingdom"},
                {"Berlin", "Germany"},
                {"Warsaw", "Poland"}
        };
    }

    @DataProvider(name = "forecastDataProvider")
    public static Object[][] getForecastData() {
        return new Object[][]{
                {"London", "United Kingdom", 3},
                {"Berlin", "Germany", 5},
                {"Warsaw", "Poland", 7}
        };
    }
}
