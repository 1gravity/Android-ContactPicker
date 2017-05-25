# Android-ContactPicker

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

The Android Contact Picker is a beautifully designed component to pick one or several contacts (including groups) from the Android contacts list.<br>
A demo app can be found here: https://play.google.com/store/apps/details?id=com.onegravity.contactpicker.demo.

![Contact picker light theme](art/contact_list_light_framed_small.png?raw=true "Contact picker light theme")
![Contact picker dark theme](art/groups_list_dark_framed_small.png?raw=true "Contact picker dark theme")

Setup
-----

####**Dependencies**

Add this to your Gradle build file:
```
dependencies {
    compile 'com.1gravity:android-contactpicker:1.3.2'
}
```

####**Manifest**

Define the contact picker Activity and the READ_CONTACTS permission in the manifest:
```
<uses-permission android:name="android.permission.READ_CONTACTS" />

<activity
    android:name="com.onegravity.contactpicker.core.ContactPickerActivity"
    android:enabled="true"
    android:exported="false" >

    <intent-filter>
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent-filter>
</activity>
```

Note: if your app targets Android 6 and higher (API level 23 and above), you also need to request the contacts permission at run-time.
Check out the demo app for an example how this can be done.
 
####**Usage**

Call the contact picker like this (see Javadoc for a description of the individual parameters):
```
Intent intent = new Intent(this, ContactPickerActivity.class)
    .putExtra(ContactPickerActivity.EXTRA_THEME, mDarkTheme ? R.style.Theme_Dark : R.style.Theme_Light)
    .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
    .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
    .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.ADDRESS.name())
    .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
    .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name());
startActivityForResult(intent, REQUEST_CONTACT);
```

Process the result like this:
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK &&
        data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {
        
        // we got a result from the contact picker

        // process contacts
        List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
        for (Contact contact : contacts) {
            // process the contacts...
        }

        // process groups
        List<Group> groups = (List<Group>) data.getSerializableExtra(ContactPickerActivity.RESULT_GROUP_DATA);
        for (Group group : groups) {
            // process the groups...
        }
    }
}
```

The source code includes a more comprehensive example.<br>Also check out the demo app on Google Play: https://play.google.com/store/apps/details?id=com.onegravity.contactpicker.demo.

####**Intent Extra Parameters**

As seen above in the example, these parameters are chained to the intent where each param descriptor is attached to `ContactPickerActivity`. 
Below is a listing of the parameters and their purpose:

| Parameter  | Description  |
|---|:---|
|  **EXTRA_SELECT_CONTACTS_LIMIT** (int)  |  This parameter will limit the amount of contacts that can be selected per intent. When set to zero, then no limiting will be enforced <br/> Default: `0` |
|  **EXTRA_LIMIT_REACHED_MESSAGE** (String)  |  This parameter sets the text displayed as a toast when the set limit is reached <br/> Default: `You can't pick more than {limit} contacts!` |
|  **EXTRA_SHOW_CHECK_ALL** (Boolean)  |  This parameter decides whether to show/hide the check_all button in the menu. When `EXTRA_SELECT_CONTACTS_LIMIT` > 0, this will be forced to `false`.  <br/> Default: `true` |
|  **EXTRA_ONLY_CONTACTS_WITH_PHONE** (Boolean)  |  This parameter sets the boolean that filters contacts that have no phone numbers <br/> Default: `false` |

####**Theming**

The library supports a dark and a light theme out-of-the-box. In order to do that, it defines a
couple of custom attributes in attrs.xml.
To integrate the contact picker in your app, you need to either extend one of the contact picker
themes (ContactPicker_Theme_Light / ContactPicker_Theme_Dark) or define the custom attributes in
your own theme.

#### **Proguard**

If you use Proguard, please add the following lines to your configuration file:
```
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
```

Issues
------

If you have an issues with this library, please open a issue here: https://github.com/1gravity/Android-ContactPicker/issues and provide enough information to reproduce it reliably. The following information needs to be provided:

* Which version of the SDK are you using?
* Which Android build are you using? (e.g. MPZ44Q)
* What device are you using?
* What steps will reproduce the problem? (Please provide the minimal reproducible test case.)
* What is the expected output?
* What do you see instead?
* Relevant logcat output.
* Optional: Link to any screenshot(s) that demonstrate the issue (shared privately in Drive.)
* Optional: Link to your APK (either downloadable in Drive or in the Play Store.)

License
-------

Copyright 2016 Emanuel Moecklin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
