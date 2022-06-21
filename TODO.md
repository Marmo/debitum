## later / unassigned
- move preference keys to string resources
- add scrollbar showing the date/month/year while scrolling the transaction list OR add bar that shows the date and total debt up to the topmost visible transaction
- swipe entry to mark as returned or delete
- use DataBinding in Edit Dialogs (see branch feature/EditPersonViewModelDataBinding)
- create a way to get from person sum list directly to filtered items list of a person
- make some intro showing basic functions

- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
  - UNCLEAR: how to do all this with dialog fragments???
  - fab -> new txn
  - action mode delete -> alertDialog (something basic like fade+scale or fade+slide)
  - action mode edit -> edit txn/person
  - move transition durations to integer resource



# release-checklist (1.7.0/21)
- [x] check github milestone
- [] update fastlane changelog
- [] update CHANGELOG.md
- [] update screenshots
- [] update licenses
  - ./gradlew checkLicenses
  - ./gradlew updateLicenses
  - ./gradlew generateLicensePage
- [] check build.gradle version+version code
- [] pull latest translations
- [] build release (!) apk, rename debitum-x.x.x.apk
- [] tag release
