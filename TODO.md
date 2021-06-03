# code
## 1.1.1
- make dialogs normal fragments
- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
  - fab -> new txn
  - action mode delete -> alertDialog (something basic like fade+scale or fade+slide)
  - action mode edit -> edit txn/person
  - editTxn money<>item https://developer.android.com/guide/topics/resources/drawable-resource#Transition
  
- add and use action from item list to new transaction dialog
- move more logic to ViewModels
  - EditTransactionFragment: switchIsMonetary status to viewModel

## 1.2.0
- add option to mark lent item as returned
  - make item list filterable (all/only unreturned/only returned)
- add sum of selected items to subtitle of contextual action bar <-- update readme!!!
- do not overwrite backups but add counter (how to handle restore when there are multiple files?)

## 1.3.0
- contacts integration

## later / unassigned
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- move Total Header to view subclass (to remove duplicate code from list fragments, https://developer.android.com/training/custom-views/create-view)
- use RxJava
- create a way to get from person sum list directly to filtered items list of a person
- make nicer quick-guide display
- move transition durations to integer resource

# other
## 1.1.1
- move from triple-t to fastlane (better fdroid support and per-release changelogs)

## later



# release-checklist
- update whatsnew (2x)
- update CHANGELOG.md
- check build.gradle version+version code
- build release (!) apk, rename debitum-x.x.x.apk
- tag release
