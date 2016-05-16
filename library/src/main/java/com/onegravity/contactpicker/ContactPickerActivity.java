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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.onegravity.contactpicker.picture.ContactPictureType;
import com.onegravity.contactpicker.contact.ContactFragment;
import com.onegravity.contactpicker.group.GroupFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ContactPickerActivity extends AppCompatActivity {

    //public static final String EXTRA_CONTACT_BADGE_TYPE = "EXTRA_CONTACT_BADGE_TYPE";
    public static final String EXTRA_CONTACT_BADGE_TYPE = "EXTRA_CONTACT_BADGE_TYPE";

    private static ContactPictureType sBadgeType = ContactPictureType.ROUND;

    public static ContactPictureType getContactBadgeType() {
        return sBadgeType;
    }

    private boolean mAllChecked;

    // ****************************************** Lifecycle Methods *******************************************

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // read Activity parameters
        Intent intent = getIntent();
        sBadgeType = ContactPictureType.ROUND;
        if (intent.hasExtra(EXTRA_CONTACT_BADGE_TYPE)) {
            String tmp = intent.getStringExtra(EXTRA_CONTACT_BADGE_TYPE);
            try {
                sBadgeType = ContactPictureType.valueOf(tmp);
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getSimpleName(), tmp + " is not a legal EXTRA_CONTACT_BADGE_TYPE value, defaulting to ROUND");
            }
        }

        setContentView(R.layout.contact_tab_layout);

        // initialize TabLayout
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabContent);
        TabLayout.Tab tabContacts = tabLayout.newTab();
        tabContacts.setText(R.string.contact_tab_title);
        tabLayout.addTab(tabContacts);
        TabLayout.Tab tabGroups = tabLayout.newTab();
        tabGroups.setText(R.string.group_tab_title);
        tabLayout.addTab(tabGroups);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // initialize ViewPager
        final ViewPager viewPager = (ViewPager) findViewById(R.id.tabPager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private static class PagerAdapter extends FragmentStatePagerAdapter {
        private int mNumOfTabs;

        public PagerAdapter(FragmentManager fm, int numOfTabs) {
            super(fm);
            mNumOfTabs = numOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return ContactFragment.newInstance();
                case 1: return GroupFragment.newInstance();
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

/*        mListView = (ListView) findViewById(android.R.id.list);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        loadContacts();

        EventBus.getDefault().register(this);

        mListView.setClickable(true);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                ContactBase contact = mAdapter.getContact(pos);
                contact.setChecked(! contact.isChecked());
                mAdapter.notifyDataSetChanged();
                ContactCheckedEvent.post();
            }
        });
        mListView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mListView.setScrollingCacheEnabled(true);
        mListView.setSmoothScrollbarEnabled(false);
        mListView.setCacheColorHint(0);
        mListView.setFastScrollEnabled(true);*/
    }

    @Override
    protected void onPause() {
        super.onPause();

//        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mAllChecked", mAllChecked);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactCheckedEvent event) {
//        setTitle(getString(R.string.actionmode_selected, mAdapter.getNrOfSelectedContacts()));
    }

    // ****************************************** Lifecycle Methods *******************************************

/*    private Contact createContact(Cursor cursor, String id, String email) {
        String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        //Divide display name to first and last
        String[] names = displayName != null ? displayName.split("\\s+") : new String[]{"---", "---"};
        String firstName = names.length >= 1 ? names[0] : displayName;
        String lastName = names.length >= 2 ? names[1] : "";
        return Helper.isNullOrEmpty(email) ? null : new Contact(id, firstName, lastName, displayName, email);
    }*/

    // ****************************************** Option Menu *******************************************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, null);
            finish();
            return true;
        }
        else if( id == R.id.menu_done) {
            onDone();
            return true;
        }
        else if( id == R.id.menu_search) {
            onSearch(item);
            return true;
        }
        else if( id == R.id.menu_check_all) {
            onCheckAll();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onDone() {
        //pass only checked contacts
/*        ArrayList<ContactBase> contacts = mAdapter.getSelectedContacts();

        Intent result = new Intent();
        result.putExtra(ContactBase.CONTACTS_DATA, contacts);
        setResult(Activity.RESULT_OK, result);*/

        finish();
    }

    private void onCheckAll() {
        //Check or uncheck all
        mAllChecked = !mAllChecked;
        //mAdapter.checkAll(mAllChecked);
    }

    @SuppressLint("InlinedApi")
    private void onSearch(final MenuItem item) {
        ViewGroup searchWidget = (ViewGroup)item.getActionView();
        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        final EditText input = (EditText) searchWidget.findViewById(R.id.search_text);

        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        // TextWatcher
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //mAdapter.getFilter().filter(s);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // OnEditorActionListener (return/enter key)
        input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event!=null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    item.collapseActionView();
                    return true;
                }
                return false;
            }
        });

        // OnClickListener (close button)
        searchWidget.findViewById(R.id.search_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.collapseActionView();
                //mAdapter.getFilter().filter("");
            }
        });

        // OnActionExpandListener
        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                input.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                input.clearFocus();
                imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                return true;
            }
        });
    }

}
