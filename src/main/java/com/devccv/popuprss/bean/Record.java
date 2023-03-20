package com.devccv.popuprss.bean;

public class Record {
    private String time;
    private String level;
    private String language;
    private String title;
    private String description;
    private String units;
    private String reward;
    private String link;

    public Record() {
    }

    public Record(String time, String level, String language, String title, String description, String units, String reward, String link) {
        this.time = time;
        this.level = level;
        this.language = language;
        this.title = title;
        this.description = description;
        this.units = units;
        this.reward = reward;
        this.link = link;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
