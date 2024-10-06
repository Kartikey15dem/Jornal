package com.example.self;

import com.google.firebase.Timestamp;

public class Journal {
   private String title;
   private String thought;
   private String imageUrl;
   private String userId;
   private Timestamp timeAdded;
   private String userName;

   public Journal(){}   //mandatory for firestore

    public Journal(String title, String imageUrl, String thought, String userId, Timestamp timeAdded, String userName) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.thought = thought;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.userName = userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
