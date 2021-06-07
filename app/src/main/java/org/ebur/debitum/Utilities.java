package org.ebur.debitum;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utilities {
    public static final String TAG = "Utilities";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static String formatDate(Date date) {
        return formatDate(date, DATE_FORMAT);
    }
    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public static double parseAmount(String localizedAmountString) throws ParseException {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        return nf.parse(localizedAmountString).doubleValue();
    }

    /**
     *
     * @param degrees how many degrees the hue of baseColor shall be shifted
     * @param baseColor color of which the hue shall be changed
     * @return a color int with saturation and value of base color and base color's hue+degrees
     */
    @ColorInt
    public static int changeHue(float degrees, @ColorInt int baseColor) {
        float[] baseColorHSV = new float[3];
        Color.colorToHSV(baseColor, baseColorHSV);
        return Color.HSVToColor(new float[] {(baseColorHSV[0]+degrees)%360,
                baseColorHSV[1],
                baseColorHSV[2]});
    }

    /**
     *
     * @param context context from which the resource shall be retrieved
     * @param attributeId the attribute that points to the desired color resource (e.g. R.attr.colorSecondary)
     * @return color int that is the result of resolving the given attributes
     */
    @ColorInt
    public static int getAttributeColor(
            Context context,
            @AttrRes int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes, null);
        } catch (Resources.NotFoundException e) {
            Log.w(TAG, "did not find color resource " + colorRes);
        }
        return color;
    }
}
