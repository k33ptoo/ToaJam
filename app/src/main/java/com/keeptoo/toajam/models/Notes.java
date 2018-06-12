package com.keeptoo.toajam.models;

/**
 * Created by keeptoo on 12/01/2017.
 */

public class Notes {


    public String name;
    public String location;
    public String date_created;
    public String photourl;
    public String note;
    public double latitude;
    public double longitude;

    public Notes() {

    }

    public Notes(String name, String location, String date_created, String note, String photourl, double latitude, double longitude) {
        this.name = name;
        this.location = location;
        this.date_created = date_created;
        this.photourl = photourl;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
