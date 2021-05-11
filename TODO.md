# code
## 1.0.0
- use scrolling-behaviour-action-bar for header (solves problems with divider in person sum list being inset also below header and looks nicer)
## later
- use RxJava
- create a way to get from person sum list directly to filtered items list
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- unify transactionList and PersonSumList (abstract base class)
- unify EditPerson and EditTransaction (abstract base class)
- move dialog toolbar to included layout xml
- contacts integration
- use contextual action bar
- move more logic to ViewModels
- make nicer quick-guide display

# visuals
## 1.0.0
- transitions
  - click on person row --> filterBar
  - changes to amount edit text when switching switch
  - fab to editTransaction
  - menu buttons to editTransaction and editPerson
  - shared element add person from editTransaction
- remove unused fonts
## later
- decide if items are shown in PersonSumList header or remove item transaction from header transaction

# other
## 1.0.0
- Release 
- FDroid
