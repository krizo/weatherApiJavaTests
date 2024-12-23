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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

public class WeatherApiTestListener implements ITestListener, ISuiteListener {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final Map<String, Logger> testLoggers = new HashMap<>();

    static {
        cleanLogs();
    }

    private Logger createLoggerForTest(ITestResult result) throws IOException {
        // Create a unique identifier for the test based on method name and parameters
        String testIdentifier = createTestIdentifier(result);

        // Create a new logger for this specific test
        Logger logger = Logger.getLogger(testIdentifier);
        logger.setUseParentHandlers(false);

        // Create logs directory if it doesn't exist
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdir();
        }

        // Create file handler for this test
        String timestamp = LocalDateTime.now().format(fileFormatter);
        FileHandler fileHandler = new FileHandler(
                String.format("logs/%s_%s.log", testIdentifier, timestamp),
                5242880, // 5MB file size
                1,       // 1 file
                false    // don't append
        );
        fileHandler.setFormatter(new CustomLogFormatter());
        logger.addHandler(fileHandler);

        // Add console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new CustomLogFormatter());
        logger.addHandler(consoleHandler);

        logger.setLevel(Level.ALL);

        return logger;
    }

    private String createTestIdentifier(ITestResult result) {
        StringBuilder identifier = new StringBuilder();
        identifier.append(result.getMethod().getMethodName());

        // Add parameters to the identifier
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            identifier.append("_").append(
                String.join("_",
                    java.util.Arrays.stream(params)
                        .map(Object::toString)
                        .map(s -> s.replaceAll("\\s+", "_"))
                        .toArray(String[]::new)
                )
            );
        }

        return identifier.toString();
    }

    @Override
    public void onStart(ISuite suite) {
        // Each test will have its own logger, so we don't need a suite-level logger
    }

    @Override
    public void onFinish(ISuite suite) {
        // Clean up all loggers
        testLoggers.values().forEach(logger -> {
            for (Handler handler : logger.getHandlers()) {
                handler.close();
                logger.removeHandler(handler);
            }
        });
        testLoggers.clear();
    }

    @Override
    public void onTestStart(ITestResult result) {
        try {
            String testIdentifier = createTestIdentifier(result);
            Logger logger = createLoggerForTest(result);
            testLoggers.put(testIdentifier, logger);

            logger.info(() -> String.format("=== Starting Test: %s [%s] ===",
                    result.getName(),
                    String.join(", ", result.getMethod().getGroups())));
        } catch (IOException e) {
            System.err.println("Failed to create logger for test: " + e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logTestResult(result, "PASSED", Level.INFO);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logTestResult(result, "FAILED", Level.SEVERE);
        Logger logger = getLoggerForTest(result);
        if (result.getThrowable() != null) {
            logger.severe(() -> String.format("Failure Details: %s", result.getThrowable()));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logTestResult(result, "SKIPPED", Level.WARNING);
    }

    private Logger getLoggerForTest(ITestResult result) {
        return testLoggers.get(createTestIdentifier(result));
    }

    private void logTestResult(ITestResult result, String status, Level level) {
        Logger logger = getLoggerForTest(result);
        if (logger == null) {
            System.err.println("Logger not found for test: " + result.getName());
            return;
        }

        StringBuilder logMessage = new StringBuilder();
        long duration = result.getEndMillis() - result.getStartMillis();
        logMessage.append(String.format("\tTest %s: %s%n", status, result.getName()));
        logMessage.append(String.format("\tDuration: %dms%n", duration));

        String description = result.getMethod().getDescription();
        if (description != null && !description.isEmpty()) {
            logMessage.append(String.format("\tDescription: %s%n", description));
        }

        logMessage.append(String.format("\tTest Class: %s%n", result.getTestClass().getName()));

        String[] groups = result.getMethod().getGroups();
        if (groups != null && groups.length > 0) {
            logMessage.append(String.format("\tGroups: %s%n", String.join(", ", groups)));
        }

        // Log parameters
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            logMessage.append("Test Parameters:\n");
            Set<String> failedParamValues = new HashSet<>();

            if (status.equals("FAILED") && result.getThrowable() instanceof AssertionError
                    && result.getThrowable().getMessage() != null) {
                String errorMessage = result.getThrowable().getMessage();
                for (Object param : parameters) {
                    if (param != null && errorMessage.contains(param.toString())) {
                        failedParamValues.add(param.toString());
                    }
                }
            }

            for (int i = 0; i < parameters.length; i++) {
                Object param = parameters[i];
                String paramType = param != null ? param.getClass().getSimpleName() : "null";
                String paramValue = String.valueOf(param);
                String failMarker = failedParamValues.contains(paramValue) ? " [FAILED]" : "";

                logMessage.append(String.format("  %d. [%s] %s%s%n",
                        i + 1, paramType, paramValue, failMarker));
            }
        }

        // Log failure details
        if (status.equals("FAILED") && result.getThrowable() != null) {
            logMessage.append("\nFailure Details:\n");
            logMessage.append(result.getThrowable().getMessage()).append("\n");

            StringWriter sw = new StringWriter();
            result.getThrowable().printStackTrace(new PrintWriter(sw));
            logger.fine(() -> "Stack trace:\n" + sw.toString());
        }

        logger.log(level, logMessage.toString());
    }

    private static void cleanLogs() {
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdir();
            return;
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