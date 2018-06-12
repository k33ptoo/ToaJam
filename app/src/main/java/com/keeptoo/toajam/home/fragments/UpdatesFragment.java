package com.keeptoo.toajam.home.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.authetication.SessionManager;
import com.keeptoo.toajam.firebase_utils.FireBaseUtilities;
import com.keeptoo.toajam.home.activities.CommentActivity;
import com.keeptoo.toajam.home.activities.HomeActivity;
import com.keeptoo.toajam.home.adapters.UpdatesAdapter;
import com.keeptoo.toajam.models.Updates;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keeptoo on 11/04/2017.
 */

public class UpdatesFragment extends Fragment {

    @BindView(R.id.rv_updates)
    RecyclerView recyclerView;

    @BindView(R.id.swiperefresh_updates)
    SwipeRefreshLayout swipeLayout;

    FirebaseDatabase database;
    UpdatesAdapter adapter;
    ArrayList<Updates> updates1;
    SessionManager sessionManager;
    View view;
    private DatabaseReference myRef;


    public UpdatesFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getActivity().getApplicationContext());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_updates, container, false);
        ButterKnife.bind(this, view);

        initViews();
        swipeLayout.setColorSchemeColors(
                Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            initViews();
            swipeLayout.setRefreshing(false);
        }, 2000));


        return view;
    }


    private void initViews() {


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        recyclerView = view.findViewById(R.id.rv_updates);
        recyclerView.setLayoutManager(layoutManager);
        updates1 = new ArrayList<>();
        adapter = new UpdatesAdapter(updates1);
        recyclerView.setAdapter(adapter);
        database = FirebaseDatabase.getInstance();

        //specify updates
        myRef = database.getReference("updates").child(sessionManager.getCountry());


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //reinitialize
                ArrayList<Updates> newUpdates = new ArrayList<>();
                for (DataSnapshot items : dataSnapshot.getChildren()) {

                    Updates value = items.getValue(Updates.class);
                    //add items
                    newUpdates.add(value);


                }
                adapter.updates = newUpdates;

                //update filters
                adapter.mFilteredList = newUpdates;
                updates1 = newUpdates;

                adapter.notifyDataSetChanged();
                if (newUpdates.size() > 0) {
                    // recyclerView.smoothScrollToPosition(newUpdates.size() - 1);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener()

        {
            GestureDetector gestureDetector = new GestureDetector(getActivity().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {

                    // appreciated update
                    View v = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    final Updates updats = adapter.updates.get(recyclerView.getChildAdapterPosition(v));

                    // ----------appeciation------------------
                    final ImageView iv_apps = recyclerView.findViewHolderForAdapterPosition(recyclerView.getChildAdapterPosition(v)).itemView.findViewById(R.id.iv_updates_appreciation);
                    iv_apps.setOnClickListener(view -> {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("updates").child(HomeActivity.Country).child(updats.getPostId());
                        new FireBaseUtilities().onAppClicked(reference);


                    });
                    // ---------- end appeciation ----------------------


                    //load comments activity
                    final TextView tv_comments = recyclerView.findViewHolderForAdapterPosition(recyclerView.getChildAdapterPosition(v)).itemView.findViewById(R.id.txt_updates_date_coms);
                    tv_comments.setOnClickListener(view -> {

                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        intent.putExtra(CommentActivity.EXTRA_POST_KEY, updats.getPostId());

                        startActivity(intent);

                    });


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    super.onLongPress(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {

                    return super.onDoubleTapEvent(e);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    //load comments activity
                    try {
                        View v = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        final Updates updats = adapter.updates.get(recyclerView.getChildAdapterPosition(v));

                        Intent intent = new Intent(getActivity(), CommentActivity.class);
                        intent.putExtra(CommentActivity.EXTRA_POST_KEY, updats.getPostId());

                        startActivity(intent);

                        Log.e(getClass().getName().toUpperCase(), "PostID: " + updats.getPostId());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    return super.onDoubleTap(e);
                }
            });


            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                final View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    int position = rv.getChildAdapterPosition(child);
                    String cont = ((TextView) rv.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.txt_update_desc)).getText().toString();
                    String title = ((TextView) rv.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.txt_updatename)).getText().toString();

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {


                rv.setNestedScrollingEnabled(false);

            }


            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }


    //search through recyclerview

    public void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                adapter.getFilter().filter(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);

                if (newText.length() == 0) {
                    adapter.updates = updates1;
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }


}
