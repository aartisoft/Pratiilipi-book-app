package com.pratilipi.android.pratilipi_and.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.pratilipi.android.pratilipi_and.CategoryFragment;
import com.pratilipi.android.pratilipi_and.HomeFragment;
import com.pratilipi.android.pratilipi_and.ProfileFragment;
import com.pratilipi.android.pratilipi_and.ShelfFragment;


/**
 * Created by MOHIT KHAITAN on 12-06-2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence Titles[];
    int NumOfTabs;

    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumOfTabsSumB) {
        super(fm);
        this.Titles = mTitles;
        this.NumOfTabs = mNumOfTabsSumB;
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            HomeFragment homeActivityObj = new HomeFragment();
            return homeActivityObj;
        } else if (position == 1) {
            CategoryFragment categoriesActivityObj = new CategoryFragment();
            return categoriesActivityObj;
        } else if (position == 2) {
            ShelfFragment shelfActivityObj = new ShelfFragment();
            return shelfActivityObj;
        } else {
            ProfileFragment profileActivityObj = new ProfileFragment();
            return profileActivityObj;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }



    @Override
    public int getCount() {
        return NumOfTabs;
    }
}
