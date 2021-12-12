package org.ebur.debitum.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.ebur.debitum.R;
import org.ebur.debitum.ui.SettingsFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public static int getNrOfDecimals(@NonNull Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(pref.getString(SettingsFragment.PREF_KEY_DECIMALS, "2"));
    }

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
