package org.ebur.debitum;

import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TransactionWithPersonTest {
    private List<TransactionWithPerson> twpList;

    /**
     * Set up the environment for testing
     */
    @Before
    public void setUp() {
        Person tester = new Person("Theo Tester");
        twpList = new ArrayList<>();
        for (int i = 1; i<=10; i++) {
            Transaction t1 = new Transaction(i);
            Transaction t2 = new Transaction(i+11);
            t1.amount = i; t1.isMonetary = true; // sum of all amounts will be 1+2+..+10 = 5*11 = 55
            t2.amount = 2*i*(int)Math.pow(-1,i); t2.isMonetary = false; // number of items will be 2+4+..+20 = 5*22 = 110
            twpList.add(new TransactionWithPerson(t1, tester));
            twpList.add(new TransactionWithPerson(t2, tester));
        }
    }

    @Test
    public void getSumStatic() {
        assertThat(TransactionWithPerson.getSum(twpList), is(55));
    }

    @Test
    public void getNumberOfItemsStatic() {
        assertThat(TransactionWithPerson.getNumberOfItems(twpList), is(110));
    }
}
