package com.example.supporter.Other;

public class Post {

    private String userName;
    private String imageURL;
    private String time;
    private String contentPost;
    private String picturePost;
    private String id;

    public Post(String userName, String imageURL, String time, String contentPost, String picturePost, String id) {
        this.userName = userName;
        this.imageURL = imageURL;
        this.time = time;
        this.contentPost = contentPost;
        this.picturePost = picturePost;
        this.id = id;
    }

    public Post() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContentPost() {
        return contentPost;
    }

    public void setContentPost(String contentPost) {
        this.contentPost = contentPost;
    }

    public String getPicturePost() {
        return picturePost;
    }

    public void setPicturePost(String picturePost) {
        this.picturePost = picturePost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
