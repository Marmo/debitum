package org.ebur.debitum.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import org.ebur.debitum.database.TransactionRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;

    public SettingsViewModel(Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
    }

    public void changeTransactionDecimals(int shift) {
        transactionRepository.changeTransactionDecimals(shift);
    }
}