package com.keeptoo.toajam.utils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FConstants {

    public static final String UPDATES_KEY = "updates";
    public static final String NUM_APPRECIATIONS_KEY = "numAppr";
    public static final String UPDATES_APPRECIATED_KEY = "updates_appreciated";
    public static final String UPDATES_IMAGES = "updates_images";
    public static final String MY_UPDATES = "my_updates";
    public static final String EXTRA_UPDATE = "update";
    public static final String COMMENTS_KEY = "comments";
    public static final String USER_RECORD = "user_record";
    public static final String USERS_KEY = "users";
    public static final String NUM_COMMENTS_KEY = "numComments";

    public static final String COMPLETE_DATE() {

        DateFormat df = new SimpleDateFormat("EEEE dd MMMM yyyy hh:mm aaa", Locale.getDefault());
        return df.format(new Date());

    }
}
