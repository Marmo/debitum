package org.ebur.debitum;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilitiesTest {

    /**
     * Set up the environment for testing
     */
    @Before
    public void setUp() {
    }

    @Test
    public void checkNextInt_1() {
        assertThat(Utilities.nextInt(4.6*100), is(460));
    }
    @Test
    public void checkNextInt_2() {
        assertThat(Utilities.nextInt(4.6), is(5));
    }
    @Test
    public void checkNextInt_3() {
        assertThat(Utilities.nextInt(4.4), is(4));
    }
}
