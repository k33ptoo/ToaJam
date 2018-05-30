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
import com.keeptoo.toajam.geoupdates.adapters.NotesAdapter;
import com.keeptoo.toajam.home.activities.HomeActivity;
import com.keeptoo.toajam.models.Notes;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keeptoo on 5/28/2018.
 */
public class NotesFragment extends Fragment {

    @BindView(R.id.notesRecyclerview)
    RecyclerView mRecyclerView;


    NotesAdapter notesAdapter;
    ArrayList<Notes> notes;

    private Context context;

    public NotesFragment() {
        //getNotes();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ly_notes, container, false);
        context = container.getContext();
        ButterKnife.bind(this, view);


        notes = new ArrayList<>();

        notesAdapter = new NotesAdapter(new MapsFragment().getNotes());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true));
        mRecyclerView.setAdapter(notesAdapter);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void getNotes() {

        DatabaseReference noteReference = FirebaseDatabase.getInstance().getReference().child("notes").child(HomeActivity.Country);
        noteReference.addValueEventListener(new ValueEventListener() {
            ArrayList<Notes> notesArrayList = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Notes values = snapshot.getValue(Notes.class);
                    //populate list
                    notesArrayList.add(values);
                }
                notes = notesArrayList;
                notesAdapter.mNotes = notesArrayList;
                notesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
