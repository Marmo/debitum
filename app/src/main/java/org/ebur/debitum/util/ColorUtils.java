package org.ebur.debitum.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.ebur.debitum.R;
import org.ebur.debitum.ui.SettingsFragment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class ColorUtils {
    public static final String TAG = "ColorUtils";


    /**
     *
     * @param degrees how many degrees the hue of baseColor shall be shifted
     * @param baseColor color of which the hue shall be changed
     * @return a color int with saturation and value of base color and base color's hue+degrees
     */
    @ColorInt
    public static int changeHue(float degrees, @ColorInt int baseColor) {
        float[] baseColorHSV = new float[3];
        android.graphics.Color.colorToHSV(baseColor, baseColorHSV);
        return android.graphics.Color.HSVToColor(new float[] {(baseColorHSV[0]+degrees)%360,
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
            Log.w(Utilities.TAG, "did not find color resource " + colorRes);
        }
        return color;
    }

    public static String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes(StandardCharsets.UTF_8));
            byte[] hashBytes = md.digest();
            //Convert hash bytes to hex format
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ignore) {
            return null;
        }
    }

    /**
     * @param context: Context to retrieve the setting+color from
     * @return the color, that should be used to color owed amounts (i.e. txn amount > 0)
     */
    @ColorInt
    public static int getOweColor(@NonNull Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean invertColors = pref.getBoolean(SettingsFragment.PREF_KEY_INVERT_COLORS, false);
        int colorRes = invertColors ? R.color.lent_red : R.color.owe_green;
        return context.getResources().getColor(colorRes, null);
    }

    /**
     * @param context: Context to retrieve the setting+color from
     * @return the color, that should be used to color lent amounts (i.e. txn amount < 0)
     */
    @ColorInt
    public static int getLentColor(@NonNull Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean invertColors = pref.getBoolean(SettingsFragment.PREF_KEY_INVERT_COLORS, false);
        int colorRes = invertColors ? R.color.owe_green : R.color.lent_red;
        return context.getResources().getColor(colorRes, null);
    }
}
