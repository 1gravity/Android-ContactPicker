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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.picture.ContactPictureManager;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    private List<? extends Contact> mContacts;

    final private ContactPictureType mContactPictureType;
    final private ContactDescription mContactDescription;
    final private int mContactDescriptionType;
    final private ContactPictureManager mContactPictureLoader;

    private LayoutInflater mInflater;

    public ContactAdapter(Context context, List<Contact> contacts,
                          ContactPictureType contactPictureType,
                          ContactDescription contactDescription,
                          int contactDescriptionType) {
        mContacts = contacts;
        mContactPictureType = contactPictureType;
        mContactDescription = contactDescription;
        mContactDescriptionType = contactDescriptionType;
        mContactPictureLoader = new ContactPictureManager(context, mContactPictureType == ContactPictureType.ROUND);
    }

    public void setData(List<? extends Contact> contacts) {
        mContacts = contacts;
        notifyDataSetChanged();
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View view = mInflater.inflate(R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(view, mContactPictureLoader, mContactPictureType,
                                     mContactDescription, mContactDescriptionType);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        if (mContacts != null) {
            holder.bind( mContacts.get(position) );
        }
    }

    @Override
    public void onViewRecycled(ContactViewHolder holder) {
        holder.onRecycled();
    }

    @Override
    public int getItemCount() {
        return mContacts == null ? 0 : mContacts.size();
    }

    @Override
    public long getItemId(int position) {
        return mContacts == null ? super.getItemId(position) : mContacts.get(position).getId();
    }

}
