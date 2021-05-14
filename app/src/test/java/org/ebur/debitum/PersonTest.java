package org.ebur.debitum;

import org.ebur.debitum.database.Person;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PersonTest {
    private Person person1, person2, person3;

    /**
     * Set up the environment for testing
     */
    @Before
    public void setUp() {
        person1 = new Person("foo"); person1.idPerson = 0; person1.name = "Samuel Mumm";
        person2 = new Person("foo"); person2.idPerson = 0; person2.name = "Samuel Mumm";
        person3 = new Person("foo"); person3.idPerson = 1; person3.name = "Fred Colon";
    }

    @Test
    public void checkEqual_ReturnsTrue() {
        assertThat(person1.equals(person2), is(true));
    }
    @Test
    public void checkEqual_ReturnsFalse() {
        assertThat(person1.equals(person3), is(false));
    }
}
