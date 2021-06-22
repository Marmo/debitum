## 1.3.1/8
- New Transaction hidden behind header bar (#2)
- EditTransactionFragment: Amount format not changed upon switching between item and money (#29)
- add splash screen (https://medium.com/android-news/the-complete-android-splash-screen-guide-c7db82bce565)
- pt translation of Debt settlement description preset
- separate hint for return money
- note about repaid money from readme to guide
  
## 1.4.0/9
- contacts integration (#10)
- add sum of selected items to subtitle of contextual action bar <-- update readme!!!
- do not overwrite backups but add counter (how to handle restore when there are multiple files?)
- Edit Transaction (item): handle if returned date is set before the date of actual transaction (#24)

## 1.5.0/10
- add scrollbar showing the date/month/year while scrolling the transaction list
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)

## later / unassigned
- use RxJava (?)
- create a way to get from person sum list directly to filtered items list of a person
- make some intro showing basic functions

- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
  - UNCLEAR: how to do all this with dialog fragments???
  - fab -> new txn
  - action mode delete -> alertDialog (something basic like fade+scale or fade+slide)
  - action mode edit -> edit txn/person
  - move transition durations to integer resource



# release-checklist (1.3.0/7)
- [x] update fastlane changelogs (2x)
- [x] update CHANGELOG.md
- [x] update screenshots
- [x] update licenses
  - ./gradlew checkLicenses
  - ./gradlew updateLicenses
  - ./gradlew generateLicensePage
- [x] check build.gradle version+version code
- [x] build release (!) apk, rename debitum-x.x.x.apk
- [x] tag release
