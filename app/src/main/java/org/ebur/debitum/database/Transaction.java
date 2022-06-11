package org.ebur.debitum.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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

    public final static int TYPE_MONEY = 0;
    public final static int TYPE_ITEM = 1;

    @Ignore
    public Transaction() {
        this(0, 0, false, "", new Date(0));
    }
    @Ignore
    public Transaction(int id) {
        this();
        this.idTransaction = id;
    }
    public Transaction(int idPerson, int amount, boolean isMonetary, String description, Date timestamp) {
        this.idPerson = idPerson;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.isMonetary = isMonetary;
        this.timestampReturned = null;
        this.hasImages = false;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_transaction") public int idTransaction;
    // Money: lent amount in 1/100 Euro/Dollar/...,
    //        negative means user gave money to person, positive means user received money from person
    // Things: Number of things
    @ColumnInfo(name = "amount") public int amount;
    @ColumnInfo(name= "id_person") public int idPerson;
    @ColumnInfo(name = "description") public String description;
    @ColumnInfo(name = "is_monetary") public boolean isMonetary; // True for money, false for things
    @ColumnInfo(name = "timestamp") public Date timestamp;  // timestamp when the transaction took place
    @ColumnInfo(name = "timestamp_returned") public Date timestampReturned;  // timestamp when the item was returned
    @ColumnInfo(name = "has_images") public boolean hasImages; // if this txn has images (needed for icon in transaction list)

    public Date getTimestamp() {
        return timestamp;
    }

    /*
     * returns the amount formatted as a string, depending on isMonetary
     */
    public String getFormattedAmount(int decimals) { return this.getFormattedAmount(true, decimals); }
    public String getFormattedAmount(boolean signed, int decimals) { return getFormattedAmount(signed, decimals, Locale.getDefault()); }
    public String getFormattedAmount(boolean signed, int decimals, Locale locale) {
        int value = signed ? this.amount : Math.abs(this.amount);
        if (isMonetary) return Transaction.formatMonetaryAmount(value, decimals, locale);
        else            return Integer.toString(value);
    }

    public void setReturned() {
        this.timestampReturned = new Date();
    }

    public boolean isReturned() {
        return timestampReturned != null;
    }

    public boolean equals(Transaction t) {
        return
                this.idTransaction == t.idTransaction &&
                this.description.equals(t.description) &&
                this.amount == t.amount &&
                this.isMonetary == t.isMonetary &&
                this.idPerson == t.idPerson &&
                this.timestamp.equals(t.timestamp) &&
                ((!this.isReturned() && !t.isReturned()) ||
                    (this.isReturned() && this.timestampReturned.equals(t.timestampReturned))
                ) &&
                this.hasImages == t.hasImages;
    }

    //---------------------
    // static tool methods
    // --------------------

    /**
     * @param amount Amount to be formatted with fractions of main currency
     * @return Returns a properly formatted Amount (0-3 decimal places, decimal separator from default locale)
     */
    public static String formatMonetaryAmount(int amount, int decimals) { return formatMonetaryAmount(amount, decimals, Locale.getDefault()); }

    /**
     * @param amount Amount to be formatted in fractions (1/1, 1/10, 1/100 or 1/1000 of main currency depending on setting)
     * @param locale Locale to format the String representation of the amount with
     * @return Returns a properly formatted Amount (0-3 decimal places, decimal separator from locale)
     */
    public static String formatMonetaryAmount(int amount, int decimals, Locale locale) {
        if(decimals < 0 || decimals > 3) {
            throw new IllegalArgumentException("decimals must be an integer between 0 and 3");
        }
        return String.format(
                locale,
                "%,."+decimals+"f",
                amount/Math.pow(10.0, decimals)
        );
    }

    /**
     * @param signed controls if absolute value or true sum is used (for the final result, not the
     *               single transaction amounts!)
     * @param transactions List of [Transaction]s whose sum shall be calculated
     * @return sum of all monetary transactions in transactions in cents (1/100 of main currency)
     */
    public static int getSum(List<Transaction> transactions, boolean signed) {
        int sum = 0;
        for(Transaction t : transactions) if (t.isMonetary) sum += t.amount;

        return signed ? sum : Math.abs(sum);
    }
    public static int getSum(List<Transaction> transactions) {
        return Transaction.getSum(transactions,true);
    }

    /**
     * @param transactions List of [Transaction]s for which the number of lent items shall be calculated
     * @return number of all lent items (sum of |amount| of all non-monetary transactions)
     */
    public static int getNumberOfItems(List<Transaction> transactions) {
        int number = 0;
        for(Transaction t : transactions) if (!t.isMonetary) number += Math.abs(t.amount);
        return number;
    }

    /**
     * @param signed if returned formatted sum shall include a negative-sign
     * @param transactions List of [Transaction]s whose sum shall be calculated and formatted
     * @return sum formatted as a String (e.g. 10.05 for amount=1005)
      */
    // signed controls if absolute value or true sum is used
    public static String getFormattedSum(List<Transaction> transactions, boolean signed, int decimals) {
        return Transaction.formatMonetaryAmount(Transaction.getSum(transactions, signed), decimals);
    }


    /**
     *
     * @param transactions List of [Transaction]s for which the sign of the sum shall be calculated
     * @return sign of the sum of all monetary [Transaction]s
     */
    public static int getSumSign(List<Transaction> transactions) {
        return Integer.compare(Transaction.getSum(transactions), 0); // -1 if sum<0, 0 if sum==0, 1 if sum>0
    }
}
