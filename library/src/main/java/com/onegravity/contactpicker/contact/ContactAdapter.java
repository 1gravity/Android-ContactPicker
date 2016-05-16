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

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.onegravity.contactpicker.ContactBase;
import com.onegravity.contactpicker.ContactCheckedEvent;
import com.onegravity.contactpicker.ContactFilter;
import com.onegravity.contactpicker.ContactPickerActivity;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.picture.ContactBadge;
import com.onegravity.contactpicker.picture.ContactPictureType;
import com.onegravity.contactpicker.picture.ContactPictureManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    private LayoutInflater mInflater;

    private List<Contact> mContacts;

    private ContactPictureManager mContactPictureLoader;
    private ContactPictureType mContactPictureType;

    private int mSelectedContacts = 0;

    private ContactFilter mFilter;

    // position --> label
    private LinkedHashMap<Integer, String> mSectionLabels;

    private boolean mNotifyOnChange = true;

    public ContactAdapter(List<Contact> contacts) {
        mContacts = contacts;
        mContactPictureType = ContactPickerActivity.getContactBadgeType();
    }

    public void setData(List<Contact> contacts) {
        mContacts = contacts;
        notifyDataSetChanged();
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View view = mInflater.inflate(R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(view, mContactPictureType);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        if (mContacts != null) {
            holder.bind( mContacts.get(position) );
        }
    }

    @Override
    public int getItemCount() {
        return mContacts == null ? 0 : mContacts.size();
    }

    @Override
    public long getItemId(int position) {
        return mContacts == null ? super.getItemId(position) : mContacts.get(position).getId();
    }

    public int getNrOfSelectedContacts() {
        return mSelectedContacts;
    }

    public ArrayList<ContactBase> getSelectedContacts() {
        ArrayList<ContactBase> result = new ArrayList<>();
        /*for (ContactBase contact : mContacts) {
            if(contact.isChecked()) {
                result.add(contact);
            }
        }*/
        return result;
    }

    public void checkAll(boolean checked) {
        /*for (ContactBase contact : mContacts) {
            contact.setChecked(checked);
        }*/

        notifyDataSetChanged();
        //ContactCheckedEvent.post();
    }

    /*ContactFilter getFilter() {
        return mFilter;
    }*/

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        //Init items
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvData = (TextView) view.findViewById(R.id.description);
        CheckBox cbSelect = (CheckBox) view.findViewById(R.id.select);
        view.findViewById(R.id.select_container).setVisibility(View.VISIBLE);

        String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        //Divide display name to first and last
        String[] names = displayName != null ? displayName.split("\\s+") : new String[]{"---", "---"};
        String firstName = names.length >= 1 ? names[0] : displayName;
        String lastName = names.length >= 2 ? names[1] : "";
        //return StringUtils.isNullOrEmpty(email) ? null : new Contact(id, firstName, lastName, displayName, email);

        // contact picture
        ContactBadge badge = (ContactBadge) view.findViewById(R.id.contact_badge);
        if (mContactPictureType != ContactPictureType.NONE) {
            /*String email = ((Contact)contact).getEmail();
            if (email != null) {
                badge.assignContactFromEmail(email, true);
                mContactPictureLoader.loadContactPicture(new Address(email), badge);
                badge.setVisibility(View.VISIBLE);
            }
            else {
                badge.setVisibility(View.INVISIBLE);
            }*/
        }
        else {
            badge.setVisibility(View.GONE);
        }

        //Set items
        tvName.setText(displayName);
        tvData.setText("");
        cbSelect.setChecked(false);

        cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                contact.setChecked(isChecked);
                ContactCheckedEvent.post();
            }
        });
    }

    public void onCheckedChanged(ContactBase contact, boolean isChecked) {
        if (isChecked) {
//            mSelectedContacts =  Math.min(mContacts.size(), mSelectedContacts + 1);
        }
        else {
            mSelectedContacts = Math.max(0, mSelectedContacts - 1);
        }
    }

    // ****************************************** SectionIndexer *******************************************

    private synchronized void calculateSections() {
        mSectionLabels.clear();
        String lastFirstChar = "";
        int pos = 0;
/*        for (ContactBase contact : mContacts) {
            String name = contact.getDisplayName();
            String firstChar = StringUtils.isNullOrEmpty(name) ? "-" : name.substring(0, 1).toUpperCase();
            if (! firstChar.equals(lastFirstChar)) {
                mSectionLabels.put(pos, firstChar);
                lastFirstChar = firstChar;
            }
            pos++;
        }*/
    }

/*    @Override
    public synchronized Object[] getSections() {
        int size = mSectionLabels.size();
        return size > 0 ? mSectionLabels.values().toArray( new Object[size] ) : new String[] {""};
    }

    @Override
    public synchronized int getPositionForSection(int sectionIndex) {
        Iterator<Integer> it = mSectionLabels.keySet().iterator();
        for (int i = 0 ; i < sectionIndex && it.hasNext(); i++, it.next()) {}
        return it.hasNext() ? it.next() : 0;
    }

    @Override
    public synchronized int getSectionForPosition(int position) {
        int sectionPos = 0;
        Iterator<Integer> it = mSectionLabels.keySet().iterator();
        for (int pos = 0; pos <= position; pos = it.hasNext() ? it.next() : Integer.MAX_VALUE) {
            sectionPos = pos;
        }
        return sectionPos;
    }*/

}
