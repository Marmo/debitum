## 1.2.x
  - make cab-returned-item invisible or "set as not returned" icon when selected item is already returned 
- move more logic to ViewModels
  - EditTransactionFragment: switchIsMonetary status to viewModel
- add option in preferences for standard filter in item list
  
## 1.3.0
- contacts integration
- add sum of selected items to subtitle of contextual action bar <-- update readme!!!
- do not overwrite backups but add counter (how to handle restore when there are multiple files?)

## later / unassigned
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- move Total Header to view subclass (to remove duplicate code from list fragments, https://developer.android.com/training/custom-views/create-view)
- use RxJava
- create a way to get from person sum list directly to filtered items list of a person
- make nicer quick-guide display
- move transition durations to integer resource
- add scrollbar showing the date/month/year while scrolling the transaction list

- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
  - UNCLEAR: how to do all this with dialog fragments???
  - fab -> new txn
  - action mode delete -> alertDialog (something basic like fade+scale or fade+slide)
  - action mode edit -> edit txn/person



# release-checklist (1.2.0/4)
[x] update fastlane changelogs (2x)
[x] update CHANGELOG.md
[-] update screenshots
[x] update licenses
  - ./gradlew checkLicenses
  - ./gradlew updateLicenses
  - ./gradlew generateLicensePage
[x] check build.gradle version+version code
[x] build release (!) apk, rename debitum-x.x.x.apk
[x] tag release
