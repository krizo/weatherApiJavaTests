# Weather API Test Framework

## 🎯 Overview
This project is an automated testing framework for the Weather API (weatherapi.com). It provides a robust solution for testing both current weather and forecast endpoints, ensuring reliable and accurate weather data retrieval.

## ⭐ Features
* Current weather data validation
* Weather forecast validation
* Parametrized tests for multiple cities
* Response time monitoring
* Comprehensive data validation
* Soft assertions for detailed test reporting

## 🛠 Technical Stack
* Java 11
* REST Assured 5.3.0
* TestNG 7.7.1
* Jackson 2.15.2
* Maven

## 📁 Project Structure
```bash
├── src
│   ├── main
│   │   └── java
│   │       └── com
│   │           └── weatherapi
│   │               ├── backend
│   │               │   ├── client
│   │               │   │   └── WeatherApiClient.java
│   │               │   └── providers
│   │               │       └── WeatherApiDataProvider.java
│   │               └── config
│   │                   └── ConfigLoader.java
│   └── test
│       └── java
│           └── com
│               └── weatherapi
│                   └── backend
│                       └── WeatherApiBackendTest.java
```
## 📋 Prerequisites

* Java JDK 11 or higher
* Maven 3.6 or higher
* Valid Weather API key (to be configured in config.properties)

## ⚙️ Configuration

Create a config.properties file in the project root directory
Add the following properties:

```ini
weather.api.key=your_api_key
weather.api.base.url=http://api.weatherapi.com/v1
weather.api.current.endpoint=/current.json
weather.api.forecast.endpoint=/forecast.json
```

## 🚀 Installation

1. Clone the repository:
2. Navigate to the project directory:
``` 
cd weather-api-tests 
```
3. Install dependencies:
```
mvn clean install
```

## 🏃 Running Tests

#### Run all tests
```bash
mvn clean test
```

#### Run specific test groups

```bash
mvn clean test -Dgroups=smoke
```

## 🧪 Test Cases
The framework includes two main test categories:
#### 🌤 Current Weather Tests

* Validates response status code and response time
* Verifies location data accuracy
* Checks presence of essential weather parameters:
    * Last updated timestamp
    * Temperature
    * Weather condition
    * Wind speed
    * Humidity
* Ensures forecast data is not included in the response

#### 📅 Forecast Weather Tests

* Validates response status code and response time
* Verifies location data accuracy
* Confirms the presence of current weather data
* Validates forecast data structure
* Verifies the requested number of forecast days


#### 📊 Data Providers
The framework uses TestNG data providers to test multiple scenarios:

citiesProvider: Provides test data for current weather tests
forecastDataProvider: Provides test data for forecast weather tests

#### ⚠️ Error Handling

* Comprehensive error messages for failed assertions
* Runtime exception handling for configuration issues
* Detailed logging of API requests and responses


#### 💡 Best Practices

* Use of soft assertions for comprehensive test results
* Implementation of page object pattern for API client
* Centralized configuration management
* Parametrized tests for better test coverage
* Response time validation for performance monitoring





