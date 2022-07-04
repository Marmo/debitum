Translations
------------
<a href="https://hosted.weblate.org/engage/debitum/">
<img src="https://hosted.weblate.org/widgets/debitum/-/multi-auto.svg" alt="Translation status" />
</a>

Contribute
----------

Translations are managed with [Weblate](https://hosted.weblate.org/engage/debitum/). Components that can be translated are the UI Strings and F-Droid's App-Descriptions.

## A note on the UI-logic of the edit-transaction-dialog

The understandability of the edit transaction dialog's logic relies on a sentence like 'Name received amount' (e.g. 'Paul received 3.00 €') working in your 
language. If this order of subject->verb->object is impossible in your language (so it's hard to tell if the selected person or the user received the money/item),
an option is to provide a locale-specific layout (for example moving the gave/received radio buttons below the amount input for Basque). 

If you made a translation for a language that has a different order than English, notify me about this (e.g. create an issue) so I can create a matching locale-specific layout.

See also [this Issue](https://github.com/Marmo/debitum/issues/5).
