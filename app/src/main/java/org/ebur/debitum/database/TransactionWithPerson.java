package org.ebur.debitum.database;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;
import java.util.stream.Collectors;


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
     * Extracts a List of Transaction from a list of TransactionWithPerson
     * @param transactionsWithPerson List of TransactionWithPerson
     * @return A list of all Transactions contained in the original List
     */
    public static List<Transaction> getTransactions(List<TransactionWithPerson> transactionsWithPerson) {
        return transactionsWithPerson
                .stream()
                .map(transactionWithPerson -> transactionWithPerson.transaction)
                .collect(Collectors.toList());
    }
}
