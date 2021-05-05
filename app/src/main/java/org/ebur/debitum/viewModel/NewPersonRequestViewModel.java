package org.ebur.debitum.viewModel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NewPersonRequestViewModel extends ViewModel {
    private final MutableLiveData<String> newPersonName = new MutableLiveData<>();

    public void setNewPersonName(String name) {
        this.newPersonName.setValue(name);
    }

    public LiveData<String> getNewPersonName() {
            return newPersonName;
    }

}
