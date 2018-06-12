package com.keeptoo.toajam.geoupdates.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.keeptoo.toajam.geoupdates.fragments.MapsFragment;
import com.keeptoo.toajam.geoupdates.fragments.NotesFragment;
import com.keeptoo.toajam.geoupdates.fragments.TowsFragment;

/**
 * Created by keeptoo on 5/28/2018.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    public Context mContext;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return new MapsFragment();
        else if (position == 1)
            return new NotesFragment();
        else if (position == 2)
            return new TowsFragment();
        else
            return null;

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Map";
            case 1:
                return "Notes";
            case 2:
                return "Tows";
            default:
                return null;
        }
    }
}
