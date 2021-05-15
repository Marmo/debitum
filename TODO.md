# code
## 1.0.x
- fix date picker
- improve transitions (https://material.io/blog/android-material-motion)
    - move transition durations to integer resource
## 1.1.0
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- unify transactionList and PersonSumList (abstract base class)
- unify EditPerson and EditTransaction (abstract base class)
- move dialog toolbar to included layout xml
- contacts integration
- use contextual action bar (and drop toolbar in main activity completely)
- move more logic to ViewModels

## later / unassigned
- move Total Header to view subclass (to remove duplicate code from list fragments, https://developer.android.com/training/custom-views/create-view)
- use RxJava
- create a way to get from person sum list directly to filtered items list
- unify transactionList and PersonSumList (abstract base class)
- unify EditPerson and EditTransaction (abstract base class)
- move dialog toolbar to included layout xml
- contacts integration
- use contextual action bar
- move more logic to ViewModels
- make nicer quick-guide display

# visuals
# 1.0.0
## 1.1.0
## later

# other
## 1.0.0
- Screenshots
- Release 
- FDroid
- Google Play Store
