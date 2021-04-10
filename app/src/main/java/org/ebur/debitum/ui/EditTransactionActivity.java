package org.ebur.debitum.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.EditTransactionViewModel;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class EditTransactionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditTransactionViewModel viewModel;

    ArrayAdapter<String> nameSpinnerAdapter;

    private Spinner spinnerNameView;
    private RadioButton gaveRadio;
    private EditText editAmountView;
    private SwitchCompat switchIsMonetaryView;
    private EditText editDescView;
    private TextView editDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);
        viewModel = new ViewModelProvider(this).get(EditTransactionViewModel.class);

        // determine if we want to create a new transaction
        viewModel.setNewTransaction(getIntent().getBooleanExtra(MainActivity.EXTRA_NEW_TRANSACTION, false));

        // setup views
        spinnerNameView = findViewById(R.id.spinner_name);
        gaveRadio = findViewById(R.id.radioButton_gave);
        editAmountView = findViewById(R.id.edit_amount);
        editAmountView.addTextChangedListener(new AmountTextWatcher());
        switchIsMonetaryView = findViewById(R.id.switch_monetary);
        editDescView = findViewById(R.id.edit_description);
        editDateView = findViewById(R.id.edit_date);

        // setup name spinner
        nameSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        nameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNameView.setAdapter(nameSpinnerAdapter);
        spinnerNameView.setOnItemSelectedListener(this);

        // observe ViewModel's LiveData
        /*viewModel.getPersons().observe(this, persons -> {
            nameSpinnerAdapter.clear();
            for(Person person : persons) {
                nameSpinnerAdapter.add(person.name);
            }
        });*/

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // since an observed person-LiveData would be filled initially too late, we have to fill the adapter manually
        try {
            for(Person person : viewModel.getPersons()) {
                nameSpinnerAdapter.add(person.name);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (viewModel.isNewTransaction()) fillViewsNewTransaction();
        else fillViewsEditTransaction();
    }

    private void fillViewsNewTransaction() {
        getSupportActionBar().setTitle(R.string.title_activity_edit_transaction_add);
        viewModel.setTimestamp(new Date());
        editDateView.setText(Utilities.formatDate(viewModel.getTimestamp(),
                getString(R.string.date_format)));
    }

    private void fillViewsEditTransaction() {
        viewModel.setIdTransaction(getIntent().getIntExtra("ID_TRANSACTION", -1));
        TransactionWithPerson txn = null;
        try {
            txn = viewModel.getTransaction(viewModel.getIdTransaction());
        } catch(ExecutionException|InterruptedException e) {
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Toast.makeText(getApplicationContext(),  errorMessage, Toast.LENGTH_LONG).show();
            finish();
        }
        spinnerNameView.setSelection(nameSpinnerAdapter.getPosition(txn.person.name));
        gaveRadio.setChecked(txn.transaction.amount<0);
        // IMPORTANT: set switchIsMonetaryView _before_ setting amount, because on setting amount the
        // AmountTextWatcher::afterTextChanged is called, and within this method isMonetary is needed to apply correct formatting!
        switchIsMonetaryView.setChecked(txn.transaction.isMonetary);
        editAmountView.setText(txn.transaction.getFormattedAmount(false));
        editDescView.setText(txn.transaction.description);
        viewModel.setTimestamp(txn.transaction.timestamp);
        editDateView.setText(Utilities.formatDate(viewModel.getTimestamp(),
                getString(R.string.date_format)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_transaction, menu);

        //remove delete menu item when creating new person
        if(viewModel.isNewTransaction()) menu.removeItem(R.id.miDeleteTransaction);

        return true;
    }

    // -----------------------------------
    // Toolbar Menu buttons event handling
    // -----------------------------------

    public void onSaveTransactionAction(MenuItem item) {

            // at least name and amount have to be filled
            if (TextUtils.isEmpty(viewModel.getName()) || TextUtils.isEmpty(editAmountView.getText())) {
                Toast.makeText(this, R.string.add_transaction_incomplete_data, Toast.LENGTH_SHORT).show();
            } else {
                //evaluate received-gave-radios
                int factor = 1;
                if (gaveRadio.isChecked()) factor = -1;

                boolean isMonetary = switchIsMonetaryView.isChecked();

                // parse amount
                // user is expected to enter something like "10.05"(€/$/...) and we want to store 1005 (format is enforced by AmountTextWatcher)
                // TODO handle different input possibilities, including not parseable ones
                // TODO limit max number of decimal places https://www.tutorialspoint.com/how-to-limit-decimal-places-in-android-edittext
                //      https://exceptionshub.com/limit-decimal-places-in-android-edittext.html
                if (isMonetary) factor *= 100;
                int amount = 0;
                try {
                    amount = (int) (factor * Utilities.parseAmount(editAmountView.getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(this, R.string.add_transaction_wrong_amount_format, Toast.LENGTH_SHORT).show();
                    return;
                }

                // get person id from selected person name
                int idPerson = -1;
                try {
                    idPerson = viewModel.getSelectedPersonId();
                } catch (ExecutionException | InterruptedException e) {
                    String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }

                // build transaction
                Transaction transaction = new Transaction(idPerson,
                        amount,
                        isMonetary,
                        editDescView.getText().toString(),
                        viewModel.getTimestamp());

                // update database
                if(viewModel.isNewTransaction()) viewModel.insert(transaction);
                else if (!viewModel.isNewTransaction()) {
                    transaction.idTransaction = viewModel.getIdTransaction();
                    viewModel.update(transaction);
                }

                finish();
            }
    }

    public void onDeleteTransactionAction(MenuItem item) {
        // build Transaction
        Transaction transaction = new Transaction();
        transaction.idTransaction = viewModel.getIdTransaction();
        // TODO ask if we really want to delete the transaction?
        //    https://developer.android.com/guide/topics/ui/dialogs

        // delete from database via viewModel
        viewModel.delete(transaction);

        // TODO show Snackbar confirming delete
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.FragmentTransactionList_constraintLayout), "Transaction deleted", Snackbar.LENGTH_SHORT);
        mySnackbar.show();

        finish();
    }

    // ---------------------------
    // Date and TimePicker dialogs
    // ---------------------------

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "addTransactionDatePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            EditTransactionActivity activity = (EditTransactionActivity) requireActivity();
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            Date d = new Date(c.getTimeInMillis());
            activity.viewModel.setTimestamp(d);
            activity.editDateView.setText(Utilities.formatDate(d, getString(R.string.date_format)));
        }
    }

    // ---------------------------
    // Name Spinner event handling
    // ---------------------------

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        viewModel.setName(parent.getItemAtPosition(pos).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    // ---------------------------------------------------------
    // Enforce correct input in editAmountView
    // ---------------------------------------------------------

    class AmountTextWatcher implements TextWatcher {

        String formattedAmount = "";
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // prevent us from looping infinitely
            if (s.toString().equals(formattedAmount)) return;
            formattedAmount = formatArbitraryDecimalInput(s.toString());
            editAmountView.setText(formattedAmount);
            // prevent cursor to jump to front
            editAmountView.setSelection(editAmountView.length());;
        }
    }

    private String formatArbitraryDecimalInput(String input) {
        // examples (monetary): 1 --> 0,01; 0,012 --> 0,12; 0,123 --> 1,23; 1,234 --> 12,34; 12,345 --> 123,45; 123,4 --> 12,34

        String formattedAmount = "";
        // remove all decimal separators (this the final result for non-monetaries, where only integers are allowed)
        formattedAmount = input.replaceAll("[.,]", "");

        if (switchIsMonetaryView.isChecked()) {
            // add decSep two digits from the right, while adding leading zeros if needed
            // this is accomplished by removing decSep --> converting to int --> dividing by 100 --> converting to local String
            formattedAmount = Transaction.formatMonetaryAmount(Integer.parseInt(formattedAmount), Locale.getDefault());
        }
        return formattedAmount;
    }
}