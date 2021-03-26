package org.ebur.debitum.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Locale;

// TODO create TransactionRaw with the contents of this Entity (without name but id_person) and remake Transaction as Relationship including Person's name

@Entity(tableName = "txn")
public class Transaction {
    public Transaction(int idPerson, int amount, boolean isMonetary, String description, Date timestamp) {
        this.idPerson = idPerson;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.isMonetary = isMonetary;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_transaction") public int idTransaction;
    @ColumnInfo(name = "amount") public int amount;
    @ColumnInfo(name= "id_person") public int idPerson;
    @ColumnInfo(name = "description") public String description;
    @ColumnInfo(name = "is_monetary") public boolean isMonetary;
    @ColumnInfo(name = "timestamp") public Date timestamp;

    public String getAmount() {
        if (isMonetary) return Integer.toString(amount);
        else            return  String.format(Locale.getDefault(),"%.2f", amount/100.0);
    }

    public boolean equals(Transaction t) { return this.idTransaction == t.idTransaction; }

    // TODO create getter and setter methods and make members private
}
