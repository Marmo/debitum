package org.ebur.debitum;

import org.ebur.debitum.util.Utilities;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilitiesTest {

    private String s1, s2, s3, s4, s5;

    /**
     * Set up the environment for testing
     */
    @Before
    public void setUp() {
        s1 = "abc";
        s2 = "abc";
        s3 = "cba";
        s4 = null;
        s5 = null;
    }

    @Test
    public void checkEqual_ReturnsTrue1() {
        assertThat(Utilities.equal(s1,s2), is(true));
    }@Test
    public void checkEqual_ReturnsTrue2() {
        assertThat(Utilities.equal(s2,s1), is(true));
    }
    @Test
    public void checkEqual_ReturnsTrue3() {
        assertThat(Utilities.equal(s4,s5), is(true));
    }
    @Test
    public void checkEqual_ReturnsTrue4() {
        assertThat(Utilities.equal(s5,s4), is(true));
    }
    @Test
    public void checkEqual_ReturnsFalse1() {
        assertThat(Utilities.equal(s1,s3), is(false));
    }
    @Test
    public void checkEqual_ReturnsFalse2() {
        assertThat(Utilities.equal(s3,s1), is(false));
    }
    @Test
    public void checkEqual_ReturnsFalse3() {
        assertThat(Utilities.equal(s1,s4), is(false));
    }
    @Test
    public void checkEqual_ReturnsFalse4() {
        assertThat(Utilities.equal(s4,s1), is(false));
    }
}

