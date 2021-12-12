package org.ebur.debitum;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.ebur.debitum.database.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

public class TransactionTest {
    private Transaction tMon1, tMon2, tMon3, tMon4, tThing1;
    Locale locale;

    /**
     * Set up the environment for testing
     */
    @Before
    public void setUp() {
        tMon1 = new Transaction(0, 1234, true, "nothing came to my mind", new Date(1234567890)); tMon1.idTransaction = 0;
        tMon2 = new Transaction(0, -1234, true, "nothing came to my mind", new Date(1234567890)); tMon2.idTransaction = 0;
        tMon3 = new Transaction(0, 1, true, "nothing came to my mind", new Date(1234567890)); tMon3.idTransaction = 0;
        tMon4 = new Transaction(0, 1234, true, "nothing came to my mind", new Date(1234567890)); tMon3.idTransaction = 0;
        tThing1 = new Transaction(0, -1234, false, "nothing came to my mind", new Date(1234567890)); tThing1.idTransaction = 1;
        locale = Locale.ENGLISH;
    }

    @Test
    public void getFormattedAmount_positive_signed_Monetary() {
        String formattedAmount = tMon1.getFormattedAmount(true, 2, locale);
        assertThat(formattedAmount, is("12.34"));
    }

    @Test
    public void getFormattedAmount_positive_unsigned_Monetary() {
        String formattedAmount = tMon1.getFormattedAmount(false, 2, locale);
        assertThat(formattedAmount, is("12.34"));
    }

    @Test
    public void getFormattedAmount_negative_signed_Monetary() {
        String formattedAmount = tMon2.getFormattedAmount(true, 2, locale);
        assertThat(formattedAmount, is("-12.34"));
    }

    @Test
    public void getFormattedAmount_negative_unsigned_Monetary() {
        String formattedAmount = tMon2.getFormattedAmount(false, 2, locale);
        assertThat(formattedAmount, is("12.34"));
    }

    @Test
    public void getFormattedAmount_negative_signed_Thing() {
        String formattedAmount = tThing1.getFormattedAmount(true, 2, locale);
        assertThat(formattedAmount, is("-1234"));
    }

    @Test
    public void getFormattedAmount_negative_unsigned_Thing() {
        String formattedAmount = tThing1.getFormattedAmount(false, 2, locale);
        assertThat(formattedAmount, is("1234"));
    }

    @Test
    public void getFormattedAmount_positive_signed_MonetarySmall() {
        String formattedAmount = tMon3.getFormattedAmount(true, 2, locale);
        assertThat(formattedAmount, is("0.01"));
    }

    @Test
    public void getFormattedAmount_positive_unsigned_MonetarySmall() {
        String formattedAmount = tMon3.getFormattedAmount(false, 2, locale);
        assertThat(formattedAmount, is("0.01"));
    }

    // nr of decimals tests
    @Test
    public void getFormattedAmount_positive_signed_Monetary_0dec() {
        String formattedAmount = tMon1.getFormattedAmount(true, 0, locale);
        assertThat(formattedAmount, is("1234"));
    }
    @Test
    public void getFormattedAmount_positive_signed_Monetary_1dec() {
        String formattedAmount = tMon1.getFormattedAmount(true, 1, locale);
        assertThat(formattedAmount, is("123.4"));
    }
    @Test
    public void getFormattedAmount_positive_signed_Monetary_3dec() {
        String formattedAmount = tMon1.getFormattedAmount(true, 3, locale);
        assertThat(formattedAmount, is("1.234"));
    }

    @Test
    public void equals_equal() {
        assertThat(tMon1.equals(tMon4), is(true));
    }

    @Test
    public void equals_nonequal() {
        assertThat(tMon1.equals(tMon2), is(false));
    }
}
