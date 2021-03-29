package org.ebur.debitum;

import org.ebur.debitum.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilities {
    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }
}
