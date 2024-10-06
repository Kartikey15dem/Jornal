package com.example.self;

import android.app.Application;

public class JournalApi extends Application {
    private String username;
    private String userid;
    private static JournalApi instance;

    public static synchronized JournalApi getInstance() {
        if (instance == null) {
            instance = new JournalApi();
        }
        return instance;
    }

        public JournalApi(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
