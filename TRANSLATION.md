Translations
------------

  * German ([@marmo](https://github.com/marmo))
  * Portuguese (Brasil) ([@mezysinc](https://github.com/mezysinc))

Contribute
----------

If you want to contribute a translation, just fork this repository and copy & translate the following files (replace `xx` with your language's [ISO 639-1](http://en.wikipedia.org/wiki/ISO_639-1) code):

File | Location | Note
-----|----------|-----
[strings.xml](app/src/main/res/values/strings.xml) | `app/src/main/res/values-xx/` | Strings used in Debitum's UI
[guide_xx.html](app/src/main/assets/guide_en.html) | `app/src/main/assets/` | Quick Guide accessible from Settings
[full_description.txt](fastlane/metadata/android/en-US/full_description.txt) | `fastlane/metadata/android/xx/` | Full app-description used by F-Droid
[short_description.txt](fastlane/metadata/android/en-US/short_description.txt) | `fastlane/metadata/android/xx/` | Short app-description used by F-Droid, **max 80 chars**

When you are done send me a pull request :-)

## A note on the UI-logic of the edit-transaction-dialog

The understandability of the edit transaction dialog's logic relies on a sentence like 'Name received amount' (e.g. 'Paul received 3.00 €') working in your 
language. If this order of subject->verb->object is impossible in your language (so it's hard to tell if the selected person or the user received the money/item),
an option is to provide a locale-specific layout (for example moving the gave/received radio buttons below the amount input for Basque). 

If you made a translation for a language that has a different order than English, please point that out in your pull reuest so I can create a matching locale-specific layout!

See also [this Issue](https://github.com/Marmo/debitum/issues/5).
