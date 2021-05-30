package org.ebur.debitum.ui;

import android.animation.Animator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.Slide;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialContainerTransform;

import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.viewModel.EditTransactionViewModel;
import org.ebur.debitum.viewModel.NewPersonRequestViewModel;
import org.ebur.debitum.viewModel.PersonFilterViewModel;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

// https://medium.com/alexander-schaefer/implementing-the-new-material-design-full-screen-dialog-for-android-e9dcc712cb38
public class EditTransactionFragment extends DialogFragment {

    private static final String TAG = "EditTransactionFragment";
    public static final String ARG_ID_TRANSACTION = "idTransaction";
    public static final String ARG_ID_NEW_ITEM = "newItem";

    private EditTransactionViewModel viewModel;
    private PersonFilterViewModel personFilterViewModel;

    private Toolbar toolbar;
    private TextInputLayout spinnerNameLayout;
    private AutoCompleteTextView spinnerName;
    private ArrayAdapter<String> spinnerNameAdapter;
    private RadioButton gaveRadio;
    private TextInputLayout editAmountLayout;
    private EditText editAmount;
    private SwitchMaterial switchIsMonetary;
    private TextInputLayout editDescriptionLayout;
    private EditText editDescription;
    private AutoCompleteTextView editDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Debitum_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(EditTransactionViewModel.class);
        personFilterViewModel = new ViewModelProvider(requireActivity()).get(PersonFilterViewModel.class);

        View root = inflater.inflate(R.layout.fragment_edit_transaction, container, false);

        // get Transaction ID from Arguments, which is also used to determine, if a new transaction is created
        viewModel.setIdTransaction(requireArguments().getInt(ARG_ID_TRANSACTION, -1));

        // setup views
        toolbar = root.findViewById(R.id.dialog_toolbar);
        spinnerNameLayout = root.findViewById(R.id.spinner_name);
        Button buttonNewPerson = root.findViewById(R.id.button_new_person);
        buttonNewPerson.setOnClickListener(this::onNewPersonAction);
        gaveRadio = root.findViewById(R.id.radioButton_gave);
        editAmountLayout = root.findViewById(R.id.edit_amount);
        editAmount = editAmountLayout.getEditText();
        assert editAmount != null;
        editAmount.addTextChangedListener(new AmountTextWatcher());
        switchIsMonetary = root.findViewById(R.id.switch_monetary);
        switchIsMonetary.setOnCheckedChangeListener(this::onSwitchIsMonetaryChanged);
        editDescriptionLayout = root.findViewById(R.id.edit_description);
        editDescription = editDescriptionLayout.getEditText();
        TextInputLayout editDateLayout = root.findViewById(R.id.edit_date);
        editDateLayout.setOnClickListener(this::showDatePickerDialog);
        editDate = (AutoCompleteTextView) editDateLayout.getEditText();
        assert editDate != null;
        editDate.setOnClickListener(this::showDatePickerDialog);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.inflateMenu(R.menu.menu_edit_transaction);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        setupSpinnerName();
        prefillSpinnerNameIfFromFilteredTransactionList();

        if (viewModel.isNewTransaction()) fillViewsNewTransaction();
        else fillViewsEditTransaction();

