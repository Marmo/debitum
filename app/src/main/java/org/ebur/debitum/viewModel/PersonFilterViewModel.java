package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import org.ebur.debitum.database.Person;

public class PersonFilterViewModel extends AndroidViewModel {

    @Nullable
    private Person filterPerson;

    public PersonFilterViewModel (Application application) {
        super(application);
        filterPerson = null;
    }

    public void setFilterPerson(@Nullable Person person) {
        filterPerson = person;
    }
    @Nullable
    public Person getFilterPerson() {
        return filterPerson;
    }
}
