package com.keeptoo.toajam.firebase_utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.keeptoo.toajam.utils.FConstants;

import java.util.ArrayList;

/**
 * Created by brad on 2017/02/05.
 */

public class FirebaseUtils {
    //I'm creating this class for similar reasons as the Constants class, and to make my code a bit
    //cleaner and more well managed.

    public static DatabaseReference getUserRef(String email){
        return FirebaseDatabase.getInstance()
                .getReference(FConstants.USERS_KEY)
                .child(email);
    }

    public static DatabaseReference getPostRef(){
        return FirebaseDatabase.getInstance()
                .getReference(FConstants.UPDATES_KEY);
    }

    public static DatabaseReference getPostLikedRef(){
        return FirebaseDatabase.getInstance()
                .getReference(FConstants.UPDATES_APPRECIATED_KEY);
    }

    public static DatabaseReference getPostLikedRef(String postId){
        return getPostLikedRef().child(getCurrentUser().getEmail()
        .replace(".",","))
                .child(postId);
    }

    public static FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getUid(){
        String path = FirebaseDatabase.getInstance().getReference().push().toString();
        return path.substring(path.lastIndexOf("/") + 1);
    }

   /* public static FirebaseDatabase getImageSRef(){
        return FirebaseDatabase.getInstance().getReference(FConstants.UPDATES_IMAGES);
    }
*/
    public static DatabaseReference getMyPostRef(){
        return FirebaseDatabase.getInstance().getReference(FConstants.MY_UPDATES)
                .child(getCurrentUser().getEmail().replace(".",","));
    }

    public static DatabaseReference getCommentRef(String postId){
        return FirebaseDatabase.getInstance().getReference(FConstants.COMMENTS_KEY)
                .child(postId);
    }

    public static DatabaseReference getMyRecordRef(){
        return FirebaseDatabase.getInstance().getReference(FConstants.USER_RECORD)
                .child(getCurrentUser().getEmail().replace(".",","));
    }

    public static void addToMyRecord(String node, final String id){
        getMyRecordRef().child(node).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> myRecordCollection;
                if(mutableData.getValue() == null){
                    myRecordCollection = new ArrayList<String>(1);
                    myRecordCollection.add(id);
                }else{
                    myRecordCollection = (ArrayList<String>) mutableData.getValue();
                    myRecordCollection.add(id);
                }

                mutableData.setValue(myRecordCollection);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

}
