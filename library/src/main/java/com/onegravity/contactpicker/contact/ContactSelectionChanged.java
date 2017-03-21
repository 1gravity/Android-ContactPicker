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

package com.onegravity.contactpicker.contact;

import org.greenrobot.eventbus.EventBus;

/**
 * The contact selection has changed.
 *
 * We need to:
 * - recalculate the number of selected contacts
 * - deselect groups if no contact is selected
 *
 * We could just use the regular listener mechanism to propagate changes for checked/un-checked
 * contacts but if the user selects "Check All / Un-check All" this would trigger a call for each
 * contact. Therefore the listener call is suppressed and a ContactSelectionChanged fired once all
 * contacts are checked / un-checked.
 *
 * Publisher: ContactFragment
 * Subscriber: ContactPickerActivity
 */
public class ContactSelectionChanged {

    private static final ContactSelectionChanged sEvent = new ContactSelectionChanged();

    static void post() {
        EventBus.getDefault().post( sEvent );
    }

}
