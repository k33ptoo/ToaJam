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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by keeptoo on 12/01/2017.
 */

public class TowsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public ArrayList<Towers> mTows;
    private Context context;

    // data is passed into the constructor
    public TowsAdapter(ArrayList<Towers> data) {
        this.mTows = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.ly_tows_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder1, int position) {
        final Towers towers = mTows.get(position);
        ViewHolder holder = (ViewHolder) holder1;
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

        @BindView(R.id.tow_location)
        TextView txt_location;
        @BindView(R.id.tow_phone)
        TextView txt_towphone;
        @BindView(R.id.tow_name)
        TextView txt_tow_user;
        @BindView(R.id.tow_user_img)
        CircleImageView img_tow_user;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }


}

