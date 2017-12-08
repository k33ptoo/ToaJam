package com.keeptoo.toajam.geoupdates.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.utils.InteractionUtils;

/**
 * Created by keeptoo on 11/16/2017.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public CustomInfoWindowAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.custom_markerwindow, null);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvSubTitle = view.findViewById(R.id.tv_subtitle);
        ImageView ivCall = view.findViewById(R.id.img_g);

        tvTitle.setText(marker.getTitle());
        tvSubTitle.setText(marker.getSnippet());

        //check if marker is note/tow
        try {
                if (!marker.getSnippet().matches("[0-9]+")) {
                    ivCall.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_note_marker, null));
                }
                else
                    if(marker.getSnippet().equals(null)) {

                        ivCall.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_call, null));
                        new InteractionUtils().showToast(context, "No Tittle", Toast.LENGTH_SHORT);
                    }

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return view;
    }
}