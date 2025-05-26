package ru.logger.parser.impl;

import ru.logger.exception.ParseException;
import ru.logger.parser.LogParser;
import ru.logger.model.LogEntry;
import ru.logger.model.Type;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class SimpleLogParser implements LogParser {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Set<String> OPS = Set.of("balance", "transferred", "withdrew");

    @Override
    public boolean correctLine(String line) {
        if (!line.startsWith("["))
            return false;

        int end = line.indexOf("]");

        if (end < 0)
            return false;

        String[] parts = line.substring(end + 2).split(" ");
        return parts.length >= 2 && OPS.contains(parts[1]);
    }

    @Override
    public LogEntry parse(String line) throws ParseException {

        int end = line.indexOf("]");
        String ts = line.substring(1, end);
        LocalDateTime timestamp;

        try {
            timestamp = LocalDateTime.parse(ts, formatter);
        } catch (Exception e) {
            throw new ParseException("Invalid timestamp");
        }

        String[] parts = line.substring(end + 2).split(" ");
        String user = parts[0];
        String op = parts[1];
        String other = null;

        Type type;
        double amount;

        switch (op) {
            case "balance" -> {
                type = Type.INQUIRY;
                amount = Double.parseDouble(parts[3]);
            }
            case "transferred" -> {
                type = Type.TRANSFERRED;
                amount = Double.parseDouble(parts[2]);
                other = parts[4];
            }
            case "withdrew" -> {
                type = Type.WITHDREW;
                amount = Double.parseDouble(parts[2]);
            }
            default -> throw new ParseException("Unsupported op: " + op);
        }
        return new LogEntry(timestamp, user, type, amount, other, line);
    }
}