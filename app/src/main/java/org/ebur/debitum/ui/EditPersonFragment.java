package org.ebur.debitum.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialContainerTransform;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.viewModel.EditPersonViewModel;
import org.ebur.debitum.viewModel.NewPersonRequestViewModel;

import java.util.concurrent.ExecutionException;

public class EditPersonFragment extends DialogFragment {

    private final static String TAG = "EditPersonFragment";

    public static final String ARG_EDITED_PERSON = "editedPerson";
    public static final String ARG_NEW_PERSON_REQUESTED = "newPersonRequested";

    private EditPersonViewModel viewModel;

    private Toolbar toolbar;
    private TextInputLayout editNameLayout;
    private TextInputEditText editName;
    private TextInputEditText editNote;
    private TextInputEditText editContact;

    ActivityResultLauncher<Void> getContact = registerForActivityResult(new ActivityResultContracts.PickContact(),
            uri -> {
                // uri will be null if the user cancels the contact-picking. In that case we
                // do not want to do anything
                if (uri != null) {
                    // TODO: get contact
                    // if editName is empty, fill it with name
                    CharSequence name = editName.getText();
                    if (name == null || name.toString().isEmpty()) {
                        editName.setText(uri.getLastPathSegment());
                    }
                    editContact.setText(uri.toString());
                    viewModel.setLinkedContactUri(uri);
                }
            });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Debitum_FullScreenDialog);
        setSharedElementEnterTransition(new MaterialContainerTransform());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EditPersonViewModel.class);

        View root = inflater.inflate(R.layout.fragment_edit_person, container, false);

        toolbar = root.findViewById(R.id.dialog_toolbar);
        editNameLayout = root.findViewById(R.id.edit_person_name);
        editName = (TextInputEditText) editNameLayout.getEditText();
        assert editName != null;
        editName.addTextChangedListener(new TextInputLayoutErrorResetter(editNameLayout));
        TextInputLayout editNoteLayout = root.findViewById(R.id.edit_person_note);
        editNote = (TextInputEditText) editNoteLayout.getEditText();
        TextInputLayout editContactLayout = root.findViewById(R.id.edit_person_linked_contact);
        editContact = (TextInputEditText) editContactLayout.getEditText();
        assert editContact != null;
        editContact.setOnClickListener(view -> getContact.launch(null));
        editContactLayout.setEndIconOnClickListener(view -> {
            editContact.setText(null);
            viewModel.setLinkedContactUri(null);
        });

        Person editedPerson = requireArguments().getParcelable(ARG_EDITED_PERSON);
        viewModel.setEditedPerson(editedPerson != null ? editedPerson : new Person(-1));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.inflateMenu(R.menu.menu_edit_person);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        // we create a new person
        if(viewModel.isNewPerson()) {
            toolbar.setTitle(R.string.title_fragment_edit_person_create);
        }
        // we edit a person
        else {
            editName.setText(viewModel.getEditedPerson().name);
            editNote.setText(viewModel.getEditedPerson().note);
            toolbar.setTitle(R.string.title_fragment_edit_person);
            Uri uri = viewModel.getEditedPerson().linkedContactUri;
            editContact.setText(uri == null ? null : uri.toString());
        }

        editName.requestFocus();
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
        Person editedPerson = viewModel.getEditedPerson();

        // check if nameView has contents and get name
        if(editName.getText() == null || TextUtils.isEmpty(editName.getText())) {
            editNameLayout.setError(getString(R.string.error_message_enter_name));
            return;
        } else {
            editedPerson.name = editName.getText().toString();
        }

        // get note
        assert editNote.getText() != null;
        editedPerson.note = editNote.getText().toString();

        try {
            if(viewModel.personExists(editedPerson.name)) {
                editNameLayout.setError(getString(R.string.error_message_name_exists, editedPerson.name));
                return;
            } else {
                viewModel.writePersonToDb();
            }

            // return the new name back to the calling fragment via NewPersonRequestViewModel
            // (currently only EditTransactionFragement uses this)
            // only set name if a new person was requested
            if(requireArguments().getBoolean(ARG_NEW_PERSON_REQUESTED)
                    && viewModel.isNewPerson()) {
                NavBackStackEntry requester = NavHostFragment.findNavController(this).getPreviousBackStackEntry();
                if (requester != null) {
                    NewPersonRequestViewModel requestViewModel =
                            new ViewModelProvider(requester).get(NewPersonRequestViewModel.class);
                    requestViewModel.setNewPersonName(editedPerson.name);
                }
            }

            NavHostFragment.findNavController(this).navigateUp();

        } catch (ExecutionException | InterruptedException e) {
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Log.e(TAG, errorMessage);
        }
    }
}