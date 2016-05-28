# Android-ContactPicker

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

The Android Contact Picker is a beautifully designed component to pick one or several contacts (including groups) from the Android contacts list.
A demo app can be found here: https://play.google.com/store/apps/details?id=com.onegravity.contactpicker.demo.

![Contact picker light theme](art/contact_list_light_framed_small?raw=true "Color picker dark theme") ![Contact picker dark theme](art/groups_list_dark_framed_small?raw=true "Color picker light theme")

Setup
-----
####**Dependencies**

Add this to your Gradle build file:
```
dependencies {
    compile 'com.1gravity:android-contactpicker:1.0.0'
}
```

#### **Proguard**

If you use Proguard in your app, please add the following lines to your configuration file:
```
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
```

####**Theming**

TBW
Use one of the AppCompat themes (Theme.AppCompat, Theme.AppCompat.Light or one of its derivatives).

Issues
------

If you have an issues with this library, please open a issue here: https://github.com/1gravity/Android-ColorPicker/issues and provide enough information to reproduce it reliably. The following information needs to be provided:

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
