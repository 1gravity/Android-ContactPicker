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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.onegravity.contactpicker.ContactCheckedEvent;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.picture.ContactBadge;
import com.onegravity.contactpicker.picture.ContactPictureManager;
import com.onegravity.contactpicker.picture.ContactPictureType;

public class ContactViewHolder extends RecyclerView.ViewHolder {

    private ContactBadge mBadge;
    private CheckBox mSelect;
    private TextView mName;
    private TextView mDescription;

    private ContactPictureManager mContactPictureLoader;
    private ContactPictureType mContactPictureType;

    ContactViewHolder(View root, ContactPictureType contactPictureType) {
        super(root);

        mSelect = (CheckBox) root.findViewById(R.id.select);
        mBadge = (ContactBadge) root.findViewById(R.id.contact_badge);
        mName = (TextView) root.findViewById(R.id.name);
        mDescription = (TextView) root.findViewById(R.id.description);

        mContactPictureLoader = new ContactPictureManager(root.getContext(),
                contactPictureType == ContactPictureType.ROUND);
        mContactPictureType = contactPictureType;
    }

    void bind(Contact contact) {
        mName.setText(contact.getDisplayName());
        mDescription.setText(contact.getEmail());

        // contact picture
        if (mContactPictureType != ContactPictureType.NONE) {
            String email = contact.getEmail();
            if (email != null) {
                mBadge.assignContactFromEmail(email, true);
                mContactPictureLoader.loadContactPicture(contact, mBadge);
                mBadge.setVisibility(View.VISIBLE);
            }
            else {
                mBadge.setVisibility(View.INVISIBLE);
            }
        }
        else {
            mBadge.setVisibility(View.GONE);
        }

        mSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //contact.setChecked(isChecked);
                ContactCheckedEvent.post();
            }
        });
    }

}
