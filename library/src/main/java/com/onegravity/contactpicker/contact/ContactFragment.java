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
import android.os.Parcelable;
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

import com.onegravity.contactpicker.ContactBase;
import com.onegravity.contactpicker.ContactsCheckedEvent;
import com.onegravity.contactpicker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ContactFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ContactBase.OnContactsCheckedListener {

    private TreeMap<Integer, Bundle> mLoaderIds;

    /*
     * List of all contacts.
     */
    private List<Contact> mContacts = new ArrayList<>();

    /*
     * Mao of all contacts by lookup key (ContactsContract.Contacts.LOOKUP_KEY).
     */
    private Map<String, Contact> mContactsByLookupKey = new HashMap<>();

    private int mSelectedContacts = 0;

    private ContactAdapter mAdapter;

    private static final int BATCH_SIZE = 25;

    // ****************************************** Lifecycle Methods *******************************************

    /**
     * Fragment for opening the message list for a "real" account/folder
     */
    public static ContactFragment newInstance() {
        ContactFragment frag = new ContactFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public ContactFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        if (savedInstanceState != null) {
            // some devices don't seem to retain the fragment even with
            // setRetainInstance(true) so we have to do this the "old fashioned" way
            restoreInstanceState(savedInstanceState);
        }

        if (mLoaderIds == null) {
            mLoaderIds = new TreeMap<>();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        saveLoaderParams(outState, "mLoaderIds", mLoaderIds);
    }

    private void saveLoaderParams(Bundle outState, String key, Map<Integer, ? extends Parcelable> params) {
        Map<Integer, ? extends Parcelable> loaderIds = new HashMap<>(params);
        ArrayList<Integer> tmp = new ArrayList<>();
        tmp.addAll(loaderIds.keySet());
        outState.putIntegerArrayList(key, tmp);
        for (int loaderId : tmp) {
            Parcelable p = loaderIds.get(loaderId);
            outState.putParcelable(key + loaderId, p);
        }
    }

    private void restoreInstanceState(Bundle state) {
        // restore mLoaderIds
        mLoaderIds = new TreeMap<>();
        ArrayList<Integer> tmp = state.getIntegerArrayList("mLoaderIds");
        for (int loaderId : tmp) {
            Bundle bundle = state.getParcelable("mLoaderIds" + loaderId);
            mLoaderIds.put(loaderId, bundle);
        }
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

        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
//        EventBus.getDefault().unregister(this);
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
            ContactsContract.Contacts.PHOTO_URI,
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
        mContacts.clear();
        mContactsByLookupKey.clear();
        mAdapter.setData(mContacts);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch(loader.getId()) {
            case CONTACTS_LOADER_ID:
                readContacts(cursor);
                getLoaderManager().initLoader(CONTACT_DETAILS_LOADER_ID, null, this);
                break;
            case CONTACT_DETAILS_LOADER_ID:
                readContactDetails(cursor);
                break;
        }
    }

    private void readContacts(Cursor cursor) {
        Log.e("1gravity", "***************************************************************");
        Log.e("1gravity", "* CONTACTS                                                    *");
        Log.e("1gravity", "***************************************************************");

        if (cursor.moveToFirst()) {
            cursor.moveToPrevious();
            int count = 0;
            while (cursor.moveToNext()) {
                Contact contact = Contact.fromCursor(cursor);
                mContacts.add(contact);

                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                mContactsByLookupKey.put(lookupKey, contact);
                contact.setOnContactCheckedListener(this);

                Log.e("1gravity", "lookupKey: " + lookupKey);
//                Log.e("1gravity", "id: " + contact.getId());
                Log.e("1gravity", "displayName: " + contact.getDisplayName());
                Log.e("1gravity", "first name: " + contact.getFirstName());
                Log.e("1gravity", "last name: " + contact.getLastName());
                Log.e("1gravity", "photoUri: " + contact.getPhotoUri());

                if (++count >= BATCH_SIZE) {
                    mAdapter.setData(mContacts);
                }
            }
        }

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

                Log.e("1gravity", "lookupKey: " + lookupKey);
                Contact contact = mContactsByLookupKey.get(lookupKey);
                if (contact != null) {
                    updateContact(cursor, contact);
                }
            }
        }

        mAdapter.setData(mContacts);
    }

    private void updateContact(Cursor cursor, Contact contact) {
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
            Log.e("1gravity", "  first name: "  + firstName);
            Log.e("1gravity", "  last name: "  + lastName);
        }
        else if (mime.equals(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)) {
            Long groupId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID));
            Log.e("1gravity", "  groupId: "  + groupId);
        }
    }

    @Override
    public void onContactChecked(ContactBase contact, boolean wasChecked, boolean isChecked) {
        onContactsChecked(Collections.singletonList(contact), wasChecked, isChecked);
    }

    @Override
    public void onContactsChecked(List<ContactBase> contacts, boolean wasChecked, boolean isChecked) {
        if (wasChecked != isChecked) {
            int nrOfContacts = contacts.size();
            if (isChecked) {
                mSelectedContacts =  Math.min(mContacts.size(), mSelectedContacts + nrOfContacts);
            }
            else {
                mSelectedContacts = Math.max(0, mSelectedContacts - nrOfContacts);
            }
            ContactsCheckedEvent.post(mSelectedContacts);
        }
    }

    // ****************************************** Option Menu Methods *******************************************

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        // TODO: 5/16/2016
        return super.onOptionsItemSelected(item);
    }

}
