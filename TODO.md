# code
## 1.1.0
- unify transactionList and PersonSumList (abstract base class)
- use contextual action bar (and drop toolbar in main activity completely?)
    - style
    - make floating
    - add title
- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
    - move transition durations to integer resource

## later / unassigned
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- move Total Header to view subclass (to remove duplicate code from list fragments, https://developer.android.com/training/custom-views/create-view)
- use RxJava
- create a way to get from person sum list directly to filtered items list of a person
- contacts integration
- move more logic to ViewModels
- make nicer quick-guide display

# visuals
## 1.1.0
## later

# other


# release-checklist
- update whatsnew (2x)
- update CHANGELOG.md
- check gradle.build version+version code
- build release (!) apk, rename debitum-x.x.x-unsigned.apk
- tag release
