# 0.6.0
 - Fixed an issue related to memory management

----

# 0.5.7
 - Fixed bug related to order of options and data elements within data entry screen

----

# 0.5.6

 - Ability to choose data elements which should be displayed within
 list of events on main screen.
 - Fixed compatibility issues with latest release of DHIS2 - 2.25.

----

# 0.5.5

 - Fixed bug related to evaluation of program rules across the whole event.
 - Improved syncing of metadata by reducing amount of calls to API.

----

# 0.5.1

 - Back button in Settings and Profile returns user to main screen (Events) instead of closing app.
 - Shows all data elements configured for the program to display in events list. Previously
 limit was three data elements.
 - Automatic background synchronisation is now working and enabled by default. In Settings,
 synchronisation intervals can be changed (4 hrs default) and background synchronisation can be
 disabled. During background synchronisation a notification is shown in the Android notification
 area. The notification can be disabled in Settings as well.
 - Fixed issue with progress indicator in events screen not hiding after completing synchronization.
 - Fixed bug related to program rule evaluation in data entry screen.

----

# 0.5.0.2

 - fixed crash on log-out on pre-lollipop android devices
 - enabled crashlytics support
 - improved performance on main screen
 - reduced application size
