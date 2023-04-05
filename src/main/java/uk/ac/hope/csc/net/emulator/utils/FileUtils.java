package uk.ac.hope.csc.net.emulator.utils;

import uk.ac.hope.csc.net.emulator.as.AutonomousSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    public static void loadUndirectedNetworkFromCsv(AutonomousSystem as, String path) throws FileNotFoundException {

        // Read CSV lines into a List
        List<String> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(path));) {
            while (scanner.hasNextLine()) {
                records.add(scanner.nextLine());
            }
        }

        // Iterate the list and add edges into the AS
        for(String line : records) {
            String[] values = line.split(",");
            long r1 = Long.parseLong(values[0]);
            long r2 = Long.parseLong(values[1]);
            long cost = Long.parseLong(values[2]);
            as.addlink(r1, r2, cost);
        }
    }
}
