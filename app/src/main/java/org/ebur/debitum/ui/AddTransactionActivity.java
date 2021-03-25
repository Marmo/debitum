package org.ebur.debitum.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ebur.debitum.R;

import java.util.Calendar;
import java.util.Date;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "org.ebur.debitum.transactionlistsql.REPLY";

    private EditText editNameView;
    private EditText editAmountView;
    private SwitchCompat switchIsMonetaryView;
    private EditText editDescView;
    private TextView editDateView;

    // TODO: This should probably be in a ViewModel, not directly here in the Activity
    private long date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        // TODO use toolbar button instead of separate save button
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        editNameView = findViewById(R.id.edit_name);
        editAmountView = findViewById(R.id.edit_amount);
        switchIsMonetaryView = findViewById(R.id.switch_monetary);
        editDescView = findViewById(R.id.edit_description);
        editDateView = findViewById(R.id.edit_date);

        final Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(editNameView.getText()) || TextUtils.isEmpty(editAmountView.getText())) {
                Toast toast = Toast.makeText(this, R.string.add_transaction_incomplete_data, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Bundle extras = new Bundle();
                extras.putString("NAME", editNameView.getText().toString());
                // TODO handle different input possibilities, including not parseable ones
                extras.putInt("AMOUNT", Integer.parseInt(editAmountView.getText().toString()));
                extras.putBoolean("ISMONETARY", switchIsMonetaryView.isChecked());
                extras.putString("DESC", editDescView.getText().toString());
                extras.putLong("TIMESTAMP", date);
                replyIntent.putExtras(extras);
                setResult(RESULT_OK, replyIntent);
            }
            finish();
        });

        // initialize date
        // TODO use ViewModel
        date = new Date().getTime();

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
            activity.editDateView.setText(getString(R.string.add_transaction_date_format, year, month, day));
            // TODO use ViewModel
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            activity.date = c.getTimeInMillis();
        }
    }


}