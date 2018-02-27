package com.keeptoo.toajam.updates;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.authetication.SessionManager;
import com.keeptoo.toajam.firebase_utils.FireBaseUtilities;
import com.keeptoo.toajam.home.HomeActivity;
import com.keeptoo.toajam.models.Updates;
import com.keeptoo.toajam.updates.adapter.UpdatesAdapter;
import com.keeptoo.toajam.updates.comments.CommentActivity;
import com.squareup.picasso.Picasso;

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
    private DatabaseReference myRef;

    UpdatesAdapter adapter;
    ArrayList<Updates> updates1;

    SessionManager sessionManager;


    private AdView mAdView;

    View view;


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
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initViews();
                        swipeLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });


        //load ads
        final RelativeLayout layout_ad = view.findViewById(R.id.rel_adview);
        mAdView = view.findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final ImageView imageView = view.findViewById(R.id.expandedImage);
        mAdView.setAdListener(new AdListener() {
                                  @Override
                                  public void onAdFailedToLoad(int i) {

                                      imageView.setVisibility(View.VISIBLE);
                                      layout_ad.setVisibility(View.INVISIBLE);
                                      super.onAdFailedToLoad(i);
                                  }

                                  @Override
                                  public void onAdLoaded() {

                                      //show custom ad
                                      loadCustomAd(imageView);

                                      imageView.postDelayed(new Runnable() {
                                          @Override
                                          public void run() {
                                              imageView.setVisibility(View.INVISIBLE);
                                              layout_ad.setVisibility(View.VISIBLE);

                                              LottieAnimationView animationView = view.findViewById(R.id.animation_view);
                                              animationView.setAnimation("pinjump.json");
                                              animationView.loop(true);
                                              animationView.playAnimation();

                                          }
                                      }, 30000);


                                      super.onAdLoaded();
                                  }
                              }

        );

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
                    iv_apps.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("updates").child(HomeActivity.Country).child(updats.getPostId());
                            new FireBaseUtilities().onAppClicked(reference);



                        }
                    });
                    // ---------- end appeciation ----------------------



                    //load comments activity
                    final TextView tv_comments = recyclerView.findViewHolderForAdapterPosition(recyclerView.getChildAdapterPosition(v)).itemView.findViewById(R.id.txt_updates_date_coms);
                    tv_comments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(getActivity(), CommentActivity.class);
                            intent.putExtra(CommentActivity.EXTRA_POST_KEY, updats.getPostId());

                            startActivity(intent);

                        }
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


    //load custom advert

    private void loadCustomAd(final ImageView view) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(HomeActivity.Country).child("toolbar.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.e(getClass().getName(), "AD URL " + uri);
                Picasso.with(getActivity().getApplicationContext()).load(uri).into(view);
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
