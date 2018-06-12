package com.keeptoo.toajam.home.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.keeptoo.toajam.home.fragments.TwitterFragment;
import com.keeptoo.toajam.home.fragments.UpdatesFragment;

/**
 * Created by keeptoo on 5/28/2018.
 */
public class HomeViewPagerAdapter extends FragmentPagerAdapter {

    public Context mContext;

    public HomeViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return new UpdatesFragment();
        else if (position == 1)
            return new TwitterFragment();
        else
            return null;

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Updates";
            case 1:
                return "Twitter";
            default:
                return null;
        }
    }
}
