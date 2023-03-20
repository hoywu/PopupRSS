package com.devccv.popuprss.util;

import com.devccv.popuprss.controller.LogsViewController;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class CSV {
    private static final Path archivedCSV = Path.of("archived/archived.csv");

    public static void readArchived() {
        checkFileExists();
        int counter = 0;
        try (Scanner scanner = new Scanner(new FileInputStream(archivedCSV.toFile()), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine() && counter++ < 1000) {
                String[] data = scanner.nextLine().split(",");
            }
        } catch (Exception e) {
            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_read_archived_error"));
        }
    }

    public static void appendArchived(String data) {
        checkFileExists();
        try (OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(archivedCSV.toFile(), true), StandardCharsets.UTF_8)) {
            output.append(data);
        } catch (IOException e) {
            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_append_archived_error"));
        }
    }

    private static void checkFileExists() {
        if (!Files.exists(archivedCSV)) {
            try {
                if (!Files.exists(archivedCSV.getParent())) {
                    Files.createDirectories(archivedCSV.getParent());
                }
                Files.createFile(archivedCSV);
            } catch (IOException e) {
                LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_create_archived_error"));
            }
        }
    }
}
