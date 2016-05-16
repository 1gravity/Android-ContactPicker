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

package com.onegravity.contactpicker.picture;

import com.onegravity.contactpicker.Helper;
import com.onegravity.contactpicker.contact.Contact;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a contact without a contact picture or one that hasn't been loaded yet.
 */
public class LetterContact {

    private static final Pattern CONTACT_LETTER = Pattern.compile("[^a-zA-Z]*([a-zA-Z]).*");

    /**
     * @see <a href="http://www.google.com/design/spec/style/color.html#color-color-palette">Color palette used</a>
     */
    private final static int CONTACT_COLORS_MATERIAL[] = {
            0xffF44336,
            0xffE91E63,
            0xff9C27B0,
            0xff673AB7,
            0xff3F51B5,
            0xff2196F3,
            0xff03A9F4,
            0xff00BCD4,
            0xff009688,
            0xff4CAF50,
            0xff8BC34A,
            0xffCDDC39,
            0xffFFC107,
            0xffFF9800,
            0xffFF5722,
            0xff795548,
            0xff9E9E9E,
            0xff607D8B
    };

    private final Contact mContact;

    LetterContact(Contact contact) {
        mContact = contact;
    }

    int getContactColor() {
        String key = mContact.getKey();
        int value = Helper.isNullOrEmpty(key) ? mContact.hashCode() : key.hashCode();
        return CONTACT_COLORS_MATERIAL[Math.abs(value) % CONTACT_COLORS_MATERIAL.length];
    }

    char getContactLetter() {
        String key = mContact.getKey();
        Matcher m = CONTACT_LETTER.matcher(key);
        String letter = m.matches() ? m.group(1).toUpperCase(Locale.US) : "?";
        return Helper.isNullOrEmpty(letter) ? '?' : letter.charAt(0);
    }

}
