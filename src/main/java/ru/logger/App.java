package ru.logger;

import ru.logger.parser.factory.ParserFactory;
import ru.logger.service.TransactionService;

import java.io.IOException;

public class App {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: java Application <logs-dir>");
            System.exit(1);
        }

        String outDirectory = "transactions_by_users";
        TransactionService service = new TransactionService(ParserFactory.createParser());

        try {
            service.process(args[0], outDirectory);
            System.out.println("Processing complete.");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
