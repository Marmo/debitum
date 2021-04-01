package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.ebur.debitum.database.PersonWithSum;
import org.ebur.debitum.database.TransactionRepository;

import java.util.List;

public class PersonSumListViewModel extends AndroidViewModel {

    private final TransactionRepository repository;

    private final LiveData<List<PersonWithSum>> personSums;

    public PersonSumListViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
        personSums = repository.getAllPersonSums();
    }

    public LiveData<List<PersonWithSum>> getPersonSums() { return personSums; }
}