        // set initial focus
        if (switchIsMonetary.isChecked()) {
            editAmount.requestFocus();
        } else {
            editDescription.requestFocus();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // make dialog fullscreen
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void setupSpinnerName() {
        spinnerName = (AutoCompleteTextView) spinnerNameLayout.getEditText();
        spinnerNameAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner);
        spinnerName.setAdapter(spinnerNameAdapter);
        // since an observed person-LiveData would be filled initially too late, we have to fill the adapter manually
        // this fixes a IllegalStateException in RecyclerView after completion
        try {
            for(Person person : viewModel.getPersons())
                spinnerNameAdapter.add(person.name);
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void prefillSpinnerNameIfFromFilteredTransactionList() {
        // Check if we come from a TransactionListFragment that was filtered by person
        // If this is the case AND we want to create a new transaction prefill the name spinner with
        // the name by which the TransactionListFragment was filtered
        NavBackStackEntry previous = NavHostFragment.findNavController(this).getPreviousBackStackEntry();
        int previousDestId = 0;
        if (previous != null)
            previousDestId = previous.getDestination().getId();
        if (previousDestId == R.id.money_dest
                || previousDestId == R.id.item_dest) {
            Person filterPerson = personFilterViewModel.getFilterPerson();
            if (filterPerson != null && viewModel.getIdTransaction() == -1) { // TransactionList was filtered by Person and we are creating a new Transaction
                spinnerName.setText(filterPerson.name, false); // IMPORTANT: filter=false, else the dropdown will be filtered to the selected name
            }
        }
    }

    private void fillViewsNewTransaction() {
        toolbar.setTitle(R.string.title_fragment_edit_transaction_create);
        if(requireArguments().getBoolean(ARG_ID_NEW_ITEM, false)) {
            editAmount.setText("1");
        } else {
            switchIsMonetary.setChecked(!requireArguments().getBoolean(ARG_ID_NEW_ITEM, false));
        }
        viewModel.setTimestamp(new Date());
        editDate.setText(Utilities.formatDate(viewModel.getTimestamp()));
    }

    private void fillViewsEditTransaction() {
        toolbar.setTitle(R.string.title_fragment_edit_transaction);

        TransactionWithPerson txn = null;
        try {
            txn = viewModel.getTransaction(viewModel.getIdTransaction());
        } catch(ExecutionException|InterruptedException e) {
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Toast.makeText(getContext(),  errorMessage, Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).navigateUp();
        }
        assert txn != null;
        spinnerName.setText(txn.person.name, false);  // IMPORTANT: filter=false, else the dropdown will be filtered to the selected name
        gaveRadio.setChecked(txn.transaction.amount>0); // per default received is set (see layout xml)
        // IMPORTANT: set switchIsMonetaryView _before_ setting amount, because on setting amount the
        // AmountTextWatcher::afterTextChanged is called, and within this method isMonetary is needed to apply correct formatting!
        switchIsMonetary.setChecked(txn.transaction.isMonetary);
        editAmount.setText(txn.transaction.getFormattedAmount(false));
        editDescription.setText(txn.transaction.description);
        viewModel.setTimestamp(txn.transaction.timestamp);
        editDate.setText(Utilities.formatDate(viewModel.getTimestamp()));
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
        }
        return true;
    }

    public void onSaveTransactionAction() {

        // CHECK PRECONDITIONS FOR SAVING
        boolean nameEmpty = TextUtils.isEmpty(spinnerName.getText());
        boolean amountEmpty = TextUtils.isEmpty(editAmount.getText());
        boolean descEmptyAndItem = !switchIsMonetary.isChecked() && TextUtils.isEmpty(editDescription.getText());
        if (nameEmpty || amountEmpty || descEmptyAndItem ) {
            if(nameEmpty)
                spinnerNameLayout.setError(getString(R.string.edit_transaction_error_select_name));
            if(amountEmpty)
                editAmountLayout.setError(getText(R.string.edit_transaction_error_enter_amount));
            if(descEmptyAndItem)
                editDescriptionLayout.setError(getText(R.string.edit_transaction_error_enter_description));
        } else {
            //evaluate received-gave-radios
            int factor = -1;
            if (gaveRadio.isChecked()) factor = 1;

            boolean isMonetary = switchIsMonetary.isChecked();

            // parse amount
            // user is expected to enter something like "10.05"(€/$/...) and we want to store 1005 (format is enforced by AmountTextWatcher)
            if (isMonetary) factor *= 100;
            int amount;
            try {
                amount = (int) (factor * Utilities.parseAmount(editAmount.getText().toString()));
            } catch (ParseException e) {
                Toast.makeText(requireActivity(), R.string.edit_transaction_wrong_amount_format, Toast.LENGTH_SHORT).show();
                return;
            }

            // get person id from selected person name
            int idPerson = -1;
            try {
                idPerson = viewModel.getPersonId(spinnerName.getText().toString());
            } catch (ExecutionException | InterruptedException e) {
                String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }

            // build transaction
            Transaction transaction = new Transaction(idPerson,
                    amount,
                    isMonetary,
                    editDescription.getText().toString(),
                    viewModel.getTimestamp());

            // update database
            if(viewModel.isNewTransaction()) viewModel.insert(transaction);
            else if (!viewModel.isNewTransaction()) {
                transaction.idTransaction = viewModel.getIdTransaction();
                viewModel.update(transaction);
            }

            NavHostFragment.findNavController(this).navigateUp();
        }
    }

    private void onNewPersonAction(View view) {
        NavController nav = NavHostFragment.findNavController(this);
        NavBackStackEntry requester = nav.getCurrentBackStackEntry();
        assert requester != null;
        NewPersonRequestViewModel requestViewModel =
                new ViewModelProvider(requester).get(NewPersonRequestViewModel.class);
        requestViewModel.getNewPersonName().observe(getViewLifecycleOwner(), newPersonName -> {
            spinnerNameAdapter.add(newPersonName);
            spinnerNameAdapter.sort(String::compareTo);
            spinnerName.setText(newPersonName, false);
            requestViewModel.getNewPersonName().removeObservers(requester);
        });
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(view, "to_edit_person")
                .build();
        NavHostFragment.findNavController(this).navigate(R.id.action_requestNewPerson, null, null, extras);
    }

    // ---------------------------
    // Date and TimePicker dialogs
    // ---------------------------

    public void showDatePickerDialog(View v) {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText(R.string.edit_transaction_date_dialog_title)
                        .setSelection(viewModel.getTimestamp().getTime())
                        .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setTimestamp(new Date(selection));
            editDate.setText(Utilities.formatDate(new Date(selection)));
        });
        datePicker.show(getParentFragmentManager(), "addTransactionDatePicker");
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
            String str = s.toString();
            // nothing to do on empty strings
            if (str.isEmpty()) return;
            // prevent us from looping infinitely
            if (str.equals(formattedAmount)) return;
            formattedAmount = formatArbitraryDecimalInput(str);
            editAmount.setText(formattedAmount);
            // prevent cursor to jump to front
            editAmount.setSelection(editAmount.length());
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

