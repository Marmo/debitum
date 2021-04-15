package org.ebur.debitum.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;


public class PersonWithTransactions {

    @Embedded
    public Person person;
    @Relation(parentColumn = "id_person", entityColumn = "id_person")
    public List<Transaction> transactions;

    public PersonWithTransactions(Person person, List<Transaction> transactions) {
        this.person = person;
        this.transactions = transactions;
    }

    public boolean equals(PersonWithTransactions pws) {
        boolean person, sum;
        person = pws.person.equals(this.person);
        sum = Transaction.getSum(pws.transactions) == Transaction.getSum(this.transactions);
        return person && sum;
    }
}
