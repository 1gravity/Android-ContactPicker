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

package com.onegravity.contactpicker.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.contact.Contact;

import java.util.Set;

public class GroupViewHolder extends RecyclerView.ViewHolder {

    private View mRoot;
    private CheckBox mSelect;
    private TextView mName;

    GroupViewHolder(View root) {
        super(root);

        mRoot = root;
        mSelect = (CheckBox) root.findViewById(R.id.select);
        mName = (TextView) root.findViewById(R.id.name);
    }

    void bind(final Group group) {
        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelect.toggle();
            }
        });

        // main text / title
        Context context = mRoot.getContext();
        Set<Contact> contacts = group.getContacts();
        String dispName = group.getDisplayName();
        String name = contacts.isEmpty() ?
                context.getString(R.string.group_desription_no_contacts, dispName) :
                context.getString(R.string.group_desription, dispName, contacts.size());
        mName.setText(name);

        // check box
        mSelect.setChecked( group.isChecked() );
        mSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                group.setChecked(isChecked);
            }
        });

    }

}
