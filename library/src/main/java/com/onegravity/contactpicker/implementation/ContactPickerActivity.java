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

package com.onegravity.contactpicker.implementation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.onegravity.contactpicker.OnContactCheckedListener;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactFragment;
import com.onegravity.contactpicker.contact.ContactsLoaded;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.group.GroupFragment;
import com.onegravity.contactpicker.group.GroupsLoaded;
import com.onegravity.contactpicker.picture.ContactPictureType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ContactPickerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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

    /**
     * We put the resulting contact list into the Intent as extra data with this key.
     */
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

    // update the adapter after a certain amount of contacts has loaded
    private static final int BATCH_SIZE = 25;

    /*
     * The selected ids are saved in onSaveInstanceState, restored in onCreate and then applied to
     * the contacts and groups in onLoadFinished.
     */
    private static final String CONTACT_IDS = "CONTACT_IDS";
    private HashSet<Long> mSelectedContactIds = new HashSet<>();

    private static final String GROUP_IDS = "GROUP_IDS";
    private HashSet<Long> mSelectedGroupIds = new HashSet<>();

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

            try {
                mSelectedContactIds = (HashSet<Long>) savedInstanceState.getSerializable(CONTACT_IDS);
            }
            catch (ClassCastException ignore) {}

            try {
                mSelectedGroupIds = (HashSet<Long>) savedInstanceState.getSerializable(GROUP_IDS);
            }
            catch (ClassCastException ignore) {}
        }

        // read Activity parameter ContactPictureType
        sBadgeType = ContactPictureType.ROUND;
        Intent intent = getIntent();
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        getSupportLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(GROUPS_LOADER_ID, null, this);
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

        mSelectedContactIds.clear();;
        for (Contact contact : mContacts) {
            if (contact.isChecked()) {
                mSelectedContactIds.add( contact.getId() );
            }
        }
        outState.putSerializable(CONTACT_IDS, mSelectedContactIds);

        mSelectedGroupIds.clear();;
        for (Group group : mGroups) {
            if (group.isChecked()) {
                mSelectedGroupIds.add( group.getId() );
            }
        }
        outState.putSerializable(GROUP_IDS, mSelectedGroupIds);
    }

    private void updateTitle() {
        if (mNrOfSelectedContacts == 0) {
            setTitle(mDefaultTitle);
        }
        else {
            String title = getString(R.string.actionmode_selected, mNrOfSelectedContacts);
            setTitle(title);
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
        List<Contact> contacts = new ArrayList<>();
        if (mContacts != null) {
            for (Contact contact : mContacts) {
                if (contact.isChecked()) {
                    contacts.add(contact);
                }
            }
        }

        Intent data = new Intent();
        data.putExtra(RESULT_CONTACT_DATA, (Serializable) contacts);
        setResult(Activity.RESULT_OK, data);
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

    // ****************************************** Loader Methods *******************************************

    /*
     * Loader configuration contacts
     */
    private static final int CONTACTS_LOADER_ID = 0;
    private static final Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;
    private static final String[] CONTACTS_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI};
    private static final String CONTACTS_SORT = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";

    /*
     * Loader configuration contacts details
     */
    private static final int CONTACT_DETAILS_LOADER_ID = 1;
    private static final Uri CONTACT_DETAILS_URI = ContactsContract.Data.CONTENT_URI;
    private static final String[] CONTACT_DETAILS_PROJECTION = {
            ContactsContract.Data.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
            ContactsContract.CommonDataKinds.StructuredPostal.POBOX,
            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
            ContactsContract.CommonDataKinds.StructuredPostal.REGION,
            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.TYPE,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
    };

    /*
     * Loader configuration groups
     */
    private static final int GROUPS_LOADER_ID = 2;
    private static final Uri GROUPS_URI = ContactsContract.Groups.CONTENT_URI;
    private static final String[] GROUPS_PROJECTION = new String[] {
            ContactsContract.Groups._ID,
            ContactsContract.Groups.SOURCE_ID,
            ContactsContract.Groups.TITLE};
    private static final String GROUPS_SELECTION = ContactsContract.Groups.DELETED + " = 0";

    private static final String GROUPS_SORT = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case CONTACTS_LOADER_ID:
                return new CursorLoader(this, CONTACTS_URI, CONTACTS_PROJECTION, null, null, CONTACTS_SORT);
            case CONTACT_DETAILS_LOADER_ID:
                return new CursorLoader(this, CONTACT_DETAILS_URI, CONTACT_DETAILS_PROJECTION, null, null, null);
            case GROUPS_LOADER_ID:
                return new CursorLoader(this, GROUPS_URI, GROUPS_PROJECTION, GROUPS_SELECTION, null, GROUPS_SORT);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ContactsLoaded.post(null);
        GroupsLoaded.post(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch(loader.getId()) {
            case CONTACTS_LOADER_ID:
                readContacts(cursor);
                // contacts loaded --> load the contact details
                getSupportLoaderManager().initLoader(CONTACT_DETAILS_LOADER_ID, null, this);
                break;

            case CONTACT_DETAILS_LOADER_ID:
                readContactDetails(cursor);
                break;

            case GROUPS_LOADER_ID: {
                readGroups(cursor);
                break;
            }
        }
    }

    // ****************************************** Contact Methods *******************************************

    /*
     * List of all contacts.
     */
    private List<ContactImpl> mContacts = new ArrayList<>();

    /*
     * Map of all contacts by lookup key (ContactsContract.Contacts.LOOKUP_KEY).
     * We use this to find the contacts when the contact details are loaded.
     */
    private Map<String, ContactImpl> mContactsByLookupKey = new HashMap<>();

    /*
     * Number of selected contacts.
     * Selected groups are reflected in this too.
     */
    private int mNrOfSelectedContacts = 0;

    private void readContacts(Cursor cursor) {
        Log.e("1gravity", "***************************************************************");
        Log.e("1gravity", "* CONTACTS                                                    *");
        Log.e("1gravity", "***************************************************************");

        mContacts.clear();
        mContactsByLookupKey.clear();
        mNrOfSelectedContacts = 0;

        if (cursor.moveToFirst()) {
            cursor.moveToPrevious();
            int count = 0;
            while (cursor.moveToNext()) {
                ContactImpl contact = ContactImpl.fromCursor(cursor);
                mContacts.add(contact);

                // LOOKUP_KEY is the one we use to retrieve the contact when the contact details are loaded
                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                mContactsByLookupKey.put(lookupKey, contact);

                boolean isChecked = mSelectedContactIds.contains( contact.getId() );
                contact.setChecked(isChecked, true);
                mNrOfSelectedContacts += isChecked ? 1 : 0;

                contact.addOnContactCheckedListener(mContactListener);

                Log.e("1gravity", "lookupKey: " + lookupKey);
                Log.e("1gravity", "id: " + contact.getId());
                Log.e("1gravity", "displayName: " + contact.getDisplayName());
                Log.e("1gravity", "first name: " + contact.getFirstName());
                Log.e("1gravity", "last name: " + contact.getLastName());
                Log.e("1gravity", "photoUri: " + contact.getPhotoUri());

                // update the ui once some contacts have loaded
                if (++count >= BATCH_SIZE) {
                    ContactsLoaded.post(mContacts);
                    count = 0;
                }
            }
        }

        updateTitle();
        ContactsLoaded.post(mContacts);
    }

    private void readContactDetails(Cursor cursor) {
        Log.e("1gravity", "***************************************************************");
        Log.e("1gravity", "* CONTACTS DETAILS                                            *");
        Log.e("1gravity", "***************************************************************");

        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToPrevious();
            while (cursor.moveToNext()) {
                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                ContactImpl contact = mContactsByLookupKey.get(lookupKey);

                if (contact != null) {
                    readContactDetails(cursor, contact);
                }
            }
        }

        ContactsLoaded.post(mContacts);
        joinContactsAndGroups(mContacts);
    }

    private void readContactDetails(Cursor cursor, ContactImpl contact) {
        String mime = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
        if (mime.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            String type = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            if (email != null) contact.setEmail(email);
            Log.e("1gravity", "  email: "  + email);
            Log.e("1gravity", "  type: "  + type);
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String type = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            contact.setPhone(phone);
            Log.e("1gravity", "  phone: "  + phone);
            Log.e("1gravity", "  type: "  + type);
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
            String FORMATTED_ADDRESS = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
            String TYPE = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            String STREET = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            String POBOX = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
            String CITY = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            String REGION = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
            String POSTCODE = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
            String COUNTRY = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            contact.setAddress(FORMATTED_ADDRESS.replaceAll("\\n", ", "));
            Log.e("1gravity", "  FORMATTED_ADDRESS: "  + FORMATTED_ADDRESS);
            Log.e("1gravity", "  TYPE: "  + TYPE);
            Log.e("1gravity", "  STREET: "  + STREET);
            Log.e("1gravity", "  POBOX: "  + POBOX);
            Log.e("1gravity", "  CITY: "  + CITY);
            Log.e("1gravity", "  POSTCODE: "  + POSTCODE);
            Log.e("1gravity", "  REGION: "  + REGION);
            Log.e("1gravity", "  COUNTRY: "  + COUNTRY);
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
            String firstName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            if (firstName != null) contact.setFirstName(firstName);
            if (lastName != null) contact.setLastName(lastName);
            Log.e("1gravity", "  first name: "  + firstName);
            Log.e("1gravity", "  last name: "  + lastName);
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)) {
            int groupId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
            Log.e("1gravity", "  groupId: "  + groupId);
            contact.addGroupId(groupId);
        }
    }

    // ****************************************** Group Methods *******************************************

    /*
     * List of all groups.
     */
    private List<GroupImpl> mGroups = new ArrayList<>();

    /*
     * Map of all groups by id (ContactsContract.Groups._ID).
     * We use this to find the group when joining contacts and groups.
     */
    private Map<Long, GroupImpl> mGroupsById = new HashMap<>();

    /*
     * List of all visible groups.
     * Only groups with contacts will be shown / visible.
     */
    private List<GroupImpl> mVisibleGroups = new ArrayList<>();

    private void readGroups(Cursor cursor) {
        Log.e("1gravity", "***************************************************************");
        Log.e("1gravity", "* GROUPS                                                      *");
        Log.e("1gravity", "***************************************************************");

        mGroups.clear();
        mGroupsById.clear();
        mVisibleGroups.clear();

        if (cursor.moveToFirst()) {
            cursor.moveToPrevious();
            while (cursor.moveToNext()) {
                GroupImpl group = GroupImpl.fromCursor(cursor);

                mGroups.add(group);
                mGroupsById.put(group.getId(), group);

                boolean isChecked = mSelectedGroupIds.contains( group.getId() );
                group.setChecked(isChecked, true);

                group.addOnContactCheckedListener(mGroupListener);

                Log.e("1gravity", "group " + group.getId() + ": " + group.getDisplayName());
                String SOURCE_ID = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID));
                Log.e("1gravity", "SOURCE_ID: " + SOURCE_ID);
            }
        }

        GroupsLoaded.post(mVisibleGroups);
        joinContactsAndGroups(mContacts);
    }

    // ****************************************** Process Contacts / Groups *******************************************

    /**
     * Join contacts and groups.
     * This can happen once the contact details and the groups have loaded.
     */
    private synchronized void joinContactsAndGroups(List<? extends Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) return;
        if (mGroupsById == null || mGroupsById.isEmpty()) return;

        // map contacts to groups
        for (Contact contact : contacts) {
            for (Long groupId : contact.getGroupIds()) {
                GroupImpl group = mGroupsById.get(groupId);
                if (group != null) {
                    if (! group.hasContacts()) {
                        mVisibleGroups.add(group);
                    }
                    group.addContact(contact);
                }
            }
        }

        GroupsLoaded.post(mVisibleGroups);
    }

    /**
     * Listening to onContactChecked for contacts because we need to update the title to reflect
     * the number of selected contacts and we also want to un-check groups if none of their contacts
     * are checked any more.
     */
    private OnContactCheckedListener<Contact> mContactListener = new OnContactCheckedListener<Contact>() {
        @Override
        public void onContactChecked(Contact contact, boolean wasChecked, boolean isChecked) {
            if (wasChecked != isChecked) {
                mNrOfSelectedContacts += isChecked ? 1 : -1;
                mNrOfSelectedContacts = Math.min(mContacts.size(), Math.max(0, mNrOfSelectedContacts));
                updateTitle();

                if (! isChecked) {
                    processGroupSelection();
                }
            }
        }
    };

    /**
     * Check/un-check a group's contacts if the user checks/un-checks a group.
     */
    private OnContactCheckedListener<Group> mGroupListener = new OnContactCheckedListener<Group>() {
        @Override
        public void onContactChecked(Group group, boolean wasChecked, boolean isChecked) {
            // check/un-check the group's contacts
            processContactSelection(group, isChecked);

            // check if we need to deselect some groups
            processGroupSelection();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactSelectionChanged event) {
        // all has changed -> calculate the number of selected contacts and update the title
        calcNrOfSelectedContacts();

        // check if we need to deselect some groups
        processGroupSelection();
    }

    /**
     * Check/un-check contacts for a group that has been selected/deselected.
     * Call this when a group has been selected/deselected or after a ContactSelectionChanged event.
     */
    private void processContactSelection(Group group, boolean isChecked) {
        if (group == null || mContacts == null) return;

        // check/un-check contacts
        boolean hasChanged = false;
        for (Contact contact : group.getContacts()) {
            if (contact.isChecked() != isChecked) {
                contact.setChecked(isChecked, true);
                hasChanged = true;
            }
        }

        if (hasChanged) {
            ContactsLoaded.post(mContacts);
            calcNrOfSelectedContacts();
        }
    }

    /**
     * Calculate the number or selected contacts.
     * Call this when a group has been selected/deselected or after a ContactSelectionChanged event.
     */
    private void calcNrOfSelectedContacts() {
        if (mContacts == null) return;

        mNrOfSelectedContacts = 0;
        for (Contact contact : mContacts) {
            if (contact.isChecked()) {
                mNrOfSelectedContacts++;
            }
        }

        updateTitle();
    }

    /**
     * Check if a group needs to be deselected because none of its contacts is selected.
     * Call this when a contact or group has been deselected or after a ContactSelectionChanged event.
     */
    private void processGroupSelection() {
        if (mGroups == null) return;

        boolean hasChanged = false;
        for (Group theGroup : mGroups) {
            if (deselectGroup(theGroup)) {
                hasChanged = true;
            }
        }

        if (hasChanged) {
            GroupsLoaded.post(mVisibleGroups);
        }
    }

    private boolean deselectGroup(Group group) {
        if (group == null) return false;

        // check if the group's contacts are all deselected
        boolean isSelected = false;
        for (Contact groupContact : group.getContacts()) {
            if (groupContact.isChecked()) {
                isSelected = true;
                break;
            }
        }

        if (! isSelected && group.isChecked()) {
            // no contact selected
            group.setChecked(false, true);
            return true;
        }

        return false;
    }

}
