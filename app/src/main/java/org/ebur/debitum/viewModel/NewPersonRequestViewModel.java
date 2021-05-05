package org.ebur.debitum.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * used in EditTransactionFragment to get a newly created person name from EditPersonFragment
 * Obervers should scope this ViewModel to themselvses (getCurrentBackStackEntry) to make it
 * short-lived and avoid side-effects
 */
public class NewPersonRequestViewModel extends ViewModel {
    private final MutableLiveData<String> newPersonName = new MutableLiveData<>();

    public void setNewPersonName(String name) {
        this.newPersonName.setValue(name);
    }

    public LiveData<String> getNewPersonName() {
            return newPersonName;
    }

}
