package org.ebur.debitum.database;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;


public class TransactionWithPerson {

    @Embedded
    public Transaction transaction;
    @Relation(
            parentColumn = "id_person",
            entityColumn = "id_person"
    )
    public Person person;

    public TransactionWithPerson(Transaction transaction, Person person) {
        this.transaction = transaction;
        this.person = person;
    }

    public boolean equals(@NonNull TransactionWithPerson t) {
        return this.transaction.equals(t.transaction) && this.person.equals(t.person);
    }

    /**
     * @param transactionWithPersonList List of TransactionWithPerson
     * @return The sum of all monetary transactions of all PersonWithTransactionss in the given List
     */
    public static int getSum(List<TransactionWithPerson> transactionWithPersonList) {
        return transactionWithPersonList
                .stream()
                .filter(twp -> twp.transaction.isMonetary)
                .mapToInt(twp -> twp.transaction.amount)
                .sum();
    }

    /**
     * @param transactionWithPersonList List of TransactionWithPerson
     * @return number of all lent items (sum of |amount| of all non-monetary transactions) of all transactions of all elements of personsWithTransactions
     */
    public static int getNumberOfItems(List<TransactionWithPerson> transactionWithPersonList) {
        return transactionWithPersonList == null ? 0 :transactionWithPersonList
                .stream()
                .filter(twp -> !twp.transaction.isMonetary)
                .mapToInt(twp -> Math.abs(twp.transaction.amount))
                .sum();
    }
}
