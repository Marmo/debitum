package org.ebur.debitum.ui;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialContainerTransform;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.util.ColorUtils;
import org.ebur.debitum.viewModel.ContactsHelper;
import org.ebur.debitum.viewModel.EditPersonViewModel;
import org.ebur.debitum.viewModel.NewPersonRequestViewModel;

import java.util.concurrent.ExecutionException;

public class EditPersonFragment extends DialogFragment {

    private final static String TAG = "EditPersonFragment";

    public static final String ARG_EDITED_PERSON = "editedPerson";
    public static final String ARG_NEW_PERSON_REQUESTED = "newPersonRequested";

    private EditPersonViewModel viewModel;
    private ContactsHelper contactsHelper;

    private Toolbar toolbar;
    private TextInputLayout editNameLayout;
    private TextInputEditText editName;
    private TextInputEditText editNote;
    private TextInputLayout editContactLayout;
    private TextInputEditText editContact;
    private ImageView avatarView;
    private TextView avatarLetterView;

    ActivityResultLauncher<Void> getContact = registerForActivityResult(new ActivityResultContracts.PickContact(),
            uri -> {
                // uri will be null if the user cancels the contact-picking. In that case we
                // do not want to do anything
                if (uri != null) {
                    handleChangedContactUri(uri);
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
        contactsHelper = new ViewModelProvider(requireActivity()).get(ContactsHelper.class);

        View root = inflater.inflate(R.layout.fragment_edit_person, container, false);

        toolbar = root.findViewById(R.id.dialog_toolbar);

        // Name
        editNameLayout = root.findViewById(R.id.edit_person_name);
        editName = (TextInputEditText) editNameLayout.getEditText();
        assert editName != null;
        editName.addTextChangedListener(new TextInputLayoutErrorResetter(editNameLayout));
        editName.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                viewModel.getEditedPerson().name = editName.getText() == null ? "" : editName.getText().toString();
                viewModel.getEditedPerson().calcuateColorIndex();
                // trigger avatar refresh
                handleChangedContactUri(viewModel.getEditedPerson().linkedContactUri);
            }
        });

        // Note
        TextInputLayout editNoteLayout = root.findViewById(R.id.edit_person_note);
        editNote = (TextInputEditText) editNoteLayout.getEditText();

        // Contact
        editContactLayout = root.findViewById(R.id.edit_person_linked_contact);
        editContact = (TextInputEditText) editContactLayout.getEditText();
        assert editContact != null;
        editContact.setOnClickListener(view -> getContact.launch(null));
        editContactLayout.setEndIconOnClickListener(view -> handleChangedContactUri(null));
        avatarView = root.findViewById(R.id.edit_person_avatar);
        avatarLetterView = root.findViewById(R.id.edit_person_avatar_text);

        Person editedPerson = requireArguments().getParcelable(ARG_EDITED_PERSON);
        viewModel.setEditedPerson(editedPerson != null ? editedPerson : new Person(-1));

        subscribeToViewModel();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.inflateMenu(R.menu.menu_edit_person);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        // check permission to enable/disable contact linking feature
        // IMPORTANT: we need to check permission _before_ getting the uri and
        // calling handleChangedContactUri(), as there we need to know about that permission
        checkReadContactsPermission();

        // we create a new person
        if(viewModel.isNewPerson()) {
            toolbar.setTitle(R.string.title_fragment_edit_person_create);
        }
        // we edit a person
        else {
            toolbar.setTitle(R.string.title_fragment_edit_person);
            editName.setText(viewModel.getEditedPerson().name);
            editNote.setText(viewModel.getEditedPerson().note);
            Uri uri = viewModel.getEditedPerson().linkedContactUri;
            handleChangedContactUri(uri);
        }

        editName.requestFocus();
    }

    private void subscribeToViewModel() {
        contactsHelper.isContactLinkingEnabled().observe(getViewLifecycleOwner(), enabled -> {
            editContactLayout.setHelperTextEnabled(!enabled);
            editContactLayout.setEnabled(enabled);
        });
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
            if(!editedPerson.name.equals(viewModel.getOriginalName())
                    && viewModel.personExists(editedPerson.name)) {
                editNameLayout.setError(getString(R.string.error_message_name_exists, editedPerson.name));
                return;
            } else {
                viewModel.writePersonToDb();
            }

            // return the new name back to the calling fragment via NewPersonRequestViewModel
            // (currently only EditTransactionFragment uses this)
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

    void handleChangedContactUri(@Nullable Uri uri) {

        if (Boolean.TRUE.equals(contactsHelper.isContactLinkingEnabled().getValue())) {
            @StringRes int hint;
            @Nullable String contactName;
            @Nullable String letter;
            @NonNull Drawable avatarDrawable;

            if (uri == null) {
                contactName = null;
                hint = R.string.edit_person_hint_no_linked_contact;
            } else {
                contactName = contactsHelper.getContactName(uri);
                hint = R.string.edit_person_hint_linked_contact;

                if (contactName == null) {
                    // contact does not exist (anymore)
                    handleChangedContactUri(null);
                    Snackbar.make(requireView(), R.string.edit_person_contact_deleted, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.dialog_gotit, view -> {})
                            .show();
                    return;
                }

                // if editName is empty, fill it with name
                CharSequence name = editName.getText();
                if (name == null || name.toString().isEmpty()) {
                    editName.setText(contactName);
                    viewModel.getEditedPerson().name = contactName;
                }
            }

            // this will yield either the photo (if uri != null and a photo is there) or a
            // generated color based on the person's color index
            @ColorInt int secondaryColorRGB = ColorUtils.getAttributeColor(requireContext(), R.attr.colorSecondary);
            avatarDrawable = contactsHelper.makeAvatarDrawable(
                    contactsHelper.getContactImage(uri),
                    viewModel.getEditedPerson().getColor(secondaryColorRGB)
            );
            String name = viewModel.getEditedPerson().name;
            letter = avatarDrawable instanceof RoundedBitmapDrawable || name.isEmpty()
                            ? null
                            : String.valueOf(name.charAt(0)).toUpperCase();

            editContactLayout.setHint(hint);
            editContact.setText(contactName);
            avatarView.setImageDrawable(avatarDrawable);
            avatarLetterView.setText(letter);
        }
        viewModel.setLinkedContactUri(uri);
    }

    private void checkReadContactsPermission() {
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher, as an instance variable.
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> contactsHelper.setContactLinkingEnabled(isGranted));

        // check and ask for permission
        contactsHelper.checkReadContactsPermission(requestPermissionLauncher);
    }
}