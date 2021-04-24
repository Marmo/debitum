package org.ebur.debitum.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.EditTransactionViewModel;
import org.ebur.debitum.viewModel.PersonFilterViewModel;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class EditTransactionFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String ARG_ID_TRANSACTION = "idTransaction";

    private EditTransactionViewModel viewModel;
    private PersonFilterViewModel personFilterViewModel;
    private NavController nav;

    ArrayAdapter<String> nameSpinnerAdapter;

    private Spinner spinnerNameView;
    private RadioButton gaveRadio;
    private EditText editAmountView;
    private SwitchCompat switchIsMonetaryView;
    private EditText editDescView;
    private TextView editDateView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(EditTransactionViewModel.class);
        personFilterViewModel = new ViewModelProvider(requireActivity()).get(PersonFilterViewModel.class);
        nav = NavHostFragment.findNavController(this);

        View root = inflater.inflate(R.layout.fragment_edit_transaction, container, false);

        // get Transaction ID from Arguments, which is also used to determine if a new transaction is created
        viewModel.setIdTransaction(requireArguments().getInt(ARG_ID_TRANSACTION, -1));

        // setup views
        spinnerNameView = root.findViewById(R.id.spinner_name);
        gaveRadio = root.findViewById(R.id.radioButton_gave);
        editAmountView = root.findViewById(R.id.edit_amount);
        editAmountView.addTextChangedListener(new AmountTextWatcher());
        switchIsMonetaryView = root.findViewById(R.id.switch_monetary);
        switchIsMonetaryView.setOnCheckedChangeListener(this::onSwitchIsMonetaryChanged);
        editDescView = root.findViewById(R.id.edit_description);
        editDateView = root.findViewById(R.id.edit_date);
        editDateView.setOnClickListener((view) -> showDatePickerDialog());

        // setup name spinner
        nameSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        nameSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNameView.setAdapter(nameSpinnerAdapter);
        spinnerNameView.setOnItemSelectedListener(this);


        fillSpinnerNameView();
        prefillNameViewIfFromFilteredTransactionList();

        if (viewModel.isNewTransaction()) fillViewsNewTransaction();
        else fillViewsEditTransaction();

        setHasOptionsMenu(true);

        return root;
    }

    private void fillSpinnerNameView() {
        // since an observed person-LiveData would be filled initially too late, we have to fill the adapter manually
        // this fixes a IllegalStateException in RecyclerView after completion
        try {
            for(Person person : viewModel.getPersons()) nameSpinnerAdapter.add(person.name);
        } catch (ExecutionException | InterruptedException e) {
            // TODO better exception handling
            e.printStackTrace();
        }
    }

    private void prefillNameViewIfFromFilteredTransactionList() {
        // Check if we come from a TransactionListFragment that was filtered by person
        // If this is the case AND we want to create a new transaction prefill the name spinner with
        // the name by which the TransactionListFragment was filtered
        NavBackStackEntry previous = nav.getPreviousBackStackEntry();
        int previousDestId = 0;
        if (previous != null)
            previousDestId = previous.getDestination().getId();
        if (previousDestId == R.id.transactionListFragment
                || previousDestId == R.id.itemTransactionListFragment) {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            if (filterPerson != null && viewModel.getIdTransaction() == -1) { // TransactionList was filtered by Person and we are creating a new Transaction
                spinnerNameView.setSelection(nameSpinnerAdapter.getPosition(filterPerson.name));
                viewModel.setSelectedName(filterPerson.name);
            }
        }
    }

    private void fillViewsNewTransaction() {
        ((MainActivity) requireActivity()).setToolbarTitle(R.string.title_fragment_edit_transaction_add);
        viewModel.setTimestamp(new Date());
        editDateView.setText(Utilities.formatDate(viewModel.getTimestamp(),
                getString(R.string.date_format)));
    }

    private void fillViewsEditTransaction() {
        TransactionWithPerson txn = null;
        try {
            txn = viewModel.getTransaction(viewModel.getIdTransaction());
        } catch(ExecutionException|InterruptedException e) {
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Toast.makeText(getContext(),  errorMessage, Toast.LENGTH_LONG).show();
            nav.navigateUp();
        }
        spinnerNameView.setSelection(nameSpinnerAdapter.getPosition(txn.person.name));
        gaveRadio.setChecked(txn.transaction.amount>0); // per default received is set (see layout xml)
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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_edit_transaction, menu);

        //delete menu item senseless when creating new person
        if(viewModel.isNewTransaction()) menu.removeItem(R.id.miDeleteTransaction);
    }

    // ---------------------------
    // Toolbar Menu event handling
    // ---------------------------

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miSaveTransaction) {
            onSaveTransactionAction();
            return true;
        } else if(id==R.id.miDeleteTransaction) {
            onDeleteTransactionAction();
            return true;
        }
        return true;
    }

    public void onSaveTransactionAction() {

            // at least name and amount have to be filled
            if (TextUtils.isEmpty(viewModel.getSelectedName()) || TextUtils.isEmpty(editAmountView.getText())) {
                Toast.makeText(requireContext(), R.string.add_transaction_incomplete_data, Toast.LENGTH_SHORT).show();
            } else {
                //evaluate received-gave-radios
                int factor = -1;
                if (gaveRadio.isChecked()) factor = 1;

                boolean isMonetary = switchIsMonetaryView.isChecked();

                // parse amount
                // user is expected to enter something like "10.05"(€/$/...) and we want to store 1005 (format is enforced by AmountTextWatcher)
                if (isMonetary) factor *= 100;
                int amount;
                try {
                    amount = (int) (factor * Utilities.parseAmount(editAmountView.getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(requireActivity(), R.string.add_transaction_wrong_amount_format, Toast.LENGTH_SHORT).show();
                    return;
                }

                // get person id from selected person name
                int idPerson = -1;
                try {
                    idPerson = viewModel.getSelectedPersonId();
                } catch (ExecutionException | InterruptedException e) {
                    String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
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

                nav.navigateUp();
            }
    }

    public void onDeleteTransactionAction() {
        // build Transaction
        Transaction transaction = new Transaction();
        transaction.idTransaction = viewModel.getIdTransaction();

        // ask for confirmation
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setPositiveButton(R.string.delete_dialog_confirm, (dialog, id) -> {
            viewModel.delete(transaction);
            // navigate back to PersonSumListFragment, as any views related to the deleted Person will become invalid
            nav.navigateUp();
            Snackbar.make(requireView(),
                    R.string.edit_transaction_snackbar_deleted_transaction,
                    Snackbar.LENGTH_SHORT)
                    .show();
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> dialog.cancel());

        builder.setMessage(R.string.edit_transaction_confirm_deletion_text)
                .setTitle(R.string.edit_transaction_confirm_deletion_title);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    // ---------------------------
    // Date and TimePicker dialogs
    // ---------------------------

    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getParentFragmentManager(), "addTransactionDatePicker");
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
            EditTransactionFragment fragment = (EditTransactionFragment) getParentFragment();
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            Date d = new Date(c.getTimeInMillis());
            assert fragment != null;
            fragment.viewModel.setTimestamp(d);
            fragment.editDateView.setText(Utilities.formatDate(d, getString(R.string.date_format)));
        }
    }

    // ------------------------------------------------------
    // Name Spinner event handling / interface implementation
    // ------------------------------------------------------

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        viewModel.setSelectedName(parent.getItemAtPosition(pos).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // ---------------------------------------
    // Enforce correct input in editAmountView
    // ---------------------------------------

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
            editAmountView.setSelection(editAmountView.length());
        }
    }

    private String formatArbitraryDecimalInput(String input) {
        // examples (monetary): 1 --> 0,01; 0,012 --> 0,12; 0,123 --> 1,23; 1,234 --> 12,34; 12,345 --> 123,45; 123,4 --> 12,34

        String formattedAmount;
        // remove all decimal separators (this the final result for non-monetaries, where only integers are allowed)
        formattedAmount = input.replaceAll("[.,]", "");

        // check if input is short enough to be parsed as integer later
        if(formattedAmount.length()>9) { // we might be above 2^32=4.294.967.296 and later want to make an int of this String
            formattedAmount = formattedAmount.substring(0, formattedAmount.length() - 1); // so we simply remove the last digit
            Toast.makeText(requireContext(), R.string.edit_transaction_snackbar_max_amount, Toast.LENGTH_SHORT).show();
        }

        if (switchIsMonetaryView.isChecked()) {
            // add decSep two digits from the right, while adding leading zeros if needed
            // this is accomplished by removing decSep --> converting to int --> dividing by 100 --> converting to local String

            formattedAmount = Transaction.formatMonetaryAmount(Integer.parseInt(formattedAmount), Locale.getDefault());
        }
        return formattedAmount;
    }

    //-------------------------------
    // Toggle isMonetary-Switch-Label
    //-------------------------------

    public void onSwitchIsMonetaryChanged(View v, boolean checked) {
        if (checked) {
            switchIsMonetaryView.setText(R.string.switch_monetary_label_money);
        } else if (!checked) {
            switchIsMonetaryView.setText(R.string.switch_monetary_label_item);
        }
    }
}