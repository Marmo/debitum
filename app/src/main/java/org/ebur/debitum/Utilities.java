package org.ebur.debitum;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utilities {
    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }
    public static double parseAmount(String localizedAmountString) throws ParseException {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        return nf.parse(localizedAmountString).doubleValue();
    }
}
