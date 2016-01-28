package com.pratilipi.android.pratilipi_and;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.pratilipi.android.pratilipi_and.adapter.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPageradapter;
    private SlidingTabLayout mTabs;
    private CharSequence mTitles[] = {"HOME","CATEGORIES","SHELF","PROFILE"};
    private int mNumbOfTabs = 4;
    private int mTabPosition;
    private SearchView  mSearchView;

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

        //TODO : SCHEDULE SERVICE. RIGHT NOW THIS IS CALLED EVERY TIME MAIN ACTIVITY IS STARTED OR RESUMED
//        Intent serviceIntent = new Intent(this, PratilipiService.class);
//        startService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        String searchQuery = (String) mSearchView.getQuery();
        Log.v(LOG_TAG, "Search Query : " + searchQuery);
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
            Intent search_intent = new Intent(this, SearchActivity.class);
            startActivity(search_intent);
            overridePendingTransition(0,0);
            return true;
        } else if(id == R.id.action_change_content_language){
            Log.e(LOG_TAG, "Change Content Language option is clicked");
            Intent intent = new Intent(this, LanguageSelectionActivity.class);
            startActivity(intent);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
