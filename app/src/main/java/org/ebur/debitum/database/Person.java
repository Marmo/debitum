package org.ebur.debitum.database;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.ebur.debitum.util.ColorUtils;

@Entity(tableName = "person")
public class Person implements Parcelable {

    public static final int NR_OF_COLORS = 12;

    public Person(String name, String note, @Nullable Uri linkedContactUri) {
        this.name = name;
        this.note = note;
        this.linkedContactUri = linkedContactUri;
        this.colorIndex = getColorIndex();
    }
    @Ignore
    public Person(String name) {
        this(name, "", null);
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
    @Nullable
    @ColumnInfo(name = "linked_contact_uri") public Uri linkedContactUri;
    @Ignore public int colorIndex = -1;

    public boolean equals(Person p) {
        if(p != null) {
            boolean equal = this.idPerson == p.idPerson;
            equal = equal && this.name.equals(p.name);
            equal = equal && (
                    (this.linkedContactUri == null && p.linkedContactUri == null)
                            || (this.linkedContactUri != null && this.linkedContactUri.equals(p.linkedContactUri))
                    );
            equal = equal && (
                    (this.note == null && p.note == null)
                            || (this.note!= null && this.note.equals(p.note))
            );
            return equal;
        } else {
            return false;
        }

    }

    // as Room generates new objects when a row is updated, we do not need to manually
    // refresh the color when the name is edited
    public int getColorIndex() {
        if (colorIndex == -1) {
            calcuateColorIndex();
        }
        return colorIndex;
    }

    public void calcuateColorIndex() {
        String md5String = ColorUtils.md5Hash(name);
        assert md5String != null;
        colorIndex = Math.abs(md5String.hashCode()%NR_OF_COLORS);
    }

    /**
     * @param baseColor color whose HSV values are used (hue as starting point, saturation & value as-is)
     * @return a color calculated using the person's color index. The color will have the same
     * saturation and value as the passed baseColor but the hue set to baseColor's
     * hue + colorIndex*360/NR_OF_COLORS
     **/
    @ColorInt
    public int getColor(@ColorInt int baseColor) {
        return ColorUtils.changeHue(colorIndex*360f/NR_OF_COLORS, baseColor);
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
        out.writeString(linkedContactUri == null ? null : linkedContactUri.toString());
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
        String uriString = in.readString();
        linkedContactUri = uriString == null ? null : Uri.parse(uriString);
    }

}
