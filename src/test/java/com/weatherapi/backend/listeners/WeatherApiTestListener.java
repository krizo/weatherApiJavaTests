package com.weatherapi.backend.listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

public class WeatherApiTestListener implements ITestListener, ISuiteListener {
    public static final Logger LOGGER = Logger.getLogger(WeatherApiTestListener.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");


    static {
        try {
            cleanLogs();
            String timestamp = LocalDateTime.now().format(fileFormatter);
            FileHandler fileHandler = new FileHandler(
                    String.format("logs/test-execution_%s.log", timestamp),
                    5242880, // 5MB file size
                    1,       // 3 rotating files
                    false    // append mode
            );
            fileHandler.setFormatter(new CustomLogFormatter());
            LOGGER.addHandler(fileHandler);

            for (Handler handler : LOGGER.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    LOGGER.removeHandler(handler);
                }
            }
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new CustomLogFormatter());
            LOGGER.addHandler(consoleHandler);
            LOGGER.setLevel(Level.ALL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(ISuite suite) {
        LOGGER.info(String.format("=== Starting Test Suite: %s at %s ===",
                suite.getName(),
                LocalDateTime.now().format(formatter)));
    }

    @Override
    public void onFinish(ISuite suite) {
        LOGGER.info(String.format("=== Finished Test Suite: %s at %s ===",
                suite.getName(),
                LocalDateTime.now().format(formatter)));
    }


    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info(() -> String.format("Starting test: %s [%s]",
                result.getName(), String.join(", ", result.getMethod().getGroups())));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logTestResult(result, "PASSED", Level.INFO);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logTestResult(result, "FAILED", Level.SEVERE);
        if (result.getThrowable() != null) {
            LOGGER.severe(() -> String.format("Failure Details: %s", result.getThrowable()));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logTestResult(result, "SKIPPED", Level.WARNING);
    }

    private void logTestResult(ITestResult result, String status, Level level) {
        StringBuilder logMessage = new StringBuilder();
        long duration = result.getEndMillis() - result.getStartMillis();
        logMessage.append(String.format("\tTest %s: %s%n", status, result.getName()));
        logMessage.append(String.format("\tDuration: %dms%n", duration));
        String description = result.getMethod().getDescription();
        if (description != null && !description.isEmpty()) {
            logMessage.append(String.format("\tDescription: %s%n", description));
        }
        // Log test class name
        logMessage.append(String.format("\tTest Class: %s%n", result.getTestClass().getName()));
        String[] groups = result.getMethod().getGroups();
        if (groups != null && groups.length > 0) {
            logMessage.append(String.format("\tGroups: %s%n", String.join(", ", groups)));
        }

        // Log parameters with their types and values
        // Log parameters with their values and mark failed parameters if test failed
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            logMessage.append("Test Parameters:\n");

            // If test failed and we have data from assertAll(), analyze which parameters were involved
            boolean hasFailedAssertions = false;
            Set<String> failedParamValues = new HashSet<>();

            if (result.getThrowable() instanceof AssertionError && result.getThrowable().getMessage() != null) {
                String errorMessage = result.getThrowable().getMessage();
                hasFailedAssertions = true;

                // Extract parameter values from error message
                for (Object param : parameters) {
                    if (param != null && errorMessage.contains(param.toString())) {
                        failedParamValues.add(param.toString());
                    }
                }
            }

            // Log each parameter, marking those involved in failures
            for (int i = 0; i < parameters.length; i++) {
                Object param = parameters[i];
                String paramType = param != null ? param.getClass().getSimpleName() : "null";
                String paramValue = String.valueOf(param);
                String failMarker = hasFailedAssertions && failedParamValues.contains(paramValue) ? " [FAILED]" : "";

                logMessage.append(String.format("  %d. [%s] %s%s%n",
                        i + 1,
                        paramType,
                        paramValue,
                        failMarker));
            }
        }

        // Log the error details if test failed
        if (status.equals("FAILED") && result.getThrowable() != null) {
            logMessage.append("\nFailure Details:\n");
            logMessage.append(result.getThrowable().getMessage()).append("\n");

            // Add stack trace for debug level
            StringWriter sw = new StringWriter();
            result.getThrowable().printStackTrace(new PrintWriter(sw));
            LOGGER.fine(() -> "Stack trace:\n" + sw.toString());
        }

        LOGGER.log(level, logMessage.toString());
    }

    private static void cleanLogs() {
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdir();
        }

        File[] files = logsDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    System.err.println("Failed to delete file: " + file.getName());
                }
            }
        }
    }
}

