package com.keeptoo.toajam.home.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.home.activities.CommentActivity;
import com.keeptoo.toajam.home.activities.HomeActivity;
import com.keeptoo.toajam.models.Updates;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by keeptoo on 11/04/2017.
 */

public class UpdatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Updates> updates;
    public ArrayList<Updates> mFilteredList;

    public FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    Context context;


    public UpdatesAdapter(ArrayList<Updates> update) {

        this.updates = update;
        this.mFilteredList = update;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doUpdatesCleanUp();
            }
        }, 5000);
        context = parent.getContext();
        return new VHUpdates(LayoutInflater.from(context).inflate(R.layout.updates_item, parent, false));

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        Log.e(getClass().getName(), "Adding item at pos: " + position);
        final Updates updats = updates.get(position);
        final VHUpdates vu = (VHUpdates) holder;
        String firsName = updats.getAuthor();
        if (firsName.contains(" ")) {
            firsName = firsName.substring(0, firsName.indexOf(" "));
            vu.tv_tittle.setText(firsName);

        }
        vu.tv_info.setText(updats.getBody());


        String textCom = updats.getDate() + " | " + 0 + " comments";
        vu.tv_date.setText(textCom);


        //get comment count

        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("updates-comments").child(HomeActivity.Country);

        firebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                try {
                    String textCom = updats.getDate() + " | " + dataSnapshot.child(updats.getPostId()).getChildrenCount() + " comments";
                    vu.tv_date.setText(textCom);
                } catch (Exception e) {

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (updats.getNumAppr() >= 1) {
            vu.tv_appreciations.setText(String.valueOf(updats.getNumAppr()));
            if (updats.allappCounts.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                vu.iv_appreciations.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.liked));

            } else
                vu.iv_appreciations.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like));

        } else
            //zero appreciations of update
            vu.tv_appreciations.setText("0");


        try {
            if (updats.getPhotourl().startsWith("http")) {

                Picasso.with(context).load(updats.getPhotourl()).into(vu.iv_up_userimg);

            } else {
                vu.iv_up_userimg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_icon_placeholder));

            }


        } catch (Exception e) {

        }

        vu.ly_clickzone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_POST_KEY, updats.getPostId());
                    context.startActivity(intent);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });


    }


    private void doUpdatesCleanUp() {

        //------clear updates after 10 days---------------


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("updates").child(HomeActivity.Country);
        final DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("updates-comments").child(HomeActivity.Country);

        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(10, TimeUnit.DAYS);


        Query oldItems = reference.orderByChild("timestamp").endAt(cutoff);


        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {

                    //delete old items

                 /*   itemSnapshot.getRef().removeValue();
                    Log.e(getClass().getName(), "Update Remove Old Items: " + itemSnapshot.getKey());
                    if (commentsRef != null) {
                        commentsRef.child(itemSnapshot.getKey()).removeValue();

                    }*/

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //--------------end clear---------------

    }


    @Override
    public int getItemCount() {

        return updates.size();
    }


    //filter the adapter
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mFilteredList = updates;
                } else {
                    ArrayList<Updates> filteredList = new ArrayList<>();
                    for (Updates row : updates) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getBody().toLowerCase().contains(charString.toLowerCase()) || row.getAuthor().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<Updates>) filterResults.values;
                updates = (ArrayList<Updates>) filterResults.values;
                Log.e(getClass().getName(), " Original List " + updates.size() + " Filtered list: " + mFilteredList.size());
                UpdatesAdapter.this.notifyDataSetChanged();
            }
        };
    }


    public class VHUpdates extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_updatename)
        TextView tv_tittle;
        @BindView(R.id.txt_update_desc)
        TextView tv_info;
        @BindView(R.id.txt_updates_date_coms)
        TextView tv_date;
        @BindView(R.id.txt_updates_appreciations)
        TextView tv_appreciations;
        @BindView(R.id.iv_updates_appreciation)
        ImageView iv_appreciations;
        @BindView(R.id.iv_up_img)
        CircleImageView iv_up_userimg;
        @BindView(R.id.ly_clickzone)
        LinearLayout ly_clickzone;


        public VHUpdates(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

    }

}
