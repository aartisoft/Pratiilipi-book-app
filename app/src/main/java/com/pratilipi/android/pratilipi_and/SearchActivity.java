package com.pratilipi.android.pratilipi_and;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by MOHIT KHAITAN on 18-12-2015.
 */
public class SearchActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private SearchView  mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setElevation(1f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.action_search).getActionView();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setFocusable(true);
        searchView.setQueryHint(getString(R.string.action_search_queryHint));
        searchView.onActionViewExpanded();


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
            searchView.setFocusable(true);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                Log.e(LOG_TAG, "Search query : " + newText);
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                //Here u can get the value "query" which is entered in the search box.
                Log.e(LOG_TAG, "Search query : " + query);
                Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                intent.putExtra(CardListActivity.INTENT_EXTRA_LAUNCHER, CardListActivity.LAUNCHER_SEARCH );
                intent.putExtra( CardListActivity.INTENT_EXTRA_SEARCH_QUERY, query );
                intent.putExtra( CardListActivity.INTENT_EXTRA_TITLE, query );
                startActivity(intent);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

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
            return true;
        } else if(id == R.id.action_change_content_language){
            Log.e(LOG_TAG, "Change Content Language option is clicked");
            Intent intent = new Intent(this, LanguageSelectionActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
