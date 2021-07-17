## 1.4.1/12
- notify user about deleted contact and clear uri (current behaviour: hint "linked contact" shown but no name and avatar)

## 1.5.0/13
- add scrollbar showing the date/month/year while scrolling the transaction list
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)

## later / unassigned
- use DataBinding in Edit Dialogs (see branch feature/EditPersonViewModelDataBinding)
- use RxJava (?)
- create a way to get from person sum list directly to filtered items list of a person
- make some intro showing basic functions

- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
  - UNCLEAR: how to do all this with dialog fragments???
  - fab -> new txn
  - action mode delete -> alertDialog (something basic like fade+scale or fade+slide)
  - action mode edit -> edit txn/person
  - move transition durations to integer resource



# release-checklist (1.4.0/11)
- [x] check github milestone
- [x] update fastlane changelogs (2x)
- [x] update CHANGELOG.md
- [x] update screenshots
- [x] update licenses
  - ./gradlew checkLicenses
  - ./gradlew updateLicenses
  - ./gradlew generateLicensePage
- [x] check build.gradle version+version code
- [] build release (!) apk, rename debitum-x.x.x.apk
- [] tag release
