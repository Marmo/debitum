package org.ebur.debitum.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@Entity(tableName = "txn",
        foreignKeys = {
                @ForeignKey(entity = Person.class,
                            parentColumns = "id_person",
                            childColumns= "id_person")
        },
        indices = { @Index("id_person") })
public class Transaction {

    public Transaction(int idPerson, int amount, boolean isMonetary, String description, Date timestamp) {
        this.idPerson = idPerson;
        this.amount = amount; //negative amounts denote money given to the user, positive means given to person
        this.description = description;
        this.timestamp = timestamp;
        this.isMonetary = isMonetary;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_transaction") public int idTransaction;
    // Money: lent amount in 1/100 Euro/Dollar/...,
    //        positive means user gave money to person, negative means user receivef money from person
    // Things: Number of things
    @ColumnInfo(name = "amount") public int amount;
    @ColumnInfo(name= "id_person") public int idPerson;
    @ColumnInfo(name = "description") public String description;
    @ColumnInfo(name = "is_monetary") public boolean isMonetary; // True for money, false for things
    @ColumnInfo(name = "timestamp") public Date timestamp;  // integer counting milliseconds from epoch

    /*
     * returns the amount formatted as a string, depending on isMonetary
     */
    public String getFormattedAmount(boolean signed) {
        int value = signed ? this.amount : Math.abs(this.amount);
        if (isMonetary) return Transaction.formatAmount(value);
        else            return Integer.toString(value);
    }
    public String getFormattedAmount() {
        return this.getFormattedAmount(true);
    }

    public boolean equals(Transaction t) { return this.idTransaction == t.idTransaction; }

    // TODO create getter and setter methods and make members private

    /****************************
     * static tool methods
     ****************************/

    public static String formatAmount(int amount) {
        return String.format(Locale.getDefault(),"%.2f", amount/100.0);
    }
    /*
     * return sum of all monetary transactions
     * signed controls if absolute value or true sum is used (for the final result, not the
     * single transaction amounts!)
     */
    public static int getSum(List<Transaction> transactions, boolean signed) {
        int sum = 0;
        for(Transaction t : transactions) if (t.isMonetary) sum += t.amount;

        return signed ? sum : Math.abs(sum);
    }
    public static int getSum(List<Transaction> transactions) { return Transaction.getSum(transactions,true); }

    // return sum formatted as a String (e.g. 10.05 for amount=1005)
    // signed controls if absolute value or true sum is used
    public static String getFormattedSum(List<Transaction> transactions, boolean signed) {
        return Transaction.formatAmount(Transaction.getSum(transactions, signed));
    }
    public static String getFormattedSum(List<Transaction> transactions) { return Transaction.getFormattedSum(transactions, true); }


    // returns if sum is negative
    public static int getSumSign(List<Transaction> transactions) {
        return Integer.compare(Transaction.getSum(transactions), 0); // -1 if sum<0, 0 if sum==0, 1 if sum>0
    }
}
