package com.weatherapi.backend.client;

import com.weatherapi.config.ConfigLoader;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class WeatherApiClient {
    private final RequestSpecification requestSpec;

    public WeatherApiClient() {
        requestSpec = given()
                .baseUri(ConfigLoader.getBaseUrl())
                .queryParam("key", ConfigLoader.getApiKey())
                .contentType("application/json");
    }



    /**
 * Retrieves the current weather for a given location.
 *
 * @param location the city name or coordinates
 * @return Response the response object containing the weather data
 */
public Response getCurrentWeather(String location) {
        return given()
                .spec(requestSpec)
                .queryParam("q", location)
                .get(ConfigLoader.getWeatherEndpoint());
    }

    /**
     * Retrieves the weather forecast for a given location
     *
     * @param location city name or coordinates
     * @return Response Response object
     */
    public Response getWeatherForecast(String location, int days) {
        return given()
                .spec(requestSpec)
                .queryParam("q", location)
                .queryParam("days", days)
                .when()
                .get(ConfigLoader.getForecastEndpoint());
    }
}
