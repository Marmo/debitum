# code
## 1.0
- show total on top of PersonSumList
- icon in front of person rows
- help html in settings
- fix back stack (e.g. from Settings Activity)
- create new Person from EditTransaction
- make description required if creating item transaction
- move filterBar from Activity to fragments
## later
- use RxJava
- create a way to get from person sum list directly to filtered items list
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- unify transactionList and PersonSumList (abstract base class)
- unify EditPerson and EditTransaction (abstract base class)
- move dialog toolbar to included layout xml
- contacts integration

# visuals
## 1.0
- refactor vector drawables (readability)
- improve icons
- rearrange + restyle list items
- understand and use themes and styles
- app icon
- transitions
  - click on person row --> filterBar
  - navgraph
  - changes to amount edit text when switching switch
  - opening of dialogs (editTxn/person)

# other
## 1.0
- german translation
- Readme
- License (Vector assets!)
- Release 
- FDroid
