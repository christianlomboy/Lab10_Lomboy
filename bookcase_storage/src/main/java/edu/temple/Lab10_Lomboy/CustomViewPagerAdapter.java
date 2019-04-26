package edu.temple.Lab10_Lomboy;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class CustomViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> adapterFragments;

    CustomViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> adapterFragments) {
        super(fm);
        this.adapterFragments = adapterFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return adapterFragments.get(position);
    }

    @Override
    public int getCount() {
        return adapterFragments.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
//        if (object instanceof BookDetailsFragment)
//            return POSITION_NONE;
        return super.getItemPosition(object);
    }
}
