package org.ebur.debitum.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.Locale;


public class PersonWithSum {

    @Embedded
    public Person person;
    public int sum;

    public String getFormattedSum() { return  String.format(Locale.getDefault(),"%.2f", sum/100.0); }

    public int getSign() {
        return Integer.compare(sum, 0); // -1 if sum<0, 0 if sum==0, 1 if sum>0
    }

    public boolean equals(PersonWithSum pws) {
        return pws.person.idPerson == this.person.idPerson;
    }
}
