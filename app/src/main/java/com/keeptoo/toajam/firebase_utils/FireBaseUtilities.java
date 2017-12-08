package com.keeptoo.toajam.firebase_utils;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.home.HomeActivity;
import com.keeptoo.toajam.models.Updates;
import com.keeptoo.toajam.utils.FConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by keeptoo on 11/06/2017.
 */

public class FireBaseUtilities extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    /// insert db


    public void writeNewPost(String userId, String username, String date, String body, String photourl, final Context context) {


        String postId = database.getReference().child("updates").child(HomeActivity.Country).push().getKey();
        final Updates post = new Updates(userId, username, date, body, photourl, postId);
        Map<String, Object> postValues = post.toMap();

        //extras
        postValues.put("timestamp", ServerValue.TIMESTAMP);


        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(postId, postValues);
        Log.e(getClass().getName(), childUpdates.toString());

        database.getReference().child("updates").child(HomeActivity.Country).updateChildren(childUpdates, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Log.e(getClass().getName(), "Posting failed: ", databaseError.toException());
                } else if (databaseError == null) {

                    Toast.makeText(context, "Successful Post", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }


    //likes

    public void onAppreciated(final String postId) {
        FirebaseUtils.getPostLikedRef(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            //User liked
                            FirebaseUtils.getPostRef()
                                    .child(postId)
                                    .child(FConstants.NUM_APPRECIATIONS_KEY)
                                    .runTransaction(new Transaction.Handler() {
                                        @Override
                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                            long num = (long) mutableData.getValue();
                                            mutableData.setValue(num - 1);
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                            FirebaseUtils.getPostLikedRef(postId)
                                                    .setValue(null);
                                        }
                                    });
                        } else {
                            FirebaseUtils.getPostRef()
                                    .child(postId)
                                    .child(FConstants.NUM_APPRECIATIONS_KEY)
                                    .runTransaction(new Transaction.Handler() {
                                        @Override
                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                            long num = (long) mutableData.getValue();
                                            mutableData.setValue(num + 1);
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                            FirebaseUtils.getPostLikedRef(postId).setValue(true);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    // [START post_allappCounts_transaction]
    public void onAppClicked(DatabaseReference postRef) {


        postRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Log.e(getClass().getName(), "Transaction: " + mutableData.getValue(Updates.class).getPostId());
                Updates p = mutableData.getValue(Updates.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                if (p.allappCounts.containsKey(user.getUid())) {
                    p.numAppr = p.numAppr - 1;
                    p.allappCounts.remove(user.getUid());

                } else {
                    p.numAppr = p.numAppr + 1;
                    p.allappCounts.put(user.getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                if (databaseError != null) {
                    Log.d(getClass().getName(), "postTransaction:onComplete:" + databaseError.getMessage());
                }
            }
        });
    }
}
