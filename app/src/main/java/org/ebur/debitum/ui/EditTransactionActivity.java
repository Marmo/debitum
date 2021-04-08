package org.ebur.debitum.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.viewModel.EditTransactionViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class EditTransactionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditTransactionViewModel viewModel;

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

        spinnerNameView = findViewById(R.id.spinner_name);
        gaveRadio = findViewById(R.id.radioButton_gave);
        editAmountView = findViewById(R.id.edit_amount);
        switchIsMonetaryView = findViewById(R.id.switch_monetary);
        editDescView = findViewById(R.id.edit_description);
        editDateView = findViewById(R.id.edit_date);

        // setup name spinner
        ArrayAdapter<String> nameSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        nameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNameView.setAdapter(nameSpinnerAdapter);
        spinnerNameView.setOnItemSelectedListener(this);

        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(this).get(EditTransactionViewModel.class);
        viewModel.getPersons().observe(this, persons -> {
            // update contents of [spinnerNameView] via [nameSpinnerAdapter]
            nameSpinnerAdapter.clear();
            for(Person person : persons) {
                nameSpinnerAdapter.add(person.name);
            }
        });

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel.setNewTransaction(getIntent().getBooleanExtra(MainActivity.EXTRA_NEW_TRANSACTION, false));
        if(viewModel.isNewTransaction()) getSupportActionBar().setTitle(R.string.title_activity_edit_person_add);

        // initialize date
        viewModel.setTimestamp(new Date());
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
        Intent replyIntent = new Intent();
        if (TextUtils.isEmpty(viewModel.getName()) || TextUtils.isEmpty(editAmountView.getText())) {
            Toast toast = Toast.makeText(this, R.string.add_transaction_incomplete_data, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            //evaluate received-gave-radios
            int factor = 1;
            if (gaveRadio.isChecked()) factor = -1;
            // user is expected to enter something like "10.05"(€/$/...) and we want to store 1005
            // TODO handle different input possibilities, including not parseable ones
            // TODO limit max number of decimal places https://www.tutorialspoint.com/how-to-limit-decimal-places-in-android-edittext
            //      https://exceptionshub.com/limit-decimal-places-in-android-edittext.html
            int amount = (int) (factor * Double.parseDouble(editAmountView.getText().toString()) * 100);

            int idPerson = -1;
            try {
                idPerson = viewModel.getSelectedPersonId();
            } catch (ExecutionException | InterruptedException e) {
                String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
            Transaction transaction = new Transaction(idPerson,
                    amount,
                    switchIsMonetaryView.isChecked(),
                    editDescView.getText().toString(),
                    viewModel.getTimestamp());
            viewModel.insert(transaction);


            finish();
        }
    }

    public void onDeleteTransactionAction(MenuItem item) {
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        viewModel.setName(parent.getItemAtPosition(pos).toString());
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}