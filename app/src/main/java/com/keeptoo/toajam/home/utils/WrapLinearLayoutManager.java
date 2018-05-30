package com.keeptoo.toajam.home.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Added to fix recycler view internal crash due to indexOutOfBounds error
 */

public class WrapLinearLayoutManager extends LinearLayoutManager {

    public WrapLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("probe", "Just caught a IOOBE in RecyclerView :D");
        }
    }
}
