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

package com.onegravity.contactpicker.contact;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.contactpicker.SelectionChanged;
import com.onegravity.contactpicker.UpdateTitle;
import com.onegravity.contactpicker.OnContactCheckedListener;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.group.GroupsLoaded;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ContactFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /*
     * List of all contacts.
     */
    private List<ContactImpl> mContacts = new ArrayList<>();

    /*
     * The selected ids are put into the Bundle in onSaveInstanceState, restored in onCreate and
     * then applied to the contacts once they are loaded in onLoadFinished.
     */
    private static final String CONTACT_IDS = "CONTACT_IDS";
    private HashSet<Long> mSelectedIds = new HashSet<>();

    /*
     * Map of all contacts by lookup key (ContactsContract.Contacts.LOOKUP_KEY).
     */
    private Map<String, ContactImpl> mContactsByLookupKey = new HashMap<>();

    private int mSelectedContacts = 0;

    private ContactAdapter mAdapter;

    // update the adapter after a certain amount of contacts has loaded
    private static final int BATCH_SIZE = 25;

    // ****************************************** Lifecycle Methods *******************************************

    /**
     * Fragment for opening the message list for a "real" account/folder
     */
    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    public ContactFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // some devices don't retain fragments
        if (savedInstanceState != null) {
            try {
                mSelectedIds = (HashSet<Long>) savedInstanceState.getSerializable(CONTACT_IDS);
            }
            catch (ClassCastException ignore) {}
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mSelectedIds.clear();;
        for (Contact contact : mContacts) {
            if (contact.isChecked()) {
                mSelectedIds.add( contact.getId() );
            }
        }
        outState.putSerializable(CONTACT_IDS, mSelectedIds);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootLayout = inflater.inflate(R.layout.contact_list, null);
        RecyclerView recyclerView = (RecyclerView) rootLayout.findViewById(R.id.recycler_view);

        // use a LinearLayout for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // create adapter for the RecyclerView
        mAdapter = new ContactAdapter(rootLayout.getContext(), null);
        recyclerView.setAdapter(mAdapter);

        return rootLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        clearData();
        EventBus.getDefault().register(this);
        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
        clearData();
    }

    // ****************************************** Loader Methods *******************************************

    // Contacts
    private static final int CONTACTS_LOADER_ID = 0;
    private static final Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;
    private static final String[] CONTACTS_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI};
    private static final String CONTACTS_SORT = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";

    // Contact Details
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        switch(id) {
            case CONTACTS_LOADER_ID:
                return new CursorLoader(activity, CONTACTS_URI, CONTACTS_PROJECTION, null, null, CONTACTS_SORT);
            case CONTACT_DETAILS_LOADER_ID:
                return new CursorLoader(activity, CONTACT_DETAILS_URI, CONTACT_DETAILS_PROJECTION, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setData(mContacts);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch(loader.getId()) {
            case CONTACTS_LOADER_ID:
                clearData();
                readContacts(cursor);
                getLoaderManager().initLoader(CONTACT_DETAILS_LOADER_ID, null, this);
                break;
            case CONTACT_DETAILS_LOADER_ID:
                readContactDetails(cursor);
                break;
        }
    }

    private void clearData() {
        mContacts.clear();
        mContactsByLookupKey.clear();
    }

    private void readContacts(Cursor cursor) {
        Log.e("1gravity", "***************************************************************");
        Log.e("1gravity", "* CONTACTS                                                    *");
        Log.e("1gravity", "***************************************************************");

        mSelectedContacts = 0;
        int selectedContacts = 0;
        if (cursor.moveToFirst()) {
            cursor.moveToPrevious();
            int count = 0;
            while (cursor.moveToNext()) {
                ContactImpl contact = ContactImpl.fromCursor(cursor);
                mContacts.add(contact);

                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                mContactsByLookupKey.put(lookupKey, contact);

                boolean isChecked = mSelectedIds.contains(contact.getId());
                if (isChecked) mSelectedContacts++;
                contact.setChecked(isChecked, true);

                contact.addOnContactCheckedListener(mContactListener);

                Log.e("1gravity", "lookupKey: " + lookupKey);
                Log.e("1gravity", "id: " + contact.getId());
                Log.e("1gravity", "displayName: " + contact.getDisplayName());
                Log.e("1gravity", "first name: " + contact.getFirstName());
                Log.e("1gravity", "last name: " + contact.getLastName());
                Log.e("1gravity", "photoUri: " + contact.getPhotoUri());

                if (++count >= BATCH_SIZE) {
                    mAdapter.setData(mContacts);
                    if (mSelectedContacts > selectedContacts) {
                        UpdateTitle.post(mSelectedContacts);
                        selectedContacts = mSelectedContacts;
                    }
                }
            }
        }

        mSelectedIds.clear();

        mAdapter.setData(mContacts);
    }

    private void readContactDetails(Cursor cursor) {
        Log.e("1gravity", "***************************************************************");
        Log.e("1gravity", "* CONTACTS DETAILS                                            *");
        Log.e("1gravity", "***************************************************************");

        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToPrevious();
            while (cursor.moveToNext()) {
                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));

                //Log.e("1gravity", "lookupKey: " + lookupKey);
                ContactImpl contact = mContactsByLookupKey.get(lookupKey);
                if (contact != null) {
                    updateContact(cursor, contact);
                }
            }
        }

        ContactsLoaded.post( mContacts );

        mAdapter.setData(mContacts);
    }

    private void updateContact(Cursor cursor, ContactImpl contact) {
        String mime = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
        if (mime.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            String type = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
            contact.setEmail(email);
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

    // ****************************************** Process Contacts / Groups *******************************************

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);

        // add a listener to each group
        for (Group group : event.getGroups()) {
            group.addOnContactCheckedListener(mGroupListener);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SelectionChanged event) {
        mSelectedContacts = 0;
        for (Contact contact : mContacts) {
            if (contact.isChecked()) {
                mSelectedContacts++;
            }
        }

        mAdapter.notifyDataSetChanged();
        UpdateTitle.post(mSelectedContacts);
    }

    private OnContactCheckedListener<Group> mGroupListener = new OnContactCheckedListener<Group>() {
        @Override
        public void onContactChecked(Group group, boolean wasChecked, boolean isChecked) {
            for (Contact contact : group.getContacts()) {
                if (contact.isChecked() != isChecked) {
                    contact.setChecked(isChecked, true);
                }
            }

            SelectionChanged.post();
        }
    };

    private OnContactCheckedListener<Contact> mContactListener = new OnContactCheckedListener<Contact>() {
        @Override
        public void onContactChecked(Contact contact, boolean wasChecked, boolean isChecked) {
            if (wasChecked != isChecked) {
                if (isChecked) {
                    mSelectedContacts =  Math.min(mContacts.size(), mSelectedContacts + 1);
                }
                else {
                    mSelectedContacts = Math.max(0, mSelectedContacts - 1);
                }
                UpdateTitle.post(mSelectedContacts);
            }
        }
    };

    // ****************************************** Option Menu *******************************************

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.menu_check_all) {
            checkAll();
            return true;
        }

        return false;
    }

    private void checkAll() {
        // if all are checked then un-check the contacts, otherwise check them all
        boolean isChecked = mSelectedContacts < mContacts.size();
        for (Contact contact : mContacts) {
            if (contact.isChecked() != isChecked) {
                contact.setChecked(isChecked, true);
            }
        }

        mAdapter.notifyDataSetChanged();
        SelectionChanged.post();
    }

    public List<Contact> getSelectedContacts() {
        List<Contact> result = new ArrayList<>();
        for (Contact contact : mContacts) {
            if (contact.isChecked()) {
                result.add(contact);
            }
        }
        return result;
    }

}
