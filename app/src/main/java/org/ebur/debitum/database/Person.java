package org.ebur.debitum.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "person")
public class Person implements Parcelable {

    public Person(String name) {
        this.name = name;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_person") public int idPerson;
    @ColumnInfo(name = "name") public String name;

    public boolean equals(Person p) {
        if(p != null) {
            return this.name.equals(p.name) && (this.idPerson == p.idPerson);
        } else {
            return false;
        }

    }

    // -------------------------
    // Parcelable implementation
    // -------------------------

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(idPerson);
        out.writeString(name);
    }

    public static final Parcelable.Creator<Person> CREATOR
            = new Parcelable.Creator<Person>() {
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    private Person(Parcel in) {
        idPerson = in.readInt();
        name = in.readString();
    }

}
