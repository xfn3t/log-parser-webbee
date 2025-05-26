package ru.logger.service;

import ru.logger.exception.ParseException;
import ru.logger.parser.LogParser;
import ru.logger.model.LogEntry;
import ru.logger.model.Type;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionService implements TransactionProcessor {

    private final List<String> invalidLines = new ArrayList<>();

    private final LogParser parser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Map<String, List<LogEntry>> userEntries = new HashMap<>();

    public TransactionService(LogParser parser) {
        this.parser = parser;
    }

    @Override
    public void process(String inputDir, String outDirectory) throws IOException {

        Path logsDir = Paths.get(inputDir);

        if (!Files.isDirectory(logsDir)) {
            throw new IllegalArgumentException("Input must be a outDirectory: " + inputDir);
        }

        Path outDir = logsDir.resolve(outDirectory);
        Files.createDirectories(outDir);

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(logsDir, "*.log")) {
            for (Path f : ds) {
                if (Files.isDirectory(f) || f.getFileName().toString().equals(outDirectory))
                    continue;
                processFile(f);
            }
        }
        writeOutputs(outDir);
    }

    private void processFile(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            while (line != null) {
                if (parser.correctLine(line)) {
                    try {
                        LogEntry entry = parser.parse(line);
                        record(entry);
                        if (entry.getType() == Type.TRANSFERRED) {
                            recordReceived(entry);
                        }
                    } catch (ParseException e) {
                        invalidLines.add(line);
                    }
                } else {
                    invalidLines.add(line);
                }
                line = reader.readLine();
            }
        }
    }

    private void record(LogEntry e) {
        userEntries.computeIfAbsent(e.getUser(), k -> new ArrayList<>()).add(e);
    }

    private void recordReceived(LogEntry e) {

        String to = e.getOtherUser();
        String ts = e.getTimestamp().format(formatter);
        String recvLine = String.format("[%s] %s received %.2f from %s", ts, to, e.getAmount(), e.getUser());

        LogEntry recv = new LogEntry(e.getTimestamp(), to, Type.RECEIVED, e.getAmount(), e.getUser(), recvLine);

        record(recv);
    }

    private void writeOutputs(Path outDir) throws IOException {

        for (var ent : userEntries.entrySet()) {

            List<LogEntry> logs = ent.getValue();
            logs.sort(Comparator.comparing(LogEntry::getTimestamp));

            double bal = calculateBalance(logs);
            Path out = outDir.resolve(ent.getKey() + ".log");

            try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {

                for (var le : logs) {
                    w.write(le.getLine());
                    w.newLine();
                }

                String now = LocalDateTime.now().format(formatter);

                w.write(String.format("[%s] %s final balance %.2f", now, ent.getKey(), bal));
                w.newLine();
            }
        }
    }

    private double calculateBalance(List<LogEntry> logs) {
        double balance = 0;
        for (var le : logs) {
            switch (le.getType()) {
                case INQUIRY -> balance = le.getAmount();
                case WITHDREW, TRANSFERRED -> balance -= le.getAmount();
                case RECEIVED -> balance += le.getAmount();
            }
        }
        return balance;
    }
}