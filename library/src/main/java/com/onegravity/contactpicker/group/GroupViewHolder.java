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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.onegravity.contactpicker.R;

public class GroupViewHolder extends RecyclerView.ViewHolder {

    private CheckBox mSelect;
    private TextView mName;
    private TextView mDescription;

    GroupViewHolder(View root) {
        super(root);
        mSelect = (CheckBox) root.findViewById(R.id.select);
        mName = (TextView) root.findViewById(R.id.name);
        mDescription = (TextView) root.findViewById(R.id.description);
    }

    void bind(Group group) {
        mName.setText(group.getDisplayName());
    }

}
