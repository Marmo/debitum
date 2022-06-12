package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ItemReturnedFilterViewModel extends AndroidViewModel {

    public static final int FILTER_UNDEF = 0;
    public static final int FILTER_ALL = 0b11;
    public static final int FILTER_UNRETURNED = 0b10;
    public static final int FILTER_RETURNED = 0b01;

    private final MutableLiveData<Integer> filterMode;

    public ItemReturnedFilterViewModel(@NonNull Application application) {
        super(application);
        filterMode = new MutableLiveData<>(FILTER_UNDEF);
    }

    public void setFilterMode(int mode) {
        if (mode == FILTER_RETURNED || mode == FILTER_UNRETURNED || mode == FILTER_ALL)
            filterMode.setValue(mode);
        else throw new IllegalArgumentException("Unknown filter mode: " + mode);
    }

    @NonNull
    public LiveData<Integer> getFilterMode() {
        return filterMode;
    }
}
