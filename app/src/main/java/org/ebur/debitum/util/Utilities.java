package org.ebur.debitum.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.ebur.debitum.R;
import org.ebur.debitum.ui.SettingsFragment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class Utilities {
    public static final String TAG = "Utilities";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    @Nullable
    public static String formatDate(@Nullable Date date, @NonNull Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String dateFormat = pref.getString(SettingsFragment.PREF_KEY_DATE_FORMAT, DATE_FORMAT);
        String dateFormatSystemDefaultS = context.getResources()
                .getString(R.string.pref_date_format_systemdefault_short_value);
        String dateFormatSystemDefaultM = context.getResources()
                .getString(R.string.pref_date_format_systemdefault_medium_value);
        String dateFormatSystemDefaultL = context.getResources()
                .getString(R.string.pref_date_format_systemdefault_long_value);

        if (dateFormat.equals(dateFormatSystemDefaultS)) {
            // format with system default short value
            return formatDate(date, java.text.DateFormat.getDateInstance(DateFormat.SHORT));
        } else if (dateFormat.equals(dateFormatSystemDefaultM)) {
            return formatDate(date, java.text.DateFormat.getDateInstance(DateFormat.MEDIUM));
        } else if (dateFormat.equals(dateFormatSystemDefaultL)) {
            return formatDate(date, java.text.DateFormat.getDateInstance(DateFormat.LONG));
        } else {
            // format with value from preference
            return formatDate(date, dateFormat);
        }
    }
    @Nullable
    public static String formatDate(@Nullable Date date, String format) {
        return formatDate(date, new SimpleDateFormat(format, Locale.getDefault()));
    }
    @Nullable
    public static String formatDate(@Nullable Date date, @NonNull DateFormat dateFormat) {
        if (date == null) {
            return null;
        } else {
            // we must not apply any timezone-hour-adding or subtraction here, see #28
            // thus we have to use a timezone with UTC offset 0:00
            dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            return dateFormat.format(date);
        }
    }

    public static double parseAmount(String localizedAmountString) throws ParseException {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        return nf.parse(localizedAmountString).doubleValue();
    }

    public static boolean equal(@Nullable Object one, @Nullable Object other) {
        return (one == null && other == null) || (one != null && one.equals(other));
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

    public static String md5Hash(@NonNull String input) {
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

    public static void setVisibilityAnimated(View view, int visibility) {
        float alpha, translationY;
        int duration = 100;
        AnimatorListenerAdapter listener;
        switch (visibility) {
            case View.VISIBLE:
                alpha = 1f;
                translationY = 10f;
                listener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        view.setVisibility(visibility);
                    }
                };
                break;
            case View.INVISIBLE:
            case View.GONE:
                alpha = 0f;
                translationY = -10f;
                listener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(visibility);
                    }
                };
                break;
            default:
                throw new IllegalArgumentException("Unsupported value for View's visibility: "+visibility);
        }

        view.animate()
                .alpha(alpha)
                .translationY(translationY)
                .setDuration(duration)
                .setListener(listener);
    }
}
