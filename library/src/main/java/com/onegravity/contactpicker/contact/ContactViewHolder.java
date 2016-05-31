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

import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.onegravity.contactpicker.Helper;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.picture.ContactBadge;
import com.onegravity.contactpicker.picture.ContactPictureManager;
import com.onegravity.contactpicker.picture.ContactPictureType;

public class ContactViewHolder extends RecyclerView.ViewHolder {

    private View mRoot;
    private TextView mName;
    private TextView mDescription;
    private ContactBadge mBadge;
    private CheckBox mSelect;

    final private ContactPictureType mContactPictureType;
    final private ContactDescription mContactDescription;
    final private int mContactDescriptionType;
    final private ContactPictureManager mContactPictureLoader;

    ContactViewHolder(View root, ContactPictureManager contactPictureLoader, ContactPictureType contactPictureType,
                      ContactDescription contactDescription, int contactDescriptionType) {
        super(root);

        mRoot = root;
        mName = (TextView) root.findViewById(R.id.name);
        mDescription = (TextView) root.findViewById(R.id.description);
        mBadge = (ContactBadge) root.findViewById(R.id.contact_badge);
        mSelect = (CheckBox) root.findViewById(R.id.select);

        mContactPictureType = contactPictureType;
        mContactDescription = contactDescription;
        mContactDescriptionType = contactDescriptionType;
        mContactPictureLoader = contactPictureLoader;

        mBadge.setBadgeType(mContactPictureType);
    }

    void bind(final Contact contact) {
        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelect.toggle();
            }
        });

        // main text / title
        mName.setText(contact.getDisplayName());

        // description
        String description = "";
        switch (mContactDescription) {
            case EMAIL:
                description = contact.getEmail(mContactDescriptionType);
                break;
            case PHONE:
                description = contact.getPhone(mContactDescriptionType);
                break;
            case ADDRESS:
                description = contact.getAddress(mContactDescriptionType);
                break;
        }
        mDescription.setText(description);
        mDescription.setVisibility( Helper.isNullOrEmpty(description) ? View.GONE : View.VISIBLE );

        // contact picture
        if (mContactPictureType == ContactPictureType.NONE) {
            mBadge.setVisibility(View.GONE);
        }
        else {
            mContactPictureLoader.loadContactPicture(contact, mBadge);
            mBadge.setVisibility(View.VISIBLE);

            String lookupKey = contact.getLookupKey();
            if (lookupKey != null) {
                Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                mBadge.assignContactUri(contactUri);
            }
        }

        // check box
        mSelect.setOnCheckedChangeListener(null);
        mSelect.setChecked( contact.isChecked() );
        mSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                contact.setChecked(isChecked, false);
            }
        });
    }

    void onRecycled() {
        mBadge.onDestroy();
    }

}
