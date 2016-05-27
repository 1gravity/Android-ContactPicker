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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.io.Serializable;
import java.util.List;

public class DemoActivity extends BaseActivity {

    private static final String EXTRA_LIGHT_THEME = "EXTRA_LIGHT_THEME";
    private static final String EXTRA_CONTACTS = "EXTRA_CONTACTS";

    private static final int REQUEST_CONTACT = 0;

    private boolean mUseLightTheme;
    private List<Contact> mContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // read parameters either from the Intent or from the Bundle
        if (savedInstanceState != null) {
            mUseLightTheme = savedInstanceState.getBoolean("mUseLightTheme");
            mContacts = (List<Contact>) savedInstanceState.getSerializable("mContacts");
        }
        else {
            Intent intent = getIntent();
            mUseLightTheme = intent.getBooleanExtra(EXTRA_LIGHT_THEME, false);
            mContacts = (List<Contact>) intent.getSerializableExtra(EXTRA_CONTACTS);
        }

        setTheme(mUseLightTheme ? R.style.Theme_Light : R.style.Theme_Dark);

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
                                      ContactPictureType.ROUND.name())

                            .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION,
                                      ContactDescription.ADDRESS.name())

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
        populateContactList(mContacts);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("mUseLightTheme", mUseLightTheme);
        if (mContacts != null) {
            outState.putSerializable("mContacts", (Serializable) mContacts);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK &&
            data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {

            // we got a result from the contact picker --> show the picked contacts
            mContacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            populateContactList(mContacts);

        }
    }

    private void populateContactList(List<Contact> contacts) {
        if (contacts == null || mContacts.isEmpty()) return;

        // we got a result from the contact picker --> show the picked contacts
        TextView contactsView = (TextView) findViewById(R.id.contacts);
        SpannableStringBuilder result = new SpannableStringBuilder();
        try {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.contact_picker_demo, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        int textId = mUseLightTheme ? R.string.dark_theme : R.string.light_theme;
        menu.findItem(R.id.action_theme).setTitle(textId);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            mUseLightTheme = ! mUseLightTheme;
            Intent intent = new Intent(this, this.getClass())
                    .putExtra(EXTRA_LIGHT_THEME, mUseLightTheme);
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
