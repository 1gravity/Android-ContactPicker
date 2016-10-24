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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onegravity.contactpicker.ContactElement;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.io.Serializable;
import java.util.List;

public class DemoActivity extends BaseActivity {

    private static final String EXTRA_DARK_THEME = "EXTRA_DARK_THEME";
    private static final String EXTRA_GROUPS = "EXTRA_GROUPS";
    private static final String EXTRA_CONTACTS = "EXTRA_CONTACTS";

    private static final int REQUEST_CONTACT = 0;

    private boolean mDarkTheme;
    private List<Contact> mContacts;
    private List<Group> mGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // read parameters either from the Intent or from the Bundle
        if (savedInstanceState != null) {
            mDarkTheme = savedInstanceState.getBoolean(EXTRA_DARK_THEME);
            mGroups = (List<Group>) savedInstanceState.getSerializable(EXTRA_GROUPS);
            mContacts = (List<Contact>) savedInstanceState.getSerializable(EXTRA_CONTACTS);
        }
        else {
            Intent intent = getIntent();
            mDarkTheme = intent.getBooleanExtra(EXTRA_DARK_THEME, false);
            mGroups = (List<Group>) intent.getSerializableExtra(EXTRA_GROUPS);
            mContacts = (List<Contact>) intent.getSerializableExtra(EXTRA_CONTACTS);
        }

        setTheme(mDarkTheme ? R.style.Theme_Dark : R.style.Theme_Light);

        // set layout
        setContentView(R.layout.main);

        // configure "pick contact(s)" button
        ImageButton button = (ImageButton) findViewById(R.id.pick_contact);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DemoActivity.this, ContactPickerActivity.class)
                            .putExtra(ContactPickerActivity.EXTRA_THEME, mDarkTheme ?
                                    R.style.Theme_Dark : R.style.Theme_Light)

                            .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE,
                                      ContactPictureType.ROUND.name())

                            .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION,
                                      ContactDescription.ADDRESS.name())
                            .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
                            .putExtra(ContactPickerActivity.EXTRA_SELECT_CONTACTS_LIMIT, 0)
                            .putExtra(ContactPickerActivity.EXTRA_ONLY_CONTACTS_WITH_PHONE, false)

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

        // populate contact list
        populateContactList(mGroups, mContacts);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(EXTRA_DARK_THEME, mDarkTheme);
        if (mGroups != null) {
            outState.putSerializable(EXTRA_GROUPS, (Serializable) mGroups);
        }
        if (mContacts != null) {
            outState.putSerializable(EXTRA_CONTACTS, (Serializable) mContacts);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK && data != null &&
                (data.hasExtra(ContactPickerActivity.RESULT_GROUP_DATA) ||
                 data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA))) {

            // we got a result from the contact picker --> show the picked contacts
            mGroups = (List<Group>) data.getSerializableExtra(ContactPickerActivity.RESULT_GROUP_DATA);
            mContacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            populateContactList(mGroups, mContacts);
        }
    }

    private void populateContactList(List<Group> groups, List<Contact> contacts) {
        // we got a result from the contact picker --> show the picked contacts
        TextView contactsView = (TextView) findViewById(R.id.contacts);
        SpannableStringBuilder result = new SpannableStringBuilder();

        try {
            if (groups != null && ! groups.isEmpty()) {
                result.append("GROUPS\n");
                for (Group group : groups) {
                    populateContact(result, group, "");
                    for (Contact contact : group.getContacts()) {
                        populateContact(result, contact, "    ");
                    }
                }
            }
            if (contacts != null && ! contacts.isEmpty()) {
                result.append("CONTACTS\n");
                for (Contact contact : contacts) {
                    populateContact(result, contact, "");
                }
            }
        }
        catch (Exception e) {
            result.append(e.getMessage());
        }

        contactsView.setText(result);
    }

    private void populateContact(SpannableStringBuilder result, ContactElement element, String prefix) {
        //int start = result.length();
        String displayName = element.getDisplayName();
        result.append(prefix);
        result.append(displayName + "\n");
        //result.setSpan(new BulletSpan(15), start, result.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.contact_picker_demo, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int textId = mDarkTheme ? R.string.light_theme : R.string.dark_theme;
        menu.findItem(R.id.action_theme).setTitle(textId);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            mDarkTheme = ! mDarkTheme;
            Intent intent = new Intent(this, this.getClass())
                    .putExtra(EXTRA_DARK_THEME, mDarkTheme);
            if (mGroups != null) {
                intent.putExtra(EXTRA_GROUPS, (Serializable) mGroups);
            }
            if (mContacts != null) {
                intent.putExtra(EXTRA_CONTACTS, (Serializable) mContacts);
            }
            startActivity(intent);
            finish();
            return true;
        }

        return false;
    }

}
