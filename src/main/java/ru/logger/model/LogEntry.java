package ru.logger.model;

import java.time.LocalDateTime;

public class LogEntry {

    private final LocalDateTime timestamp;
    private final String user;
    private final Type type;
    private final double amount;
    private final String otherUser;
    private final String line;

    public LogEntry(LocalDateTime timestamp, String user, Type type, double amount, String otherUser, String line) {
        this.timestamp = timestamp;
        this.user = user;
        this.type = type;
        this.amount = amount;
        this.otherUser = otherUser;
        this.line = line;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public Type getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getOtherUser() {
        return otherUser;
    }

    public String getLine() {
        return line;
    }
}
