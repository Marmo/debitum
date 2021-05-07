# Debitum

## About
With Debitum you can track all kinds of IOUs, be it money or lent items. This way you will never 
forget if your friend already gave you back that book anymore or dispute about much you owe your 
colleague for coffee.

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
* Money: Shows only monetary transactions, ordered by date (descending)
* Items: Shows only lent items, ordered by date (descending). There is no extra way to mark an item 
as returned, so returned item's transacctions should simply be deleted. 

The only options currently available in Settings are backup and restore of the database (contains 
all transactions and persons)

## Sreenshots
TODO


## License
All code in this repository is licensed under the [GNU General Public License v3.0](LICENSE).

Menu icons and parts of the launcher icon (pen, dollar-sign) are Material Icons by Google, licensed 
under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).
