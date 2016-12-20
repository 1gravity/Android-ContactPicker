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

package com.onegravity.contactpicker.core;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.onegravity.contactpicker.OnContactCheckedListener;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSelectionChanged;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.contact.ContactsLoaded;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.group.GroupsLoaded;
import com.onegravity.contactpicker.picture.ContactPictureType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS;

public class ContactPickerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Use this parameter to set the Theme for this activity.
     * It's the theme's resource id (like R.style.YourFancyTheme)..
     * Please note that the theme needs to define the attributes defined in attrs.xml or the
     * Activity will crash. You can either extend the library's theme(s) or define the attributes in
     * your custom theme.
     */
    public static final String EXTRA_THEME = "EXTRA_THEME";

    /**
     * Use this parameter to determine whether the contact picture shows a contact badge and if yes
     * what type (round, square).
     *
     * {@link com.onegravity.contactpicker.picture.ContactPictureType}
     */
    public static final String EXTRA_CONTACT_BADGE_TYPE = "EXTRA_CONTACT_BADGE_TYPE";

    /**
     * Use this to define what contact information is used for the description field (second line).
     *
     * {@link com.onegravity.contactpicker.contact.ContactDescription}
     */
    public static final String EXTRA_CONTACT_DESCRIPTION = "EXTRA_CONTACT_DESCRIPTION";

    /**
     * This parameter sets limit of the amount of contacts the user can select on each intent.
     * By default, the retrieved value is 0
     */
    public static final String EXTRA_SELECT_CONTACTS_LIMIT = "EXTRA_SELECT_CONTACTS_LIMIT";

    /**
     * This parameter sets messages seen in a toast when the selection limit has been reached.
     */
    public static final String EXTRA_LIMIT_REACHED_MESSAGE = "EXTRA_LIMIT_REACHED_MESSAGE";

    /**
     * This parameter sets the boolean which decides whether to show the check all menu button.
     * By default, the retrieved value is true
     */
    public static final String EXTRA_SHOW_CHECK_ALL = "EXTRA_SHOW_CHECK_ALL";

    /**
     * This parameter sets a boolean to filter out contacts that do not have phone numbers.
     * By default, the retrieved value is false
     */
    public static final String EXTRA_ONLY_CONTACTS_WITH_PHONE = "EXTRA_ONLY_CONTACTS_WITH_PHONE";

    /**
     * This defines which type is shown in the description. It refines the EXTRA_CONTACT_DESCRIPTION
     * parameter and uses the android.provider.ContactsContract.CommonDataKinds values
     *
     * If the description is PHONE then this parameter needs to match one of the
     * ContactsContract.CommonDataKinds.Email.ContactsContract.CommonDataKinds.Phone.TYPE values:
     *
     * - TYPE_CUSTOM, TYPE_HOME, TYPE_MOBILE, TYPE_WORK, TYPE_FAX_WORK, TYPE_FAX_HOME, TYPE_PAGER
     * - TYPE_OTHER, TYPE_CALLBACK, TYPE_CAR, TYPE_COMPANY_MAIN, TYPE_ISDN, TYPE_MAIN
     * - TYPE_OTHER_FAX, TYPE_RADIO, TYPE_TELEX, TYPE_TTY_TDD, TYPE_WORK_MOBILE, TYPE_WORK_PAGER
     * - TYPE_ASSISTANT, TYPE_MMS
     *
     * (https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Phone.html)
     *
     * If the description is EMAIL then this parameter needs to match one of the
     * ContactsContract.CommonDataKinds.Email.TYPE values:
     *
     * - TYPE_CUSTOM
     * - TYPE_HOME
     * - TYPE_WORK
     * - TYPE_OTHER
     * - TYPE_MOBILE
     *
     * (https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Email.html)
     *
     * If the description is ADDRESS then this parameter needs to match one of the
     * ContactsContract.CommonDataKinds.StructuredPostal.TYPE values:
     *
     * - TYPE_CUSTOM
     * - TYPE_HOME
     * - TYPE_WORK
     * - TYPE_OTHER
     *
     * (https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.StructuredPostal.html)
     */
    public static final String EXTRA_CONTACT_DESCRIPTION_TYPE = "EXTRA_CONTACT_DESCRIPTION_TYPE";

    /**
     * Use this to define the sorting order for contacts
     *
     * {@link com.onegravity.contactpicker.contact.ContactSortOrder}
     */
    public static final String EXTRA_CONTACT_SORT_ORDER = "EXTRA_CONTACT_SORT_ORDER";

    /**
     * This parameter contains a serializable collection of contact IDs.
     * The contacts matching these IDs are preselected when entering the activity.
     *
     * {@link com.onegravity.contactpicker.ContactElement}
     */
    public static final String EXTRA_PRESELECTED_CONTACTS = "EXTRA_PRESELECTED_CONTACTS";

    /**
     * This parameter contains a serializable collection of group IDs.
     * The groups matching these IDs are preselected when entering the activity.
     *
     * {@link com.onegravity.contactpicker.ContactElement}
     */
    public static final String EXTRA_PRESELECTED_GROUPS = "EXTRA_PRESELECTED_GROUPS";

    /**
     * We put the resulting contact list into the Intent as extra data with this key.
     */
    public static final String RESULT_CONTACT_DATA = "RESULT_CONTACT_DATA";

    /**
     * We put the resulting group list into the Intent as extra data with this key.
     */
    public static final String RESULT_GROUP_DATA = "RESULT_GROUP_DATA";

    private int mThemeResId;

    private ContactPictureType mBadgeType = ContactPictureType.ROUND;

    private int mDescriptionType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
    private ContactDescription mDescription = ContactDescription.ADDRESS;

    private ContactSortOrder mSortOrder = ContactSortOrder.AUTOMATIC;

    private PagerAdapter mAdapter;

    private String mDefaultTitle;

    private Boolean mShowCheckAll = true;

    // update the adapter after a certain amount of contacts has loaded
    private static final int BATCH_SIZE = 50;

    /*
     * The selected ids are saved in onSaveInstanceState, restored in onCreate and then applied to
     * the contacts and groups in onLoadFinished.
     */
    private static final String CONTACT_IDS = "CONTACT_IDS";
    private HashSet<Long> mSelectedContactIds = new HashSet<>();

    private static final String GROUP_IDS = "GROUP_IDS";
    private HashSet<Long> mSelectedGroupIds = new HashSet<>();

    private String mLimitReachedMessage;
    private int mSelectContactsLimit = 0;
    private Boolean mOnlyWithPhoneNumbers = false;

    // ****************************************** Lifecycle Methods *******************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Check if we have the READ_CONTACTS permission, if not --> terminate.
         */
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
        if (savedInstanceState == null) {
            /*
             * Retrieve default title used if no contacts are selected.
             */
            try {
                PackageManager pkMgr = getPackageManager();
                ActivityInfo activityInfo = pkMgr.getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                mDefaultTitle = activityInfo.loadLabel(pkMgr).toString();
            }
            catch (PackageManager.NameNotFoundException ignore) {
                mDefaultTitle = getTitle().toString();
            }

            if(intent.hasExtra(EXTRA_PRESELECTED_CONTACTS)) {
                Collection<Long> preselectedContacts = (Collection<Long>) intent.getSerializableExtra( EXTRA_PRESELECTED_CONTACTS );
                mSelectedContactIds.addAll(preselectedContacts);
            }

            if(intent.hasExtra(EXTRA_PRESELECTED_GROUPS)) {
                Collection<Long> preselectedGroups = (Collection<Long>) intent.getSerializableExtra( EXTRA_PRESELECTED_GROUPS );
                mSelectedGroupIds.addAll(preselectedGroups);
            }

            mThemeResId = intent.getIntExtra(EXTRA_THEME, R.style.ContactPicker_Theme_Light);
        }
        else {
            mDefaultTitle = savedInstanceState.getString("mDefaultTitle");

            mThemeResId = savedInstanceState.getInt("mThemeResId");

            // Retrieve selected contact and group ids.
            try {
                mSelectedContactIds = (HashSet<Long>) savedInstanceState.getSerializable(CONTACT_IDS);
                mSelectedGroupIds = (HashSet<Long>) savedInstanceState.getSerializable(GROUP_IDS);
            }
            catch (ClassCastException ignore) {}
        }

        /*
         * Retrieve ContactPictureType.
         */
        String enumName = intent.getStringExtra(EXTRA_CONTACT_BADGE_TYPE);
        mBadgeType = ContactPictureType.lookup(enumName);

        /*
         * Retrieve SelectContactsLimit.
         */
        mSelectContactsLimit = intent.getIntExtra(EXTRA_SELECT_CONTACTS_LIMIT, 0);

        /*
         * Retrieve ShowCheckAll.
         */
        mShowCheckAll = mSelectContactsLimit > 0 ? false :
                intent.getBooleanExtra(EXTRA_SHOW_CHECK_ALL, true);

        /*
         * Retrieve OnlyWithPhoneNumbers.
         */
        mOnlyWithPhoneNumbers = intent.getBooleanExtra(EXTRA_ONLY_CONTACTS_WITH_PHONE, false);

        /*
         * Retrieve LimitReachedMessage.
         */
        String limitMsg = intent.getStringExtra(EXTRA_LIMIT_REACHED_MESSAGE);
        if(limitMsg != null){
            mLimitReachedMessage = limitMsg;
        }
        else {
            mLimitReachedMessage = getString(R.string.cp_limit_reached, mSelectContactsLimit);
        }

        /*
         * Retrieve ContactDescription.
         */
        enumName = intent.getStringExtra(EXTRA_CONTACT_DESCRIPTION);
        mDescription = ContactDescription.lookup(enumName);
        mDescriptionType = intent.getIntExtra(EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);

        /*
         * Retrieve ContactSortOrder.
         */
        enumName = intent.getStringExtra(EXTRA_CONTACT_SORT_ORDER);
        mSortOrder = ContactSortOrder.lookup(enumName);

        setTheme(mThemeResId);
        setContentView(R.layout.cp_contact_tab_layout);

        // initialize TabLayout
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabContent);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        TabLayout.Tab tabContacts = tabLayout.newTab();
        tabContacts.setText(R.string.cp_contact_tab_title);
        tabLayout.addTab(tabContacts);

        TabLayout.Tab tabGroups = tabLayout.newTab();
        tabGroups.setText(R.string.cp_group_tab_title);
        tabLayout.addTab(tabGroups);

        // initialize ViewPager
        final ViewPager viewPager = (ViewPager) findViewById(R.id.tabPager);
        mAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(),
                mSortOrder, mBadgeType, mDescription, mDescriptionType);
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

        outState.putInt("mThemeResId", mThemeResId);

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
            String title = getString(R.string.cp_actionmode_selected, mNrOfSelectedContacts);
            setTitle(title);
        }
    }

    // ****************************************** Option Menu *******************************************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cp_contact_picker, menu);
        if(!mShowCheckAll){
            MenuItem checkAllBtn = menu.findItem(R.id.action_check_all);
            checkAllBtn.setVisible(mShowCheckAll);
        }
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
        else if( id == R.id.action_done) {
            onDone();
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

        // return only checked groups
        List<Group> groups = new ArrayList<>();
        if (mGroups != null) {
            for (Group group : mGroups) {
                if (group.isChecked()) {
                    groups.add(group);
                }
            }
        }

        Intent data = new Intent();
        data.putExtra(RESULT_CONTACT_DATA, (Serializable) contacts);
        data.putExtra(RESULT_GROUP_DATA, (Serializable) groups);
        setResult(Activity.RESULT_OK, data);
        finish();
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
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
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
            FORMATTED_ADDRESS,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
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
            ContactsContract.Groups.TITLE};
    private static final String GROUPS_SELECTION = ContactsContract.Groups.DELETED + " = 0";
    private static final String GROUPS_SORT = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = "";
        if(mOnlyWithPhoneNumbers){
            selection = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        }
        switch(id) {
            case CONTACTS_LOADER_ID:
                return new CursorLoader(this, CONTACTS_URI, CONTACTS_PROJECTION,
                        selection, null, CONTACTS_SORT);
            case CONTACT_DETAILS_LOADER_ID:
                return new CursorLoader(this, CONTACT_DETAILS_URI, CONTACT_DETAILS_PROJECTION,
                        selection, null, null);
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

    private Comparator<ContactImpl> mContactComparator = new Comparator<ContactImpl>() {
        @Override
        public int compare(ContactImpl lhs, ContactImpl rhs) {
            switch(mSortOrder) {
                case FIRST_NAME: return lhs.getFirstName().compareToIgnoreCase(rhs.getFirstName());
                case LAST_NAME: return lhs.getLastName().compareToIgnoreCase(rhs.getLastName());
                default: return lhs.getDisplayName().compareToIgnoreCase(rhs.getDisplayName());
            }
        }
    };

    private void readContacts(Cursor cursor) {
        mContacts.clear();
        mContactsByLookupKey.clear();
        mNrOfSelectedContacts = 0;

        int count = 0;
        if (cursor.moveToFirst()) {
            cursor.moveToPrevious();
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

                // update the ui once some contacts have loaded
                if (++count >= BATCH_SIZE) {
                    sortAndPostCopy(mContacts);
                    count = 0;
                }
            }
        }

        if (count > 0) {
            sortAndPostCopy(mContacts);
        }

        updateTitle();
    }

    /**
     * For concurrency reasons we create a copy of the contacts list before it's sorted and sent to
     * the ContactFragment. Note: this affects only the list itself, individual contacts are still
     * shared between the Activity and the Fragment (and its adapter).
     */
    private void sortAndPostCopy(List<ContactImpl> contacts) {
        List<ContactImpl> copy = new ArrayList<>();
        copy.addAll(contacts);
        Collections.sort(copy, mContactComparator);
        ContactsLoaded.post(copy);
    }

    private void readContactDetails(Cursor cursor) {
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

        sortAndPostCopy(mContacts);
        joinContactsAndGroups(mContacts);
    }

    private void readContactDetails(Cursor cursor, ContactImpl contact) {
        String mime = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
        if (mime.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            if (email != null) {
                contact.setEmail(type, email);
            }
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            if (phone != null) {
                contact.setPhone(type, phone);
            }
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
            String address = cursor.getString(cursor.getColumnIndex(FORMATTED_ADDRESS));
            int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            if (address != null) {
                contact.setAddress(type, address.replaceAll("\\n", ", "));
            }
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
            String firstName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String lastName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            if (firstName != null) contact.setFirstName(firstName);
            if (lastName != null) contact.setLastName(lastName);
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)) {
            int groupId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
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

        Collections.sort(mVisibleGroups, new Comparator<GroupImpl>() {
            @Override
            public int compare(GroupImpl lhs, GroupImpl rhs) {
                return lhs.getDisplayName().compareTo(rhs.getDisplayName());
            }
        });

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
            if (!wasChecked && isChecked && mSelectContactsLimit > 0 &&
                    mNrOfSelectedContacts+1 > mSelectContactsLimit ){
                contact.setChecked(false, true);
                ContactsLoaded.post(mContacts);
                Toast.makeText(ContactPickerActivity.this, mLimitReachedMessage,
                        Toast.LENGTH_LONG).show();
            } else if (wasChecked != isChecked) {
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
            if (!wasChecked && isChecked && mSelectContactsLimit > 0 &&
                    mNrOfSelectedContacts + group.getContacts().size() > mSelectContactsLimit ){
                group.setChecked(false, true);
                GroupsLoaded.post(mVisibleGroups);
                Toast.makeText(ContactPickerActivity.this, mLimitReachedMessage,
                        Toast.LENGTH_LONG).show();
            } else {
                // check/un-check the group's contacts
                processContactSelection(group, isChecked);
                // check if we need to deselect some groups
                processGroupSelection();
            }


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
