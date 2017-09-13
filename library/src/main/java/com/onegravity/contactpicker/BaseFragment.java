/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Locale;

/**
 * Super class of ContactFragment / GroupFragment to take care of common tasks.
 */
public abstract class BaseFragment extends Fragment implements SearchView.OnQueryTextListener {

    private String[] mQueryStrings;

    private View mRootLayout;
    private RecyclerView mRecyclerView;
    private View mFastScroll;
    private View mSectionIndex;
    private View mEmptyView;

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

    protected final View createView(LayoutInflater inflater, int layoutId,
                                    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter,
                                    List<? extends ContactElement> elements) {
        mRootLayout = inflater.inflate(layoutId, null);
        mRecyclerView = (RecyclerView) mRootLayout.findViewById(android.R.id.list);
        mFastScroll = mRootLayout.findViewById(R.id.fast_scroller);
        mSectionIndex = mRootLayout.findViewById(R.id.fast_scroller_section_title_indicator);
        mEmptyView = mRootLayout.findViewById(android.R.id.empty);

        // use a LinearLayout for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(adapter);

        updateEmptyViewVisibility(elements);

        return mRootLayout;
    }

    protected void updateEmptyViewVisibility(List<? extends ContactElement> elements) {
        boolean isEmpty = elements == null || elements.isEmpty();
        mRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (mFastScroll != null) {
            mFastScroll.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (mSectionIndex != null) {
            mSectionIndex.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
        if (item.getItemId() == R.id.action_check_all) {
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
        String queryString = query.toLowerCase(Locale.getDefault());
        mQueryStrings = queryString.split(" ");
        performFiltering(mQueryStrings);
        return true;
    }

    /**
     * Filter the data using the query strings.
     */
    abstract protected void performFiltering(String[] queryStrings);

}