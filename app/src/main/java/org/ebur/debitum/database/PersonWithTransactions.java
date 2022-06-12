package org.ebur.debitum.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class PersonWithTransactions {

    @Embedded
    public Person person;
    @Relation(parentColumn = "id_person", entityColumn = "id_person")
    public List<Transaction> transactions;

    public PersonWithTransactions(Person person, List<Transaction> transactions) {
        this.person = person;
        this.transactions = transactions;
    }

    public PersonWithTransactions(Person person, Transaction... transaction) {
        this.person = person;
        this.transactions = new ArrayList<>();
        this.transactions.addAll(Arrays.asList(transaction));
    }

    // needed for Comparator in PersonSumListFragment (sorting)
    public String getName() {
        return this.person.name;
    }

    public boolean equals(PersonWithTransactions pwt) {
        boolean person, sum;
        person = pwt.person.equals(this.person);
        sum = Transaction.getSum(pwt.transactions) == Transaction.getSum(this.transactions);
        return person && sum;
    }

    /**
     * @return The sum of all monetary transactions of this PersonWithTransactions' transaction list
     */
    public int getSum() {
        return Transaction.getSum(this.transactions);
    }

    /**
     * @return number of all lent items (sum of |amount| of all non-monetary transactions) in this PersonWithTransactions' transaction list
     */
    public int getNumberOfItems() {
        return Transaction.getNumberOfItems(this.transactions);
    }

    public Date getLastTxnTimestamp() {
        return transactions.stream().max(Comparator.comparing(Transaction::getTimestamp)).get().timestamp;
    }

    /**
     * @param personWithTransactionsList List of PersonWithTransactions
     * @return The sum of all monetary transactions of all PersonWithTransactionss in the given List
     */
    public static int getSum(List<PersonWithTransactions> personWithTransactionsList) {
        int sum = 0;
        for (PersonWithTransactions pwt :personWithTransactionsList) {
            sum+=Transaction.getSum(pwt.transactions);
        }
        return sum;
    }

    /**
     * @param personWithTransactionsList List of PersonWithTransactions
     * @return number of all lent items (sum of |amount| of all non-monetary transactions) of all transactions of all elements of personWithTransactionsList
     */
    public static int getNumberOfItems(List<PersonWithTransactions> personWithTransactionsList) {
        int sum = 0;
        for (PersonWithTransactions pwt :personWithTransactionsList) {
            sum+=Transaction.getNumberOfItems(pwt.transactions);
        }
        return sum;
    }
}
