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

package com.onegravity.contactpicker.picture.cache;

import android.net.Uri;

/**
 * Used to cache the uri for contact photos
 */
public class ContactUriCache extends InMemoryCache<String, Uri> {

    private static ContactUriCache sInstance;

    // we need to synchronize this to make sure there's no race condition instantiating the cache
    public synchronized static ContactUriCache getInstance() {
        if (sInstance == null) {
            sInstance = new ContactUriCache();
        }
        return sInstance;
    }

    /**
     * Get a photo Uri from the cache.
     *
     * @return Null if the Uri is not in the cache.
     * Uri.Empty if it's not in the cache with a previous miss meaning we already tried to
     * retrieve the query before and we failed, so there's really no point trying again
     * A valid Uri that can be used to retrieve the image.
     */
    public static Uri getUriFromCache(String key) {
        return getInstance().get(key, Uri.EMPTY);
    }

    private ContactUriCache() {
        // purge after 10 minutes of being idle, holds a maximum of 100 URIs
        super(1000 * 60 * 10, 100);
    }

}
