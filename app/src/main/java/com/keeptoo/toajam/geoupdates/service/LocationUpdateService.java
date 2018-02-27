package com.keeptoo.toajam.geoupdates.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.authetication.SessionManager;
import com.keeptoo.toajam.geoupdates.activities.MapActivity;
import com.keeptoo.toajam.models.Notes;
import com.keeptoo.toajam.utils.InteractionUtils;

public class LocationUpdateService extends Service {

    SharedPreferences settingsPref;
    SessionManager sessionManager;

    public LocationUpdateService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sessionManager = new SessionManager(getApplicationContext());
        // show notification

        final DatabaseReference noteReference = FirebaseDatabase.getInstance().getReference().child("notes").child(sessionManager.getCountry());

        final InteractionUtils interactionUtils = new InteractionUtils();

        Query items = noteReference.orderByChild("timestamp").limitToLast(1);
        items.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //note information

                for (DataSnapshot items : snapshot.getChildren()) {
                    final Notes values = items.getValue(Notes.class);
                    //show notification after 60 seconds
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (values != null) {
                                final boolean firstime = interactionUtils.getFirstTimeRun(getApplicationContext(), values.date_created);
                                if (firstime == true) {
                                    Log.e(getClass().getName(), "is showing");
                                    showNotif(values.name, values.location, values.note);
                                    interactionUtils.storeFirstTimeRun(getApplicationContext(), values.date_created);
                                }
                                Log.e(getClass().getName(), "done showing");

                            }
                        }

                    }, 30 * 1000);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void showNotif(String name, String place, String note)

    {
        if (settingsPref.getBoolean("notifications_new_update", true)) {
            //push a notification
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext(), null);

            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_action_notif)
                    .setTicker("Hearty365")
                    .setContentTitle("ToaJam : Traffic Update")
                    .setContentText(name + " at " + place + " Says :" + note)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent)
                    .setContentInfo("Update");


            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, b.build());
        }

    }

}
