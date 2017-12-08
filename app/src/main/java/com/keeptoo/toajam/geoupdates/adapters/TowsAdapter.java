package com.keeptoo.toajam.geoupdates.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keeptoo.toajam.R;
import com.keeptoo.toajam.models.Towers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by keeptoo on 12/01/2017.
 */

public class TowsAdapter extends RecyclerView.Adapter<TowsAdapter.ViewHolder> {


    public ArrayList<Towers> mTows = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;

    // data is passed into the constructor
    public TowsAdapter(Context context, ArrayList<Towers> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mTows = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.ly_tows_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //bind data
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //set info
        final Towers towers = mTows.get(position);
        String phone = "0" + String.valueOf(towers.phone);
        holder.txt_location.setText(towers.location);
        holder.txt_tow_user.setText(towers.name);
        holder.txt_towphone.setText(phone);
        Picasso.with(context).load(towers.photourl).into(holder.img_tow_user);
        Log.e(getClass().getName(), "Location Towers: " + towers.location);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mTows.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_location;
        public TextView txt_towphone;
        public TextView txt_tow_user;
        public CircleImageView img_tow_user;


        public ViewHolder(View itemView) {
            super(itemView);
            txt_location = itemView.findViewById(R.id.tow_location);
            txt_towphone = itemView.findViewById(R.id.tow_phone);
            txt_tow_user = itemView.findViewById(R.id.tow_name);
            img_tow_user = itemView.findViewById(R.id.tow_user_img);
        }


    }


}

