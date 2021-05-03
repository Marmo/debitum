# code
## 1.0
- help html in settings
- create new Person from EditTransaction
- move filterBar from Activity to fragments
- fix restart after restore
- remove selectedName from EditPersonViewModel as we can get it from the spinnerName which is an TextView now
## later
- use RxJava
- create a way to get from person sum list directly to filtered items list
- use ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT intent to get source/destination for restore/backup (see https://github.com/lordi/tickmate/blob/master/app/src/main/java/de/smasi/tickmate/Tickmate.java)
- unify transactionList and PersonSumList (abstract base class)
- unify EditPerson and EditTransaction (abstract base class)
- move dialog toolbar to included layout xml
- contacts integration, icon in front of person rows

# visuals
## 1.0
- refactor vector drawables (readability)
- change totals bg or owe green
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
