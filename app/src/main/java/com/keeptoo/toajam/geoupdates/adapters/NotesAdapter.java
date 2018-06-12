package com.keeptoo.toajam.geoupdates.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keeptoo.toajam.R;
import com.keeptoo.toajam.models.Notes;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by keeptoo on 12/01/2017.
 */

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public ArrayList<Notes> mNotes;
    private Context context;


    public NotesAdapter(ArrayList<Notes> data) {
        this.mNotes = data;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.ly_notes_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.e(getClass().getName(), "Adding Note at pos: " + position);
        final Notes notes = mNotes.get(position);
        ViewHolder h = (ViewHolder) holder;
        h.txt_locationtime.setText(String.format("%s : %s", notes.date_created, notes.location));
        h.txt_note_user.setText(notes.name);
        h.txt_note_info.setText(notes.note);
        Picasso.with(context).load(notes.photourl).into(h.img_note_user);
    }


    // total number of rows
    @Override
    public int getItemCount() {
        return mNotes.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_time_location)
        TextView txt_locationtime;

        @BindView(R.id.note_info)
        TextView txt_note_info;

        @BindView(R.id.note_user)
        TextView txt_note_user;

        @BindView(R.id.note_user_img)
        CircleImageView img_note_user;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }


}

