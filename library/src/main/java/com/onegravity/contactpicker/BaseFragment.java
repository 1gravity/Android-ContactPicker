/*
 * Copyright (C) 2015-2016 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.contactpicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;

/**
 * Super class of ContactFragment / GroupFragment to take care of common tasks.
 */
public abstract class BaseFragment extends Fragment implements SearchView.OnQueryTextListener {

    private String[] mQueryStrings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (savedInstanceState != null) {
            mQueryStrings = (String[]) savedInstanceState.getSerializable("mQueryStrings");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("mQueryStrings", mQueryStrings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        if (mQueryStrings != null && mQueryStrings.length > 0) {
            performFiltering(mQueryStrings);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.action_check_all) {
            checkAll();
            return true;
        }

        return false;
    }

    abstract protected void checkAll();

    @Override
    final public boolean onQueryTextSubmit(String query) {
        return onQuery(query);
    }

    @Override
    final public boolean onQueryTextChange(String query) {
        return onQuery(query);
    }

    private boolean onQuery(String query) {
        String queryString = query.toString().toLowerCase( Locale.getDefault() );
        mQueryStrings = queryString.split(" ");
        performFiltering(mQueryStrings);
        return true;
    }

    /**
     * Filter the data using the query strings.
     */
    abstract protected void performFiltering(String[] queryStrings);

}