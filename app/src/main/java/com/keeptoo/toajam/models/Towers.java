package com.keeptoo.toajam.models;

/**
 * Created by keeptoo on 11/16/2017.
 */

public class Towers {
    public String name;
    public String location;
    public String photourl;
    public double latitude;
    public double longitude;
    public String date_created;
    public int phone;
    public boolean verified;

    public Towers() {

    }

    public Towers(String name, String location, int phone, String photourl, double latitude, double longitude, String date_created, boolean verified) {
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.photourl = photourl;
        this.verified = verified;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date_created = date_created;
    }
}
