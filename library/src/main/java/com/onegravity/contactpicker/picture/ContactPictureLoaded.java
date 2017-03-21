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

package com.onegravity.contactpicker.picture;

import android.graphics.Bitmap;

import org.greenrobot.eventbus.EventBus;

/**
 * This event is sent from the ContactPictureLoader to the ContactPictureManager.
 * The latter will then set the ContactBadge's contact picture (if the keys match).
 */
public class ContactPictureLoaded {

    private final String mKey;
    private final ContactBadge mBadge;
    private final Bitmap mBitmap;

    static void post(String key, ContactBadge badge, Bitmap bitmap) {
        ContactPictureLoaded event = new ContactPictureLoaded(key, badge, bitmap);
        EventBus.getDefault().post(event);
    }

    private ContactPictureLoaded(String key, ContactBadge badge, Bitmap bitmap) {
        mKey = key;
        mBadge = badge;
        mBitmap = bitmap;
    }

    ContactBadge getBadge() {
        return mBadge;
    }

    String getKey() {
        return mKey;
    }

    Bitmap getBitmap() {
        return mBitmap;
    }

}
