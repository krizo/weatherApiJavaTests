package com.weatherapi.backend.listeners;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalDateTime.now().format(formatter))
            .append(" [").append(record.getLevel()).append("] ")
            .append(formatMessage(record))
            .append(System.lineSeparator());

        if (record.getThrown() != null) {
            sb.append("Exception details: \n");
            for (Throwable throwable : record.getThrown().getSuppressed()) {
                sb.append(throwable)
                    .append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
