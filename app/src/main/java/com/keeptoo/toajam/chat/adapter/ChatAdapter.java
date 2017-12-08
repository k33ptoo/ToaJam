package com.keeptoo.toajam.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keeptoo.toajam.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keeptoo on 11/04/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VHNearby> {

    public ArrayList<String> nearby;
    Context context;

    public ChatAdapter(ArrayList<String> nearbies) {
        this.nearby = nearbies;
    }

    @Override
    public ChatAdapter.VHNearby onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new VHNearby(LayoutInflater.from(context).inflate(R.layout.nearby_items, parent, false));

    }

    @Override
    public void onBindViewHolder(ChatAdapter.VHNearby holder, int position) {
        holder.tv_tittle.setText("Nai");
        holder.tv_info.setText("traffic com.keeptoo.toajam.updates");

    }

    @Override
    public int getItemCount() {
        return nearby.size();
    }

    public class VHNearby extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView tv_tittle;
        @BindView(R.id.txt_desc)
        TextView tv_info;
        @BindView(R.id.iv_icon)
        ImageView imageView_icon;

        public VHNearby(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            Log.e("Adapter", "Bind CLass");
        }
    }
}
