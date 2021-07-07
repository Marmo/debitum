package org.ebur.debitum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

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
    public static String formatDate(@Nullable Date date) {
        return formatDate(date, DATE_FORMAT);
    }
    @Nullable
    public static String formatDate(@Nullable Date date, String format) {
        if (date == null) {
            return null;
        } else {
            DateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
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
