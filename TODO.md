## 1.3.0/7
- move more logic to ViewModels
  - EditTransactionFragment: switchIsMonetary status to viewModel
- turn returned functionality in EditTransactionFragment (visibility of input, pre-filling input, saving value) on/off using nav argument 
- make EditTransaction's arg "newItem" --> "presetTypeItem" (nav graph, Dialog)
  
## 1.4.0/8
- contacts integration
- add sum of selected items to subtitle of contextual action bar <-- update readme!!!
- do not overwrite backups but add counter (how to handle restore when there are multiple files?)
- add returned button to money list (#12)

## later / unassigned
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- use RxJava (?)
- create a way to get from person sum list directly to filtered items list of a person
- make some intro showing basic functions
- add scrollbar showing the date/month/year while scrolling the transaction list

- improve transitions (https://material.io/blog/android-material-motion, https://developer.android.com/codelabs/material-motion-android#0)
  - UNCLEAR: how to do all this with dialog fragments???
  - fab -> new txn
  - action mode delete -> alertDialog (something basic like fade+scale or fade+slide)
  - action mode edit -> edit txn/person
  - move transition durations to integer resource



# release-checklist (1.2.2/6)
- [x] update fastlane changelogs (2x)
- [x] update CHANGELOG.md
- [-] update screenshots
- [-] update licenses
  - ./gradlew checkLicenses
  - ./gradlew updateLicenses
  - ./gradlew generateLicensePage
- [x] check build.gradle version+version code
- [x] build release (!) apk, rename debitum-x.x.x.apk
- [x] tag release
