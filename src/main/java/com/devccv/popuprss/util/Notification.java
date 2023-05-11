package com.devccv.popuprss.util;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public final class Notification {
    public static boolean BrowserOpenLink(String link) throws IOException {
        if (Desktop.isDesktopSupported()) {
            URI uri = URI.create(link);
            Desktop dp = Desktop.getDesktop();
            if (dp.isSupported(Desktop.Action.BROWSE)) {
                dp.browse(uri);
                return true;
            }
        }
        return false;
    }

    public static boolean PushSystemNotify(String title, String text) throws IOException {
        if (SystemTray.isSupported()) {
            SystemTray.getSystemTray().getTrayIcons()[0].displayMessage(title, text, TrayIcon.MessageType.INFO);
            return true;
        }
        return false;
    }
}
