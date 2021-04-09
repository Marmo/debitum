package org.ebur.debitum.database;

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

    public boolean equals(TransactionWithPerson t) {
        return this.transaction.equals(t.transaction) && this.person.equals(t.person);
    }
}
