package org.ebur.debitum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.ebur.debitum.ui.SettingsFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static int nextInt(double d) {
        return new BigDecimal(Double.toString(d))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
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

    // TODO cleanup duplicate code in copy methods
    public static void copyFile(File source, File dest) throws IOException {
        // try-with-resources
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    public static void copyFile(Uri source, File dest, ContentResolver resolver) throws IOException {
        // try-with-resources
        try (ParcelFileDescriptor pfdSource = resolver.openFileDescriptor(source, "r");
             FileChannel sourceChannel = new FileInputStream(pfdSource.getFileDescriptor()).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    // inserts infix before the last "." character of filename
    public static String insertInfixBeforeExt(@NonNull String filename, @NonNull String infix) {
        return filename.substring(0, filename.lastIndexOf("."))
                + infix + filename.substring(filename.lastIndexOf("."));
    }

    /**
     *
     * @param dir directory in which to look for images
     * @return for all files in dir whose name represents a 8 digit hex value, the maximum is
     * determined and max + 1 as 8 digit hex value returned
     */
    @NonNull
    public static String getNextImageFilename(@NonNull File dir) {
        if (dir.exists()) {
            // create sorted list of all files whose names represent 8-digit hex numbers
            // and find its maximum
            String[] dirlist = dir.list();
            if (dirlist == null) {
                return "00000001";
            } else {
                String maxFilename =  new ArrayList<String>(Arrays.asList(dirlist))
                        .stream()
                        .filter(s -> s.matches("^[0123456789abcdef]{8}\\.[^.]+$")) // only consider 8-digit-hex representations
                        .map(s -> s.replaceAll("\\.[^.]+$", "")) // remove extension
                        .max(String::compareTo)
                        .orElse("00000000");
                Long num = Long.parseLong(maxFilename, 16) + 1;
                return String.format(Locale.getDefault(), "%04x", num);
            }
        } else {
            dir.mkdirs();
            return "00000000";
        }
    }
}
