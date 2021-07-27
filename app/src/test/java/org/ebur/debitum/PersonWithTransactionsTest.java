package org.ebur.debitum;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.PersonWithTransactions;
import org.ebur.debitum.database.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PersonWithTransactionsTest {
    private List<PersonWithTransactions> pwtList;

    /**
     * Set up the environment for testing
     */
    @Before
    public void setUp() {
        Person tester = new Person("Theo Tester");
        pwtList = new ArrayList<>();
        for (int i = 1; i<=10; i++) {
            Transaction t1 = new Transaction(i);
            Transaction t2 = new Transaction(i+11);
            t1.amount = i; t1.isMonetary = true; // sum of all amounts will be 1+2+..+10 = 5*11 = 55
            t2.amount =  2*i*(int)Math.pow(-1,i); t2.isMonetary = false; // number of items will be 2+4+..+20 = 5*22 = 110
            pwtList.add(new PersonWithTransactions(tester, t1, t2));
        }
    }

    @Test
    public void getSumStatic() {
        assertThat(PersonWithTransactions.getSum(pwtList), is(55));
    }

    @Test
    public void getNumberOfItemsStatic() {
        assertThat(PersonWithTransactions.getNumberOfItems(pwtList), is(110));
    }

    @Test
    public void getSum() {
        assertThat(pwtList.get(0).getSum(), is(1));
    }

    @Test
    public void getNumberOfItems() {
        assertThat(pwtList.get(0).getNumberOfItems(), is(2));
    }
}
