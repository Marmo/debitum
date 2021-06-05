package org.ebur.debitum.database;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.content.res.ResourcesCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "person")
public class Person implements Parcelable {

    public static final int[] COLORS = new int[] {0xffd44e2d, 0xff22c9dc, 0xffdc3522, 0xffdc9222, 0xffc9dc22,
            0xff22dc92, 0xff226cdc, 0xff22dc35, 0xff3522dc, 0xff6cdc22, 0xff9222dc};
    public static final int NR_OF_COLORS = 11;

    public Person(String name, String note) {
        this.name = name;
        this.note = note;
        this.colorIndex = getColorIndex();
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
    @Ignore public int colorIndex = -1;

    public boolean equals(Person p) {
        if(p != null) {
            boolean equalId = this.idPerson == p.idPerson;
            boolean equalNameAndId = equalId && this.name.equals(p.name);
            return equalNameAndId
                    && ((this.note == null && p.note == null)
                        || (this.note!= null && this.note.equals(p.note))
                       );
        } else {
            return false;
        }

    }

    public int getColorIndex() {
        if (colorIndex == -1) {
            calcuateColorIndex();
        }
        return colorIndex;
    }

    public void calcuateColorIndex() {
        colorIndex = Math.abs(name.hashCode()%NR_OF_COLORS);
    }

    public int getColor() {
        // TODO get primary color from attribute
        // TODO get primary color's saturation and value
        return Color.HSVToColor(new float[] {255*colorIndex/12f, .788f, .831f});
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
