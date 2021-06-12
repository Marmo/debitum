package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class ItemReturnedFilterViewModel extends AndroidViewModel {

    public static final int FILTER_RETURNED = 0x10;
    public static final int FILTER_UNRETURNED = 0x01;
    public static final int FILTER_ALL = 0x11;

    private int filterMode = FILTER_UNRETURNED;

    public ItemReturnedFilterViewModel(@NonNull Application application) {
        super(application);
    }

    public void setFilterMode(int mode) {
        filterMode = mode;
    }

    public int getFilterMode() {
        return filterMode;
    }
}
