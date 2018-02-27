package com.keeptoo.toajam.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String name;
    public String uid;
    public String email;
    public String country;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String uid, String email, String country) {
        this.name = name;
        this.uid = uid;
        this.email = email;
        this.country = country;
    }

}
// [END blog_user_class]
