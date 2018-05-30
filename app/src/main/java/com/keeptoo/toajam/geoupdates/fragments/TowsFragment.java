package com.keeptoo.toajam.geoupdates.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.geoupdates.adapters.TowsAdapter;
import com.keeptoo.toajam.home.activities.HomeActivity;
import com.keeptoo.toajam.models.Towers;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keeptoo on 5/28/2018.
 */
public class TowsFragment extends Fragment {

    @BindView(R.id.towersRecyclerView)
    RecyclerView mRecyclerView;


    TowsAdapter towsAdapter;
    ArrayList<Towers> towers;

    private Context context;

    public TowsFragment() {
       // getTows();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ly_tows_list, container, false);
        context = container.getContext();
        ButterKnife.bind(this, view);


        towers = new ArrayList<>();

        towsAdapter = new TowsAdapter(new MapsFragment().getTows());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true));
        mRecyclerView.setAdapter(towsAdapter);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void getTows() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country);
        databaseReference.addValueEventListener(new ValueEventListener() {
            ArrayList<Towers> towersArrayList = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Towers values = snapshot.getValue(Towers.class);
                    //populate list
                    towersArrayList.add(values);
                }
                towers = towersArrayList;
                towsAdapter.mTows = towersArrayList;
                towsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
