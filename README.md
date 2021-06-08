<h1 align="center">Debitum</h1>
<p align="center">
  <a href="https://f-droid.org/de/packages/org.ebur.debitum/">
    <img src="https://img.shields.io/f-droid/v/org.ebur.debitum.svg" />
  </a>
  <a href="https://github.com/marmo/debitum/releases/latest">
    <img src="https://img.shields.io/github/release/marmo/debitum.svg?logo=github" />
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

There is no online service whatsoever involved, so your data is saved on your 
device only (as far as this app is concerned).

The UI is inspired by [UOme](https://play.google.com/store/apps/details?id=cz.kns.uome) by Pikadorama.

## Usage
To take note of lent money or items, you need to create a transaction using the red floating action 
button. There you can enter details like amount, description or date and switch between a money- or 
an item-transaction.

Via the Bottom Navigation you can choose different views of your transactions:
* People: summarized by person, ordered by last transaction 
  * you can tap on a row to filter the money- and items list by that person
  * you can create a new Person from the menu of this screen or using the + button while creating a 
  new transaction
* Money: Shows only monetary transactions, ordered by date (descending). If someone repays a debt to you, you should create a new transaction for this. For details why see [here](https://github.com/Marmo/debitum/issues/3#issue-911261188).
* Items: Shows only lent items, ordered by date (descending). There is no extra way to mark an item 
as returned, so returned item's transactions should simply be deleted. 

Upon creating a new transaction (or editing an existent one) you can switch between a monetary and a
item transaction using the switch to the right of the amount input field.

From the Settings Screen you can backup and restore the database (contains all transactions and persons).
Make sure to move backup files out of the way before uninstalling the app or clearing app data, as currently
the backup is saved in the app's data folder on external storage, which is deleted upon 
uninstall/clearing app data.

## Screenshots
<img alt="People list" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/01_en_light_people.png?raw=true" width="200"/> <img alt="Money" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/02_en_light_money.png?raw=true" width="200"/>
<img alt="Money filtered" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/04_en_light_money_filtered.png?raw=true" width="200"/> <img alt="Items" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/03_en_light_items.png?raw=true" width="200"/> <img alt="Items filtered" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/05_en_light_items_filtered.png?raw=true" width="200"/> <img alt="Create Transaction Dialog" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/06_en_light_createTxn.png?raw=true" width="200"/> <img alt="Settings" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/07_en_light_settings.png?raw=true" width="200"/>
<img alt="Dark mode" src="/fastlane/metadata/android/en-US/images/phoneScreenshots/51_en_night_money.png?raw=true" width="200"/>

## Translation
You are welcome to contribute to [translations](TRANSLATION.md).

## License
All code in this repository is licensed under the [GNU General Public License v3.0](LICENSE).

Menu icons and parts of the launcher icon (pen, dollar-sign) are Material Icons by Google, licensed 
under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).
