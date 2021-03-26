package org.ebur.debitum.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;


public class PersonWithTransactions {

    @Embedded
    public Person person;
    @Relation(
            parentColumn = "id_person",
            entityColumn = "id_person"
    )
    public List<Transaction> transactions;
}
