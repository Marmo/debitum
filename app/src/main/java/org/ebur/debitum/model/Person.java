package org.ebur.debitum.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "person")
public class Person {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_person") public int idPerson;
    @ColumnInfo(name = "name") public String name;

}
