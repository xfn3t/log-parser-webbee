package ru.logger.parser.factory;

import ru.logger.parser.LogParser;
import ru.logger.parser.impl.SimpleLogParser;

public class ParserFactory {
    public static LogParser createParser() {
        return new SimpleLogParser();
    }
}
