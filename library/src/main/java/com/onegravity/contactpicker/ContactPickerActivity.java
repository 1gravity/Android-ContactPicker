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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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

import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactFragment;
import com.onegravity.contactpicker.group.GroupFragment;
import com.onegravity.contactpicker.picture.ContactPictureType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.List;

public class ContactPickerActivity extends AppCompatActivity {

    /**
     * Use this parameter to determine whether the contact picture shows a contact badge and if yes
     * what type (round, square)
     *
     * {@link com.onegravity.contactpicker.picture.ContactPictureType}
     */
    public static final String EXTRA_CONTACT_BADGE_TYPE = "EXTRA_CONTACT_BADGE_TYPE";

    /**
     * Use this to define what contact information is used for the description field (second line)
     *
     * {@link com.onegravity.contactpicker.contact.ContactDescription}
     */
    public static final String EXTRA_CONTACT_DESCRIPTION = "EXTRA_CONTACT_DESCRIPTION";

    public static final String RESULT_CONTACT_DATA = "RESULT_CONTACT_DATA";

    private static ContactPictureType sBadgeType = ContactPictureType.ROUND;
    public static ContactPictureType getContactBadgeType() {
        return sBadgeType;
    }

    private static ContactDescription sDescription = ContactDescription.EMAIL;
    public static ContactDescription getContactDescription() {
        return sDescription;
    }

    private PagerAdapter mAdapter;

    private String mDefaultTitle;

    // ****************************************** Lifecycle Methods *******************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if we have the READ_CONTACTS permission, if not --> terminate
        try {
            int pid = android.os.Process.myPid();
            PackageManager pckMgr = getPackageManager();
            int uid = pckMgr.getApplicationInfo(getComponentName().getPackageName(), PackageManager.GET_META_DATA).uid;
            enforcePermission(Manifest.permission.READ_CONTACTS, pid, uid, "Contact permission hasn't been granted to this app, terminating.");
        }
        catch (PackageManager.NameNotFoundException | SecurityException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            finish();
            return;
        }

        Intent intent = getIntent();

        // retrieve default title which is used if no contacts are selected
        if (savedInstanceState == null) {
            try {
                PackageManager pkMgr = getPackageManager();
                ActivityInfo activityInfo = pkMgr.getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                mDefaultTitle = activityInfo.loadLabel(pkMgr).toString();
            }
            catch (PackageManager.NameNotFoundException ignore) {
                mDefaultTitle = getTitle().toString();
            }
        }
        else {
            mDefaultTitle = savedInstanceState.getString("mDefaultTitle");
        }

        // read Activity parameter ContactPictureType
        sBadgeType = ContactPictureType.ROUND;
        String tmp = intent.getStringExtra(EXTRA_CONTACT_BADGE_TYPE);
        if (tmp != null) {
            try {
                sBadgeType = ContactPictureType.valueOf(tmp);
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getSimpleName(), tmp + " is not a legal EXTRA_CONTACT_BADGE_TYPE value, defaulting to ROUND");
            }
        }

        // read Activity parameter ContactDescription
        sDescription = ContactDescription.EMAIL;
        tmp = intent.getStringExtra(EXTRA_CONTACT_DESCRIPTION);
        if (tmp != null) {
            try {
                sDescription = ContactDescription.valueOf(tmp);
            }
            catch (IllegalArgumentException e) {
                Log.e(getClass().getSimpleName(), tmp + " is not a legal EXTRA_CONTACT_DESCRIPTION value, defaulting to EMAIL");
            }
        }

        setContentView(R.layout.contact_tab_layout);

        // initialize TabLayout
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabContent);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        TabLayout.Tab tabContacts = tabLayout.newTab();
        tabContacts.setText(R.string.contact_tab_title);
        tabLayout.addTab(tabContacts);

        TabLayout.Tab tabGroups = tabLayout.newTab();
        tabGroups.setText(R.string.group_tab_title);
        tabLayout.addTab(tabGroups);

        // initialize ViewPager
        final ViewPager viewPager = (ViewPager) findViewById(R.id.tabPager);
        mAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(mAdapter);
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

        private ContactFragment mContactFragment;
        private GroupFragment mGroupFragment;

        public PagerAdapter(FragmentManager fm, int numOfTabs) {
            super(fm);
            mNumOfTabs = numOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mContactFragment = ContactFragment.newInstance();
                    return mContactFragment;
                case 1:
                    mGroupFragment = GroupFragment.newInstance();
                    return mGroupFragment;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }

        ContactFragment getContactFragment() {
            return mContactFragment;
        }

        GroupFragment getGroupFragment() {
            return mGroupFragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("mDefaultTitle", mDefaultTitle);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsCheckedEvent event) {
        int nrOfContacts = event.getNrOfContacts();
        if (nrOfContacts == 0) {
            setTitle(mDefaultTitle);
        }
        else {
            setTitle(getString(R.string.actionmode_selected, nrOfContacts));
        }
    }

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

        return super.onOptionsItemSelected(item);
    }

    private void onDone() {
        // return only checked contacts
        ContactFragment fragment = mAdapter.getContactFragment();
        if (fragment != null) {
            List<Contact> contacts = fragment.getSelectedContacts();
            Intent data = new Intent();
            data.putExtra(RESULT_CONTACT_DATA, (Serializable) contacts);
            setResult(Activity.RESULT_OK, data);
        }
        else {
            setResult(Activity.RESULT_CANCELED);
        }

        finish();
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
