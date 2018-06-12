package com.keeptoo.toajam.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by keeptoo on 11/15/2017.
 */

//

@IgnoreExtraProperties
public class Updates {

    public String uid;
    public String author;
    public String body;
    public String photourl;
    public String date;
    public String postId;


    public long timestamp;
    public long numAppr = 0;
    public long numComments;
    public Map<String, Boolean> allappCounts = new HashMap<>();


    public Updates(String uid, String author, String date, String body, String photourl, String postId) {
        this.uid = uid;
        this.author = author;
        this.date = date;
        this.body = body;
        this.photourl = photourl;
        this.postId = postId;


    }

    public Updates() {

    }

    public Map<String, Boolean> getAllappCounts() {
        return allappCounts;
    }

    public long getNumAppr() {
        return numAppr;
    }

    public void setNumAppr(long numAppr) {
        this.numAppr = numAppr;
    }

    public long getNumComments() {
        return numComments;
    }

    public void setNumComments(long numComments) {
        this.numComments = numComments;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String title) {
        this.date = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("date", date);
        result.put("body", body);
        result.put("photourl", photourl);
        result.put("postId", postId);
        result.put("numAppr", numAppr);
        result.put("numComments", numComments);
        result.put("allappCounts", allappCounts);
        result.put("timestamp", timestamp);
        return result;
    }

}