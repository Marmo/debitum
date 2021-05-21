package org.ebur.debitum.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "person")
public class Person implements Parcelable {

    public Person(String name, String note) {
        this.name = name;
        this.note = note;
    }
    @Ignore
    public Person(String name) {
        this(name, "");
    }

    @Ignore
    public Person(int idPerson) {
        this("");
        this.idPerson = idPerson;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_person") public int idPerson;
    @ColumnInfo(name = "name") public String name;
    @ColumnInfo(name = "note") public String note;

    public boolean equals(Person p) {
        if(p != null) {
            // treat note as "" when null
            String note = this.note == null ? "":this.note;

            return this.name.equals(p.name)
                    && (this.idPerson == p.idPerson)
                    && (note.equals(p.note));
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
        out.writeString(note);
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
        note = in.readString();
    }

}
