package com.devccv.popuprss.util;

import com.devccv.popuprss.bean.Record;
import com.devccv.popuprss.controller.LogsViewController;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CSV {
    private static final Path archivedCSV = Path.of("archived/archived.csv");

    public static List<Record> readArchived() {
        checkFileExists();
        List<Record> list = new LinkedList<>();
        int counter = 0;
        try (Scanner scanner = new Scanner(new FileInputStream(archivedCSV.toFile()), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine() && counter++ < 1000) {
                String[] data = scanner.nextLine().split(",");
                list.add(new Record(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]));
            }
        } catch (Exception e) {
            LogsViewController.newLog(ResourceBundleUtil.getStringValue("log_read_archived_error"));
        }
        return list;
    }

    public static void appendArchived(Record record) {
        checkFileExists();
        try (OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(archivedCSV.toFile(), true), StandardCharsets.UTF_8)) {
            String data = record.getTime() + "," + record.getLevel() + "," + record.getLanguage() + "," + record.getTitle() + "," + record.getDescription() + "," + record.getUnits() + "," + record.getReward() + "," + record.getLink();
            output.append(data).append("\n");
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
