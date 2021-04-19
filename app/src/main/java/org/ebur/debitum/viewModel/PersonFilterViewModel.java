package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.ebur.debitum.database.Person;

public class PersonFilterViewModel extends AndroidViewModel {

    @Nullable
    private Person filterPerson;
    private MutableLiveData<Person> filterPersonLive;

    public PersonFilterViewModel (Application application) {
        super(application);
        filterPerson = null;
        filterPersonLive = new MutableLiveData<>(null);
    }

    public void setFilterPerson(@Nullable Person person) {
        filterPerson = person;
        filterPersonLive.setValue(person);
    }
    @Nullable
    public Person getFilterPerson() {
        return filterPerson;
    }

    public LiveData<Person> getFilterPersonLive() {
        return filterPersonLive;
    }
}
