package org.ebur.debitum.ui.edit_transaction;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.database.Transaction;
import org.ebur.debitum.database.TransactionWithPerson;
import org.ebur.debitum.ui.TextInputLayoutErrorResetter;
import org.ebur.debitum.util.FileUtils;
import org.ebur.debitum.util.Utilities;
import org.ebur.debitum.viewModel.EditTransactionViewModel;
import org.ebur.debitum.viewModel.NewPersonRequestViewModel;
import org.ebur.debitum.viewModel.PersonFilterViewModel;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

// https://medium.com/alexander-schaefer/implementing-the-new-material-design-full-screen-dialog-for-android-e9dcc712cb38
public class EditTransactionFragment extends DialogFragment {

    private static final String TAG = "EditTransactionFragment";
    public static final String ARG_ID_TRANSACTION = "idTransaction";
    public static final String ARG_PRESET_TYPE = "presetType";
    public static final String ARG_PRESET_NAME = "presetName";
    public static final String ARG_PRESET_AMOUNT = "presetAmount";
    public static final String ARG_PRESET_DESCRIPTION = "presetDescription";
    public static final String ARG_PRESET_DATE = "presetDate";
    public static final String ARG_PRESET_RETURNDATE = "presetReturndate";

    private final static String IMAGE_SUBDIR = "transaction-images";

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
    private TextInputLayout editReturnDateLayout;
    private AutoCompleteTextView editReturnDate;
    private RecyclerView imageRecyclerView;
    private EditTransactionImageAdapter imageAdapter;

