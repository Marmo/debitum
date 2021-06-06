package org.ebur.debitum.database;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.core.content.res.ResourcesCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.ebur.debitum.Utilities;

@Entity(tableName = "person")
public class Person implements Parcelable {

    public static final int NR_OF_COLORS = 12;

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

    /**
     * calculates a color based on the person's color index.
     * The color will have the same saturation and value as the
     * current secondary color but the hue set to secondary color's
     * hue + colorIndex*360/12
     **/
    @ColorInt
    public int getColor(@ColorInt int baseColor) {
        float[] baseColorHSV = new float[3];
        Color.colorToHSV(baseColor, baseColorHSV);
        return Color.HSVToColor(new float[] {(baseColorHSV[0]+360f*colorIndex/NR_OF_COLORS)%360,
                                             baseColorHSV[1],
                                             baseColorHSV[2]});
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
