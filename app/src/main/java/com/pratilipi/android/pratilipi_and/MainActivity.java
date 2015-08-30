package com.pratilipi.android.pratilipi_and;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.pratilipi.android.pratilipi_and.adapter.ViewPagerAdapter;
import com.pratilipi.android.pratilipi_and.service.PratilipiService;

public class MainActivity extends AppCompatActivity{

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPageradapter;
    private SlidingTabLayout mTabs;
    private CharSequence mTitles[] = {"HOME","CATEGORIES","SHELF","PROFILE"};
    private int mNumbOfTabs = 4;
    private int mTabPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0f);

        mViewPageradapter = new ViewPagerAdapter(getSupportFragmentManager(), mTitles, mNumbOfTabs);

        mViewPager = (ViewPager) findViewById(R.id.pager_main_activity);
        mViewPager.setAdapter(mViewPageradapter);

        mTabs = (SlidingTabLayout) findViewById(R.id.tabs_main_activity);
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mTabs.setViewPager(mViewPager);
        mViewPager.setCurrentItem(getIntent().getFlags());

        Intent serviceIntent = new Intent(this, PratilipiService.class);
        startService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.action_search).getActionView();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.action_search_queryHint));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String lan = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("selectedLanguage", "");

        if (lan.length() < 1) {
            startActivity(new Intent(this, LanguageSelectionActivity.class));
            finish();
        }

        //TODO : CLEAR SAERCH BOX IF ITS NOT EMPTY
//        if(null!= searchViewButton)
//            searchViewButton.clearFocus();
    }
}
