# code
## 1.0.1
- restore db: add specific error message when no backup file was found

## 1.1.0
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- unify transactionList and PersonSumList (abstract base class)
- unify EditPerson and EditTransaction (abstract base class)
- move dialog toolbar to included layout xml
- contacts integration
- use contextual action bar (and drop toolbar in main activity completely)
- move more logic to ViewModels
- add licenses view
- add notes to person (editPersonFragment, person entity, filter bar with note as subtitle)
- improve transitions (https://material.io/blog/android-material-motion)
    - move transition durations to integer resource

## later / unassigned
- move Total Header to view subclass (to remove duplicate code from list fragments, https://developer.android.com/training/custom-views/create-view)
- use RxJava
- create a way to get from person sum list directly to filtered items list of a person
- unify transactionList and PersonSumList (abstract base class)
- unify EditPerson and EditTransaction (abstract base class)
- move dialog toolbar to included layout xml
- contacts integration
- use contextual action bar
- move more logic to ViewModels
- make nicer quick-guide display

# visuals
## 1.1.0
## later

# other
