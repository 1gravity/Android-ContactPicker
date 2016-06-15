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

package com.onegravity.contactpicker;

import java.io.Serializable;

/**
 * A ContactElement (single contact or group) always has a unique id and a display name.
 * It also can be checked/unchecked and can be observer by attaching an OnContactCheckedListener.
 */
public interface ContactElement extends Serializable {

    long getId();

    String getDisplayName();

    boolean isChecked();

    void setChecked(boolean checked, boolean suppressListenerCall);

    void addOnContactCheckedListener(OnContactCheckedListener listener);

    boolean matchesQuery(String[] queryStrings);

}
