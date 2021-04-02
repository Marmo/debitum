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

    public int getSum() {
        int sum = 0;
        for(Transaction t : transactions) if (t.isMonetary) sum += t.amount;
        return sum;
    }

    public String getFormattedSum() {
        return  String.format(Locale.getDefault(),"%.2f", this.getSum()/100.0);
    }

    public int getSign() {
        return Integer.compare(this.getSum(), 0); // -1 if sum<0, 0 if sum==0, 1 if sum>0
    }

    public boolean equals(PersonWithTransactions pws) {
        return pws.person.idPerson == this.person.idPerson;
    }

    /*public static List<PersonWithTransactions> summarizeAmounts(List<PersonWithTransactions> rawList) {
        Map<Integer, Integer> sumMap = new HashMap<>(); //Map<personId, sum>
        List<PersonWithTransactions> sumList = Collections.emptyList();

        for (PersonWithTransactions p : rawList) {
            int id = p.person.idPerson;
            if (!sumMap.containsKey(id)) {
                sumMap.put(id, p.getSum());
            }
        }
        for (int id : sumMap.keySet()) {

        }
    }*/
}
