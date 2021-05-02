package org.ebur.debitum.ui;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.viewModel.EditPersonViewModel;

import java.util.concurrent.ExecutionException;

public class EditPersonFragment extends DialogFragment {

    public static final String ARG_EDITED_PERSON = "editedPerson";

    private EditPersonViewModel viewModel;

    private Toolbar toolbar;
    private TextInputLayout nameViewLayout;
    private TextInputEditText nameView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Debitum_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EditPersonViewModel.class);

        View root = inflater.inflate(R.layout.fragment_edit_person, container, false);

        toolbar = root.findViewById(R.id.dialog_toolbar);
        nameViewLayout = root.findViewById(R.id.edit_person_name);
        nameView = (TextInputEditText) nameViewLayout.getEditText();
        assert nameView != null;
        nameView.addTextChangedListener(new NameTextWatcher());

        Person editedPerson = requireArguments().getParcelable(ARG_EDITED_PERSON);
        viewModel.setEditedPerson(editedPerson);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.inflateMenu(R.menu.menu_edit_person);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        // adding or editing a person?
        if(!viewModel.isNewPerson()) {
            nameView.setText(viewModel.getEditedPerson().name);
            toolbar.setTitle(R.string.title_fragment_edit_person);
        }
        else {
            toolbar.setTitle(R.string.title_fragment_edit_person_add);
        }
    }

    // make dialog fullscreen
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    // ---------------------------
    // Toolbar Menu event handling
    // ---------------------------

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.miSavePerson) {
            onSavePersonAction();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onSavePersonAction() {
        String name;

        // check if nameView has contents
        if(TextUtils.isEmpty(nameView.getText())) {
            nameViewLayout.setError(getString(R.string.error_message_enter_name));
            return;
        }
        else name = nameView.getText().toString();

        // check if Person with that name already exists
        try {
            if(viewModel.personExists(name)) {
                nameViewLayout.setError(getString(R.string.error_message_name_exists, name));
                /*String errorMessage = getResources().getString(R.string.error_message_name_exists, name);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();*/
            }
            else {
                if(viewModel.getEditedPerson() == null) {
                    // insert new person (via viewModel) and finish activity
                    viewModel.addPerson(name);
                } else {
                    Person oldPerson = viewModel.getEditedPerson();
                    oldPerson.name = name;
                    viewModel.update(oldPerson);
                }
                NavHostFragment.findNavController(this).navigateUp();
            }
        } catch (ExecutionException | InterruptedException e) {
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Toast.makeText(getContext(),  errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    class NameTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            nameViewLayout.setError(null);
        }
    }
}