        if (switchIsMonetary.isChecked()) {
            // add decSep two digits from the right, while adding leading zeros if needed
            // this is accomplished by removing decSep --> converting to int --> dividing by 100 --> converting to local String

            formattedAmount = Transaction.formatMonetaryAmount(Integer.parseInt(formattedAmount), Locale.getDefault());
        } else {
            // remove leading 0s
            formattedAmount = formattedAmount.replaceFirst("^0+","");
        }
        return formattedAmount;
    }

    //-------------------------------
    // Toggle isMonetary-Switch-Label
    //-------------------------------

    public void onSwitchIsMonetaryChanged(View v, boolean checked) {
        Resources res = getResources();
        Drawable startIcon;

        if (checked) {
            startIcon = res.getDrawable(R.drawable.ic_baseline_money_24, null);
            editAmountLayout.setHint(R.string.edit_transaction_hint_amount_money);
            editDescriptionLayout.setHint(R.string.edit_transaction_hint_desc);
            editDescriptionLayout.setError(null);
            editDescriptionLayout.setHelperText(null);
        } else {
            startIcon = res.getDrawable(R.drawable.ic_baseline_tag_24, null);
            editAmountLayout.setHint(R.string.edit_transaction_hint_amount_item);
            editDescriptionLayout.setHint(R.string.edit_transaction_hint_desc_item);
            editDescriptionLayout.setHelperText(getString(R.string.required_helper_text));
        }

        // apply proper formatting for chosen amount type
        String s = editAmount.getText().toString();
        if(!s.isEmpty()) {
            editAmount.setText(formatArbitraryDecimalInput(s));
        }

        // change editAmount icon;
        editAmountLayout.setStartIconDrawable(startIcon);
    }
}