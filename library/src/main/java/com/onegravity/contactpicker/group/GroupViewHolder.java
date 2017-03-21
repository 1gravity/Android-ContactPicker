/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
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

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.contact.Contact;

import java.util.Collection;

public class GroupViewHolder extends RecyclerView.ViewHolder {

    private View mRoot;
    private CheckBox mSelect;
    private TextView mName;
    private TextView mDescription;

    GroupViewHolder(View root) {
        super(root);

        mRoot = root;
        mSelect = (CheckBox) root.findViewById(R.id.select);
        mName = (TextView) root.findViewById(R.id.name);
        mDescription = (TextView) root.findViewById(R.id.description);
    }

    void bind(final Group group) {
        mRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelect.toggle();
            }
        });

        // main text / title
        mName.setText(group.getDisplayName());

        // description
        Collection<Contact> contacts = group.getContacts();
        Resources res = mRoot.getContext().getResources();
        String desc = res.getQuantityString(R.plurals.cp_group_description, contacts.size(), contacts.size());
        mDescription.setText(desc);

        // check box
        mSelect.setOnCheckedChangeListener(null);
        mSelect.setChecked( group.isChecked() );
        mSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                group.setChecked(isChecked, false);
            }
        });

    }

}
