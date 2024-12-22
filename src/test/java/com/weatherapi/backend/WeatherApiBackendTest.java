package com.weatherapi.backend;

import com.weatherapi.backend.client.WeatherApiClient;
import com.weatherapi.backend.listeners.WeatherApiTestListener;
import com.weatherapi.backend.providers.WeatherApiDataProvider;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(WeatherApiTestListener.class)
public class WeatherApiBackendTest {
    private WeatherApiClient apiClient;
    private SoftAssert softAssert;

    @BeforeSuite
    public void setUp() {
        apiClient = new WeatherApiClient();
        softAssert = new SoftAssert();
    }


    @Test(description = "Test to verify the current weather response",
            groups = {"smoke", "critical"},
            dataProviderClass = WeatherApiDataProvider.class,
            dataProvider = "citiesProvider")
    public void testGetCurrentWeather(String city, String country) {
        Response response = apiClient.getCurrentWeather(city);

        // Basic response validation
        assertEquals(response.getStatusCode(), 200,
                String.format("Failed to get weather for %s, %s. (Status code: %s)",
                        city, country, response.getStatusCode()));

        assertTrue(response.getTime() < 2000,
                String.format("Response time should be less than 2000 ms, but was %d ms",
                        response.getTime()));
        JsonPath response_body = response.jsonPath();

        // Location validation
        String actualLocation = response_body.getString("location.name");
        String actualCountry = response_body.getString("location.country");

        assertTrue(actualLocation.contains(city) || city.contains(actualLocation),
                String.format("Location name should contain %s, but was %s",
                        city, actualLocation));

        assertEquals(actualCountry, country,
                String.format("Country should match expected value. Expected: %s, Actual: %s",
                        country, actualCountry));

        // Current weather specific validations
        softAssert.assertNotNull(response_body.get("current.last_updated"),
                "Last updated timestamp should be present");
        softAssert.assertNotNull(response_body.get("current.temp_c"),
                "Current temperature should be present");
        softAssert.assertNotNull(response_body.get("current.condition.text"),
                "Current weather condition should be present");
        softAssert.assertNotNull(response_body.get("current.wind_kph"),
                "Current wind speed should be present");
        softAssert.assertNotNull(response_body.get("current.humidity"),
                "Current humidity should be present");

        // Verify that forecast data is NOT present
        softAssert.assertNull(response_body.get("forecast"),
                "Forecast data should not be present in current weather response");

        softAssert.assertAll();
    }

    @Test(description = "Test to verify the weather forecast response",
            groups = {"smoke", "critical"},
            dataProviderClass = WeatherApiDataProvider.class,
            dataProvider = "forecastDataProvider")
    public void testGetWeatherForecast(String city, String country, int forecastDays) {
        Response response = apiClient.getWeatherForecast(city, forecastDays);

        // Verify response status and time
        assertEquals(response.getStatusCode(), 200,
                String.format("Failed to get forecast for %s, %s. (Status code: %s)",
                        city, country, response.getStatusCode()));

        assertTrue(response.getTime() < 2000,
                String.format("Response time should be less than 2000 ms, but was %d ms",
                        response.getTime()));

        JsonPath response_body = response.jsonPath();

        // Verify location details
        String actualLocation = response_body.getString("location.name");
        String actualCountry = response_body.getString("location.country");

        assertTrue(actualLocation.contains(city) || city.contains(actualLocation),
                String.format("Location name should contain %s, but was %s",
                        city, actualLocation));

        assertEquals(actualCountry, country,
                String.format("Country should match expected value. Expected: %s, Actual: %s",
                        country, actualCountry));

        // Verify current data is included
        softAssert.assertNotNull(response_body.get("current"),
                "Current weather data should be included in forecast response");

        // Verify forecast-specific structure
        softAssert.assertNotNull(response_body.get("forecast"),
                "Forecast object should be present");
        softAssert.assertNotNull(response_body.get("forecast.forecastday"),
                "Forecast days array should be present");

        // Verify number of forecast days
        int actualForecastDays = response_body.getList("forecast.forecastday").size();
        assertEquals(actualForecastDays, forecastDays,
                String.format("Number of forecast days should be %d, but was %d",
                        forecastDays, actualForecastDays));
    }
}
