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

import android.net.Uri;

import com.onegravity.contactpicker.ContactElement;

import java.util.Set;

/**
 * This interface describes a single contact.
 * It only provides read methods to make sure no class outside this package can modify it.
 * Write access is only possible through the ContactImpl class which has package access.
 */
public interface Contact extends ContactElement {

	String getFirstName();

	String getLastName();

	String getEmail();

	String getPhone();

	String getAddress();

	String getKey();

	Uri getPhotoUri();

    Set<Long> getGroupIds();
}
