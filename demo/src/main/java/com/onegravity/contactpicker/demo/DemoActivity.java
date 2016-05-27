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

package com.onegravity.contactpicker.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.util.List;

public class DemoActivity extends BaseActivity {

    private static final int REQUEST_CONTACT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.main);

        // configure "pick contact(s)" button
        ImageButton button = (ImageButton) findViewById(R.id.pick_contact);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DemoActivity.this, ContactPickerActivity.class)
                            .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE,
                                      ContactPictureType.SQUARE.name())
                            .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION,
                                      ContactDescription.EMAIL.name())
                            .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE,
                                      ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                            .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER,
                                      ContactSortOrder.AUTOMATIC.name());
                    startActivityForResult(intent, REQUEST_CONTACT);
                }
            });
        }
        else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK &&
            data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {

            // we got a result from the contact picker --> show the picked contacts
            TextView contactsView = (TextView) findViewById(R.id.contacts);
            SpannableStringBuilder result = new SpannableStringBuilder();
            try {
                List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
                int pos = 0;
                for (Contact contact : contacts) {
                    String displayName = contact.getDisplayName();
                    result.append(displayName + "\n");
                    result.setSpan(new BulletSpan(15), pos, pos + displayName.length() + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    pos += displayName.length() + 1;
                }
            }
            catch (Exception e) {
                result.append(e.getMessage());
            }

            contactsView.setText(result);
        }
    }

}
