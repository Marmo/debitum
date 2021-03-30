package org.ebur.debitum.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
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
import org.ebur.debitum.viewModel.AddTransactionViewModel;

import java.util.Calendar;
import java.util.Date;

public class AddTransactionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String EXTRA_REPLY = "org.ebur.debitum.transactionlistsql.REPLY";

    private AddTransactionViewModel viewModel;

    private Spinner spinnerNameView;
    private RadioButton gaveRadio;
    private EditText editAmountView;
    private SwitchCompat switchIsMonetaryView;
    private EditText editDescView;
    private TextView editDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        // TODO use toolbar button instead of separate save button
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        spinnerNameView = findViewById(R.id.spinner_name);
        gaveRadio = findViewById(R.id.radioButton_gave);
        editAmountView = findViewById(R.id.edit_amount);
        switchIsMonetaryView = findViewById(R.id.switch_monetary);
        editDescView = findViewById(R.id.edit_description);
        editDateView = findViewById(R.id.edit_date);

        // initialize name spinner
        ArrayAdapter<String> nameSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        nameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNameView.setAdapter(nameSpinnerAdapter);
        spinnerNameView.setOnItemSelectedListener(this);

        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(this).get(AddTransactionViewModel.class);
        viewModel.getPersons().observe(this, persons -> {
            // update contents of [spinnerNameView] via [nameSpinnerAdapter]
            nameSpinnerAdapter.clear();
            for(Person person : persons) {
                nameSpinnerAdapter.add(person.name);
            }
        });


        final Button saveButton = findViewById(R.id.button_save);
        // TODO make lambda a separate method (for better readability)
        saveButton.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(viewModel.getName()) || TextUtils.isEmpty(editAmountView.getText())) {
                Toast toast = Toast.makeText(this, R.string.add_transaction_incomplete_data, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                //evaluate received-gave-radios
                int factor = 1;
                if(gaveRadio.isChecked()) factor = -1;

                Bundle extras = new Bundle();
                extras.putInt("PERSON_ID", viewModel.getPersonId());
                // TODO handle different input possibilities, including not parseable ones
                extras.putInt("AMOUNT", factor*Integer.parseInt(editAmountView.getText().toString()));
                extras.putBoolean("ISMONETARY", switchIsMonetaryView.isChecked());
                extras.putString("DESC", editDescView.getText().toString());
                extras.putLong("TIMESTAMP", viewModel.getTimestamp().getTime());
                replyIntent.putExtras(extras);
                setResult(RESULT_OK, replyIntent);

                finish();
            }
        });

        // initialize date
        viewModel.setTimestamp(new Date());
        editDateView.setText(Utilities.formatDate(viewModel.getTimestamp(),
                getString(R.string.add_transaction_date_format)));
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
            AddTransactionActivity activity = (AddTransactionActivity) requireActivity();
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            Date d = new Date(c.getTimeInMillis());
            activity.viewModel.setTimestamp(d);
            activity.editDateView.setText(Utilities.formatDate(d, getString(R.string.add_transaction_date_format)));
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