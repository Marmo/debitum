# code
## 1.1.0
- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
  - https://developer.android.com/reference/android/widget/PopupWindow
  - fab -> new txn dialog (something basic like fade+scale or fade+slide OR windowEnterTransition with MaterialSharedAxis from xml)
  - action mode delete -> alertDialog (something basic like fade+scale or fade+slide)
  - action mode edit -> edit txn/person dialog (something basic like fade+scale or fade+slide)
  - editTxn money<>item TransitionManager.beginDelayedTransition() (https://developer.android.com/training/transitions/#NoScenes) or android:animateLayoutChanges
  
- move internal error messages from Toasts to Log output!(EditTransactionFragment:191, EditPersonFragment:173)
- add and use action from item list to new transaction dialog
- wrap bottom navigation in com.google.android.material.bottomappbar.BottomAppBar and add cradle for fab

## 1.2.0
- add option to mark lent item as returned
  - make item list filterable (all/only unreturned/only returned)
- add sum of selected items to subtitle of contextual action bar <-- update readme!!!
  
## later / unassigned
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- move Total Header to view subclass (to remove duplicate code from list fragments, https://developer.android.com/training/custom-views/create-view)
- use RxJava
- create a way to get from person sum list directly to filtered items list of a person
- contacts integration
- move more logic to ViewModels
- make nicer quick-guide display
- move transition durations to integer resource

# visuals
## 1.1.0
## later

# other


# release-checklist
- update whatsnew (2x)
- update CHANGELOG.md
- check gradle.build version+version code
- build release (!) apk, rename debitum-x.x.x.apk
- tag release
