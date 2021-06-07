# v1.2.0
- add: the generic gray person-icon in the person list is now a colored name-based icon
# v1.1.0
- add: list with third party licenses in settings
- add: persons can have a note that is displayed in the filter bar
- change: use contextual action bar for displaying edit/delete menu items when selecting rows
- change: edit person button in filtered transaction list is now in filter bar instead of top toolbar
- change: amount is initially set to 1 for new item transactions
- change: focus is set to appropriate input fields in edit dialogs
- change: use cradle for floating action button (add transaction button)
- change: brighter icon foreground color (aztec->fjord)
- fix: error messages in edit transaction dialog are cleared now when something is entered
- refactor: moved all common code from the two list fragments (Transaction + PersonSum) to a new abstract superclass AbstractBaseListFragment
- refactor: move dialog toolbar to separate included layout

# v1.0.1
- add version info in settings
- add note on deletion of backup files upon uninstall in settings
- fix snackbars in People-List appearing in the wrong place
- fix date picker in edit transaction screen needing two clicks to appear
- add specific error message when trying to restore from backup and backup file was not found

# v1.0.0
initial release
