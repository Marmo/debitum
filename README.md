<h1 align="center">Debitum</h1>
<p align="center">
  <a href="https://f-droid.org/de/packages/org.ebur.debitum/">
    <img src="https://img.shields.io/f-droid/v/org.ebur.debitum.svg" />
  </a>
  <a href="https://github.com/marmo/debitum/releases/latest">
    <img src="https://img.shields.io/github/release/marmo/debitum.svg?logo=github" />
  </a>
  <a href="https://hosted.weblate.org/engage/debitum/">
    <img src="https://hosted.weblate.org/widgets/debitum/-/svg-badge.svg" alt="Translation status" />
  </a>
</p>

With Debitum you can track all kinds of IOUs, be it money or lent items. This way you will never 
more forget if your friend already gave you back that book or dispute about how much you owe your 
colleague for coffee.

<p align="center">
 <a href="https://f-droid.org/de/packages/org.ebur.debitum/">
  <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75" />
 </a>
</p>

The intended use case is to manage (smaller) personal debts and lent items. To keep things simple, 
things like interest, deadlines, fees etc. will not be an integral part of the app (though the description 
could somehow be used for that, but I would generally recommend to use more specialised apps for this).

There is no online service whatsoever involved, so your data is saved on your 
device only (as far as this app is concerned).

The UI is inspired by [UOme](https://play.google.com/store/apps/details?id=cz.kns.uome) by Pikadorama.

## Features
- create transactions to record lent money or items
- record transaction details: person, direction (they gave/received), amount, description, date, type (money or item)
- attach images to transactions
- record when an item was returned

- view all transactions summarized by person
- filter transactions by person
- view a list of all money or a list of all item transactions
- easily calculate the sum of transactions by selecting them
- mark items as returned
- filter the items list for returned/unreturned/all items.
- link persons to phone contacts

- backup and restore all app data, settings and attached images

## Permissions
### READ_CONTACTS
This is used for linking phone contacts to persons. You can safely deny this permission and use Debitum without linking contacts.

## Screenshots
<img alt="People list" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/01_en_light_people.png?raw=true" width="200"/> <img alt="Money" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/02_en_light_money.png?raw=true" width="200"/>
<img alt="Money filtered" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/04_en_light_money_filtered.png?raw=true" width="200"/> <img alt="Items" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/03_en_light_items.png?raw=true" width="200"/> <img alt="Items filtered" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/05_en_light_items_filtered.png?raw=true" width="200"/> <img alt="Create Transaction Dialog" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/06_en_light_createTxn.png?raw=true" width="200"/> <img alt="Settings" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/07_en_light_settings.png?raw=true" width="200"/>
<img alt="Dark mode" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/51_en_night_money.png?raw=true" width="200"/>

## Translation
You are welcome to contribute to [translations](TRANSLATION.md).

<a href="https://hosted.weblate.org/engage/debitum/">
<img src="https://hosted.weblate.org/widgets/debitum/-/287x66-grey.png" alt="Translation status" />
</a>

## License
All code in this repository is licensed under the [GNU General Public License v3.0](LICENSE).

Menu icons and parts of the launcher icon (pen, dollar-sign) are Material Icons by Google, licensed 
under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).
