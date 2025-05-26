package ru.logger.parser;

import ru.logger.model.LogEntry;

public interface LogParser {
    boolean correctLine(String line);
    LogEntry parse(String line) throws ru.logger.exception.ParseException;
}
