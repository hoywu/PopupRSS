package com.devccv.popuprss;

import com.devccv.popuprss.thread.RSSMonitorThread;
import com.devccv.popuprss.util.ConfigManager;
import com.devccv.popuprss.util.Encrypt;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.util.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AppCli {
    private static RSSMonitorThread RSS_MONITOR_THREAD = null;
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        App.initSetting(false);
        System.out.println(ResourceBundleUtil.getStringValue("log_default"));

        String input;
        while (true) {
            showHelpMessage();
            input = in.nextLine();
            switch (input) {
                case "start", "s" -> {
                    if (RSS_MONITOR_THREAD == null) {
                        try {
                            RSS_MONITOR_THREAD = new RSSMonitorThread(Utils.getProxyInstance(), Utils.getURLInstance());
                            RSS_MONITOR_THREAD.start();
                        } catch (Exception e) {
                            System.out.println(ResourceBundleUtil.getStringValue("cli_start_failed"));
                        }
                    } else {
                        System.out.println(ResourceBundleUtil.getStringValue("cli_start_already"));
                    }
                }
                case "pause", "p" -> {
                    if (RSS_MONITOR_THREAD != null) {
                        RSS_MONITOR_THREAD.interrupt();
                        RSS_MONITOR_THREAD = null;
                    } else {
                        System.out.println(ResourceBundleUtil.getStringValue("cli_pause_already"));
                    }
                }
                case "config", "c" -> showConfig();
                case "quit", "q" -> System.exit(0);
                default -> {
                }
            }
        }
    }

    public static void showHelpMessage() {
        String[] helps = {"cli_header", "cli_help", "cli_start", "cli_pause", "cli_editConfig", "cli_quit"};
        System.out.println("=".repeat(50));
        for (String help : helps) {
            System.out.println(ResourceBundleUtil.getStringValue(help));
        }
        System.out.println("=".repeat(50));
        System.out.print("> ");
    }

    public static void showConfig() {
        System.out.println("=".repeat(50));
        System.out.print(ResourceBundleUtil.getStringValue("cli_config_link") + ": ");
        try {
            System.out.println(Encrypt.decryptWithUserName(ConfigManager.CONFIG.getRssLink()));
        } catch (Exception e) {
            System.out.println(ResourceBundleUtil.getStringValue("log_decryption_error"));
        }
        System.out.println("=".repeat(50));
        System.out.println(ResourceBundleUtil.getStringValue("cli_config_help"));
        System.out.print("> ");
        String input = in.nextLine();
        switch (input) {
            case "q" -> {
                return;
            }
            default -> {
                try {
                    String encrypt = Encrypt.encryptWithUserName(input);
                    ConfigManager.CONFIG.setRssLink(encrypt);
                    ConfigManager.saveConfig();
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    System.out.println(ResourceBundleUtil.getStringValue("log_encryption_error"));
                }
            }
        }
    }
}
