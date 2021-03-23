package org.ebur.debitum.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

// TODO create TransactionRaw with the contents of this Entity (without name but id_person) and remake Transaction as Relationship including Person's name

@Entity(tableName = "txn")
public class Transaction {
    public Transaction(String name, int amount, String description, Date timestamp) {
        this.name = name;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.isMonetary = true;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_transaction") public int idTransaction;
    @ColumnInfo(name = "amount") public int amount;
    @ColumnInfo(name= "name") public String name;
    @ColumnInfo(name = "description") public String description;
    @ColumnInfo(name = "is_monetary") public boolean isMonetary;
    @ColumnInfo(name = "timestamp") public Date timestamp;

    public String getAmount() {
        // TODO differentiate by isMonetary
        // TODO use locale's decimal separator
        String str =  String.valueOf(amount);
        return new StringBuilder(str).insert(str.length()-2, ".").toString();
    }

    public boolean equals(Transaction t) { return this.idTransaction == t.idTransaction; }

    // TODO create getter and setter methods and make members private
}
