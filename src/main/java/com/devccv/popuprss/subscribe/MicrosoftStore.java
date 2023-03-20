package com.devccv.popuprss.subscribe;

public class MicrosoftStore implements Subscribe {
    public boolean isSubscribed() {
        return true;
    }

    public String getSubscribeValidity() {
        return "2024/03/01";
    }
}
