package com.devccv.popuprss;

import com.devccv.popuprss.thread.RSSMonitorThread;
import com.devccv.popuprss.util.ResourceBundleUtil;
import com.devccv.popuprss.util.Utils;

import java.util.Scanner;

public class AppCli {
    private static RSSMonitorThread RSS_MONITOR_THREAD = null;

    public static void main(String[] args) {
        App.initSetting(false);
        System.out.println(ResourceBundleUtil.getStringValue("log_default"));
        System.out.println("=".repeat(50));
        showHelpMessage();

        Scanner in = new Scanner(System.in);
        String input;
        while (true) {
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
                case "quit", "q" -> System.exit(0);
                default -> showHelpMessage();
            }
        }
    }

    public static void showHelpMessage() {
        System.out.println(ResourceBundleUtil.getStringValue("cli_header"));
        System.out.println(ResourceBundleUtil.getStringValue("cli_help"));
        System.out.println(ResourceBundleUtil.getStringValue("cli_start"));
        System.out.println(ResourceBundleUtil.getStringValue("cli_quit"));
        System.out.println("=".repeat(50));
        System.out.print("> ");
    }
}