    // launcher for picking a new image file
    protected final ActivityResultLauncher<String> addImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) { // uri is null when the picker was cancelled
                            File dir = getImageDir();
                            String filename = FileUtils.getNextImageFilename(dir)
                                    + "."
                                    + FileUtils.getFileExtension(uri, requireContext().getContentResolver());
                            File destFile = new File(dir, filename);
                            // copy image to txn dir
                            try {
                                FileUtils.copyFile(uri,
                                        destFile,
                                        requireContext().getContentResolver());
                                viewModel.addImageLink(destFile.getName());
                            } catch (IOException e) {
                                Toast.makeText(requireContext(), getString(R.string.edit_transaction_image_error_copying, e.getMessage()), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Debitum_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(EditTransactionViewModel.class);
        personFilterViewModel = new ViewModelProvider(requireActivity()).get(PersonFilterViewModel.class);

        View root = inflater.inflate(R.layout.fragment_edit_transaction, container, false);

        // get Transaction ID from Arguments and set viewModel's transaction
        int idTransaction = requireArguments().getInt(ARG_ID_TRANSACTION, -1);
        if (idTransaction == -1) {
            viewModel.setTransaction(null);
        } else {
            try {
                viewModel.setTransaction(viewModel.getTransactionFromDatabase(idTransaction));
            } catch(ExecutionException|InterruptedException e) {
                String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
                Log.e(TAG, errorMessage);
                // escape from this mess
                NavHostFragment.findNavController(this).navigateUp();
            }
        }

        // set viewModel's transactionType, look for preset if new transaction, else look in edited transaction
        int type;
        if(viewModel.isNewTransaction()) {
            // new transaction --> look for preset type
            type = requireArguments().getInt(ARG_PRESET_TYPE, Transaction.TYPE_MONEY);
        } else if (!viewModel.getTransaction().transaction.isMonetary) {
            // existing item transaction, ignore preset
            type = Transaction.TYPE_ITEM;
        } else {
            // existing money transaction
            type = Transaction.TYPE_MONEY;
        }
        viewModel.setTransactionType(type);

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
        editAmount.addTextChangedListener(new TextInputLayoutErrorResetter(editAmountLayout));

        switchIsMonetary = root.findViewById(R.id.switch_monetary);
        switchIsMonetary.setOnCheckedChangeListener(this::onSwitchIsMonetaryChanged);

        editDescriptionLayout = root.findViewById(R.id.edit_description);
        editDescription = editDescriptionLayout.getEditText();
        assert editDescription!=null;
        editDescription.addTextChangedListener(new TextInputLayoutErrorResetter(editDescriptionLayout));

        TextInputLayout editDateLayout = root.findViewById(R.id.edit_date);
        editDate = (AutoCompleteTextView) editDateLayout.getEditText();
        assert editDate != null;
        editDate.setOnClickListener(view -> showDatePickerDialog(view,
                viewModel.getTimestamp().getTime(),
                selection -> {
                    viewModel.setTimestamp(new Date(selection)); // TODO use viewModel's LiveData
                    editDate.setText(Utilities.formatDate(new Date(selection), requireContext()));
        }));

        editReturnDateLayout = root.findViewById(R.id.edit_returndate);
        editReturnDate = (AutoCompleteTextView) editReturnDateLayout.getEditText();
        assert editReturnDate != null;
        editReturnDate.setOnClickListener(view -> showDatePickerDialog(
                view,
                viewModel.getReturnTimestamp() != null ? viewModel.getReturnTimestamp().getTime() : new Date().getTime(),
                this::onReturnDateSet)
        );
        editReturnDateLayout.setEndIconOnClickListener(view -> {
            editReturnDate.setText(null); // TODO use viewModel's LiveData
            viewModel.setReturnTimestamp(null);
        });

        setupRecyclerView(root);
        subscribeToViewModel();

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

        // set initial focus & show or hide return date input
        if (viewModel.isMoneyTransaction()) {
            editAmount.requestFocus();
            editReturnDateLayout.setVisibility(View.GONE);
        } else {
            editDescription.requestFocus();
            editReturnDateLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismiss() {
        viewModel.deleteOrphanedImageFiles(getImageDir());
        super.dismiss();
    }

    private void setupRecyclerView(@NonNull View root) {
        imageRecyclerView = root.findViewById(R.id.images);
        imageAdapter = new EditTransactionImageAdapter(new EditTransactionImageAdapter.Diff(), addImageLauncher, imagefile -> {
            viewModel.deleteImageLink(imagefile.getName()); // the actual file will be deleted by deleteOrphanedImageFiles on save/dismiss
        });
        imageRecyclerView.setAdapter(imageAdapter);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        // load images for existing txns
        // (for new ones this will set the viewModel's LiveData to an empty list)
        viewModel.loadImageFilenamesFromDb();
    }

    private void subscribeToViewModel() {
        viewModel.getImageFilenames().observe(getViewLifecycleOwner(), this::updateAdapterList);
    }

    private void updateAdapterList(List<String> filenames) {
        List<File> images = new ArrayList<>();
        File imagedir = getImageDir();
        if (!imagedir.exists()) {
            imagedir.mkdirs();
        }
        for (String filename:filenames) {
            File file = new File(imagedir, filename);
            if (file.exists() && file.canRead()) {
                images.add(file);
            }
        }
        images.add(null); // the null element makes the viewHolder display the "add image" placeholder
        imageAdapter.submitList(images);
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
        spinnerName.addTextChangedListener(new TextInputLayoutErrorResetter(spinnerNameLayout));
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
            if (filterPerson != null && viewModel.isNewTransaction()) { // TransactionList was filtered by Person and we are creating a new Transaction
                spinnerName.setText(filterPerson.name, false); // IMPORTANT: filter=false, else the dropdown will be filtered to the selected name
            }
        }
    }

    private void fillViewsNewTransaction() {
        toolbar.setTitle(R.string.title_fragment_edit_transaction_create);
        switchIsMonetary.setChecked(viewModel.isMoneyTransaction());
        if(viewModel.isItemTransaction()) {
            editAmount.setText("1");
        }
        viewModel.setTimestamp(new Date());
        editDate.setText(Utilities.formatDate(viewModel.getTimestamp(), requireContext()));

        evaluatePresets();
    }

    private void fillViewsEditTransaction() {
        toolbar.setTitle(R.string.title_fragment_edit_transaction);

        TransactionWithPerson txn = viewModel.getTransaction();
        assert txn != null; // we should never get here, if this is not the case
        spinnerName.setText(txn.person.name, false);  // IMPORTANT: filter=false, else the dropdown will be filtered to the selected name
        gaveRadio.setChecked(txn.transaction.amount>0); // per default received is set (see layout xml)
        // IMPORTANT: set switchIsMonetaryView _before_ setting amount, because on setting amount the
        // AmountTextWatcher::afterTextChanged is called, and within this method isMonetary is needed to apply correct formatting!
        switchIsMonetary.setChecked(txn.transaction.isMonetary);
        editAmount.setText(txn.transaction.getFormattedAmount(false, Utilities.getNrOfDecimals(requireContext())));
        editDescription.setText(txn.transaction.description);
        viewModel.setTimestamp(txn.transaction.timestamp);
        editDate.setText(Utilities.formatDate(viewModel.getTimestamp(), requireContext()));
        viewModel.setReturnTimestamp(txn.transaction.timestampReturned);
        editReturnDate.setText(Utilities.formatDate(viewModel.getReturnTimestamp(), requireContext()));
    }

    /**
     * preset all fields and respective viewModel members if in arguments, i.e. if dialog is called
     * from settle-debt-cab-button or has presets set from selection (getPresetsFromSelection)
     */
    private void evaluatePresets() {
        // TODO: move all those presets to viewModel (presetIdTransaction, presetType, preset...)
        int presetAmount = requireArguments().getInt(ARG_PRESET_AMOUNT, 0);
        if (presetAmount != 0) { // amount > 0 means person gave to user --> gave radio must be checked
            editAmount.setText(Transaction.formatMonetaryAmount(Math.abs(presetAmount), Utilities.getNrOfDecimals(requireContext())));
            gaveRadio.setChecked(presetAmount>0);
        }

        String presetDescription = requireArguments().getString(ARG_PRESET_DESCRIPTION, null);
        if (presetDescription != null) {
            editDescription.setText(presetDescription);
        }

        String presetName = requireArguments().getString(ARG_PRESET_NAME, null);
        if (presetName != null) {
            spinnerName.setText(presetName, false); // IMPORTANT: filter=false, else the dropdown will be filtered to the selected name
        }

        long presetDate = requireArguments().getLong(ARG_PRESET_DATE, Long.MIN_VALUE);
        if (presetDate != Long.MIN_VALUE) {
            viewModel.setTimestamp(new Date(presetDate));
            editDate.setText(Utilities.formatDate(viewModel.getTimestamp(), requireContext()));
        }

        long presetReturndate = requireArguments().getLong(ARG_PRESET_RETURNDATE, Long.MIN_VALUE);
        if (presetReturndate != Long.MIN_VALUE) {
            viewModel.setReturnTimestamp(new Date(presetReturndate));
            editDate.setText(Utilities.formatDate(viewModel.getReturnTimestamp(), requireContext()));
        }
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
            // OR                                       "10"                                  10
            // OR                                       "10.1"                                101
            // OR                                       "10.050"                              10050
            if (isMonetary) factor *= Double.valueOf(Math.pow(10, Utilities.getNrOfDecimals(requireContext()))).intValue();
            int amount;
            try {
                amount = Utilities.nextInt(factor * Utilities.parseAmount(editAmount.getText().toString()));
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
                Log.e(TAG, errorMessage);
            }

            // build transaction
            Transaction transaction = new Transaction(idPerson,
                    amount,
                    isMonetary,
                    editDescription.getText().toString(),
                    viewModel.getTimestamp());

            // record if txn has images
            transaction.hasImages = viewModel.hasImages();

            // update returned date for item txns
            if (viewModel.isItemTransaction()) {
                // check if return date is empty (could have been cleared by endIcon click which
                // stays unnoticed by the view model)
                if (editReturnDate.getText().toString().isEmpty())
                    viewModel.setReturnTimestamp(null);
                transaction.timestampReturned = viewModel.getReturnTimestamp();
            }

            // update database
            if (viewModel.isNewTransaction()) {
                Long id = viewModel.insert(transaction);
                if (id != null) {
                    viewModel.synchronizeDbWithViewModel(getImageDir(), id.intValue());
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_message_database_access, ""), Toast.LENGTH_LONG).show();
                    return; //do not close dialog, since nothing was saved
                }
            } else if (!viewModel.isNewTransaction()) {
                transaction.idTransaction = viewModel.getTransaction().transaction.idTransaction;
                viewModel.update(transaction);
                viewModel.synchronizeDbWithViewModel(getImageDir());
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

    // ------------------
    // Date Picker dialog
    // ------------------

    public void showDatePickerDialog(@SuppressWarnings("unused") View v,
                                     Long date,
                                     MaterialPickerOnPositiveButtonClickListener<Long> listener) {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText(R.string.edit_transaction_date_dialog_title)
                        .setSelection(date)
                        .build();
        datePicker.addOnPositiveButtonClickListener(listener);
        datePicker.show(getParentFragmentManager(), "addTransactionDatePicker");
    }

    public void onReturnDateSet(Long selection) {
        if(new Date(selection).before(viewModel.getTimestamp())) {
            showSuspiciousReturnDateWarning(new Date(selection));
        } else {
            viewModel.setReturnTimestamp(new Date(selection));
            editReturnDate.setText(Utilities.formatDate(new Date(selection), requireContext())); // TODO use LiveData
        }
    }

    public void showSuspiciousReturnDateWarning(Date newReturnDate) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setPositiveButton(R.string.edit_transaction_use_suspicious_return_date, (dialog, id) -> {
            viewModel.setReturnTimestamp(newReturnDate);
            editReturnDate.setText(Utilities.formatDate(newReturnDate, requireContext())); // TODO use LiveData
        });
        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> dialog.cancel());
        builder.setMessage(getResources().getString(
                R.string.edit_transaction_suspicious_return_date_message,
                Utilities.formatDate(newReturnDate, requireContext())
        ))
                .setTitle(R.string.edit_transaction_suspicious_return_date_title);

        builder.create().show();
    }

    // ---------------------------------------
    // Enforce correct input in editAmountView
    // ---------------------------------------

    class AmountTextWatcher implements TextWatcher {

        String formattedAmount = "";
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
        // examples (monetary, decimals setting = 2): 1 --> 0,01; 0,012 --> 0,12; 0,123 --> 1,23; 1,234 --> 12,34; 12,345 --> 123,45; 123,4 --> 12,34

        String formattedAmount;
        // remove all decimal separators (this the final result for non-monetaries, where only integers are allowed)
        formattedAmount = input.replaceAll("[^0-9]", "");
        if (formattedAmount.isEmpty()) return ""; // inputs like "." or "," might be accepted by the textInput, but would be emptied by the above regex and then cause an exception in Integer.parseInt below, so we escape here 

        // check if input is short enough to be parsed as integer later
        if(formattedAmount.length()>9) { // we might be above 2^32=4.294.967.296 and later want to make an int of this String
            formattedAmount = formattedAmount.substring(0, formattedAmount.length() - 1); // so we simply remove the last digit
            Toast.makeText(requireContext(), R.string.edit_transaction_snackbar_max_amount, Toast.LENGTH_SHORT).show();
        }

        if (viewModel.isMoneyTransaction()) {
            // add decSep two digits from the right, while adding leading zeros if needed
            // this is accomplished by removing decSep --> converting to int --> dividing by 100 --> converting to local String
            formattedAmount = Transaction.formatMonetaryAmount(
                    Integer.parseInt(formattedAmount),
                    Utilities.getNrOfDecimals(requireContext()),
                    Locale.getDefault()
            );
        } else {
            // remove leading 0s
            formattedAmount = formattedAmount.replaceFirst("^0+","");
        }
        return formattedAmount;
    }

    //---------------------------
    // Toggle isMonetary handling
    //---------------------------

    public void onSwitchIsMonetaryChanged(View v, boolean checked) {
        TransitionDrawable startIcon;
        startIcon = (TransitionDrawable) editAmountLayout.getStartIconDrawable();

        if (checked) {
            if (startIcon != null) {
                startIcon.setCrossFadeEnabled(true);
                startIcon.startTransition(0);
                startIcon.reverseTransition(50);
            }
            editAmountLayout.setHint(R.string.edit_transaction_hint_amount_money);
            editDescriptionLayout.setHint(R.string.edit_transaction_hint_desc);
            editDescriptionLayout.setError(null);
            editDescriptionLayout.setHelperText(null);
            Utilities.setVisibilityAnimated(editReturnDateLayout, View.GONE);
            viewModel.setTransactionType(Transaction.TYPE_MONEY);
        } else {
            if (startIcon != null) {
                startIcon.setCrossFadeEnabled(true);
                startIcon.startTransition(50);
            }
            editAmountLayout.setHint(R.string.edit_transaction_hint_amount_item);
            editDescriptionLayout.setHint(R.string.edit_transaction_hint_desc_item);
            editDescriptionLayout.setHelperText(getString(R.string.required_helper_text));
            Utilities.setVisibilityAnimated(editReturnDateLayout, View.VISIBLE);
            viewModel.setTransactionType(Transaction.TYPE_ITEM);
        }

        // apply proper formatting for chosen amount type by setting the text without changing it
        String s = editAmount.getText().toString();
        if(!s.isEmpty()) {
            editAmount.setText(formatArbitraryDecimalInput(s));
        }
    }

    @NonNull
    private File getImageDir() {
        return getImageDir(requireContext());
    }

    // static version for use in SettingsFragment
    @NonNull
    public static File getImageDir(@NonNull Context context) {
        return new File(context.getExternalFilesDir(null), IMAGE_SUBDIR);
    }
}
