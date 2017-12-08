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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by keeptoo on 12/01/2017.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {


    public ArrayList<Notes> mNotes = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;

    // data is passed into the constructor
    public NotesAdapter(Context context, ArrayList<Notes> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mNotes = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.ly_notes_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //set info
        final Notes notes = mNotes.get(position);
        holder.txt_locationtime.setText(notes.date_created + " : " + notes.location);
        holder.txt_note_user.setText(notes.name);
        holder.txt_note_info.setText(notes.note);
        Picasso.with(context).load(notes.photourl).into(holder.img_note_user);
        Log.e(getClass().getName(), "Location: " + notes.location);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mNotes.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_locationtime;
        public TextView txt_note_info;
        public TextView txt_note_user;
        public CircleImageView img_note_user;


        public ViewHolder(View itemView) {
            super(itemView);
            txt_locationtime = itemView.findViewById(R.id.note_time_location);
            txt_note_info = itemView.findViewById(R.id.note_info);
            txt_note_user = itemView.findViewById(R.id.note_user);
            img_note_user = itemView.findViewById(R.id.note_user_img);
        }


    }


}

