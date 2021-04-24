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
        twpList = new ArrayList<>();
        for (int i = 1; i<=10; i++) {
            twpList.add(new TransactionWithPerson(new Transaction(i), new Person("Theo Tester")));
        }
    }

    @Test
    public void getTransactions() {
        List<Transaction> tList = TransactionWithPerson.getTransactions(twpList);
        int idSum = 0;
        for(Transaction t:tList) idSum+=t.idTransaction;
        assertThat(idSum, is(55));
    }
}
