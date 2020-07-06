# This project is now DEPRECATED
The app is not supported anymore so there won't be new versions in the future. Moreover, any pending or future bug won't be fixed.

# dhis2-android-eventcapture
Android application for DHIS 2 for capturing events.

Get the APK from the release page:

https://github.com/dhis2/dhis2-android-eventcapture/releases

# Testing
If you want to try the application out with a demo database, you can use the following:
- Server: https://play.dhis2.org/demo
- Username: android
- Password: Android123

# How to Download and Set up the development environment in Android Studio
The dhis2-android-sdk project https://github.com/dhis2/dhis2-android-sdk folder should be in a subfolder named sdk inside dhis2-android-eventcapture. It is configured as a git submodule, so it will be automatically included when cloned using --recursive. 
 
Currently, the compatibility is guaranteed with 2.27, 2.28 and 2.29 servers, use develop branch in dhis2-android-eventcapture and event-capture branch in dhis2-android-sdk repositories.
 
When cloning from zero, it's strongly recommended to do it as follows:
 
```
git clone --recursive -b develop git@github.com:dhis2/dhis2-android-eventcapture.git
cd dhis2-android-eventcapture/sdk
git checkout event-capture
```
 
Then open Android Studio, select "File" -> "Open", and select the build.gradle in dhis2-android-eventcapture/
