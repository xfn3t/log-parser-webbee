package ru.logger.service;

import java.io.IOException;

public interface TransactionProcessor {
    void process(String inputDir, String outDir) throws IOException;
}