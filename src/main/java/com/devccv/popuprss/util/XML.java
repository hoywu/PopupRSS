package com.devccv.popuprss.util;

import com.devccv.popuprss.App;
import com.devccv.popuprss.bean.Record;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class XML {
    public static List<Record> parseRecord(String xml) {
        ArrayList<Record> list = new ArrayList<>();
        String fullTitle;
        String time;
        String level;
        String language;
        String title;
        String description;
        String units;
        String reward;
        String link;

        String[] items = xml.split("<item>"); //从index为1开始为有效数据
        for (int i = 1; i < items.length; i++) {
            time = items[i].split("<pubDate>")[1].split("</pubDate>")[0];
            fullTitle = items[i].split("<title>")[1].split("</title>")[0];
            String[] titleSplit = fullTitle.split("\\|");
            level = titleSplit[0].trim().replace("(", "").replace(")", "");
            language = titleSplit[titleSplit.length - 1].trim();
            title = titleSplit[1].trim();
            description = title; //todo:临时
            units = titleSplit[2].replace("chars", "").trim();
            reward = titleSplit[3].replace("Reward: US", "").trim();
            link = items[i].split("<link>")[1].split("</link>")[0];

            try {
                time = App.DATE_TIME_FORMATTER.format(LocalDateTime.parse(time, DateTimeFormatter.RFC_1123_DATE_TIME));
            } catch (Exception ignored) {
            }

            list.add(new Record(fullTitle, time, level, language, title, description, units, reward, link));
        }

        return list;
    }
}
