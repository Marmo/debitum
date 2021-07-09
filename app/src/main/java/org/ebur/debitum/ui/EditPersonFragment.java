package org.ebur.debitum.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialContainerTransform;

import org.ebur.debitum.ContactsHelper;
import org.ebur.debitum.R;
import org.ebur.debitum.Utilities;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.viewModel.EditPersonViewModel;
import org.ebur.debitum.viewModel.NewPersonRequestViewModel;

import java.util.concurrent.ExecutionException;

public class EditPersonFragment extends DialogFragment {
    // TODO use listeners on input fields to keep viewModel up to date
    //  and observers to keep input fields up to date. The viewModel
    //  needs to have single values (name, note, uri, image) of LiveData.
    //  Then upon saving the viewModel's data (that is always up to date)
    //  can be directly used.
    //  Use DataBinding https://developer.android.com/topic/libraries/data-binding/

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
        // TODO move contactsHelper to shared View model (scope: Activity or navgraph)
        contactsHelper = new ContactsHelper(requireContext());

        View root = inflater.inflate(R.layout.fragment_edit_person, container, false);

        toolbar = root.findViewById(R.id.dialog_toolbar);
        editNameLayout = root.findViewById(R.id.edit_person_name);
        editName = (TextInputEditText) editNameLayout.getEditText();
        assert editName != null;
        editName.addTextChangedListener(new TextInputLayoutErrorResetter(editNameLayout));
        TextInputLayout editNoteLayout = root.findViewById(R.id.edit_person_note);
        editNote = (TextInputEditText) editNoteLayout.getEditText();
        editContactLayout = root.findViewById(R.id.edit_person_linked_contact);
        editContact = (TextInputEditText) editContactLayout.getEditText();
        assert editContact != null;
        editContact.setOnClickListener(view -> getContact.launch(null));
        editContactLayout.setEndIconOnClickListener(view -> {
            handleChangedContactUri(null);
        });
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
        viewModel.isContactLinkingEnabled().observe(getViewLifecycleOwner(), enabled -> {
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

    void handleChangedContactUri(@Nullable Uri uri) {

        if (viewModel.isContactLinkingEnabled().getValue()) {
            LayerDrawable baseAvatarDrawable = (LayerDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.avatar, null);
            assert baseAvatarDrawable != null;
            @StringRes int hint;
            @Nullable String contactName;
            @Nullable String letter;

            if (uri == null) {
                contactName = null;
                hint = R.string.edit_person_hint_no_linked_contact;
            } else {
                contactName = contactsHelper.getContactName(uri);
                hint = R.string.edit_person_hint_linked_contact;

                // if editName is empty, fill it with name
                CharSequence name = editName.getText();
                if (name == null || name.toString().isEmpty()) {
                    editName.setText(contactName);
                    viewModel.getEditedPerson().name = contactName;
                }
            }

            @Nullable Bitmap photo = contactsHelper.getContactImage(uri);
            // this will yield either the photo (if uri != null and a photo is there) or a
            // generated color based on the person's color index
            @ColorInt int secondaryColorRGB = Utilities.getAttributeColor(requireContext(), R.attr.colorSecondary);
            LayerDrawable avatarDrawable = contactsHelper.makeAvatarDrawable(
                    baseAvatarDrawable,
                    photo,
                    viewModel.getEditedPerson().getColor(secondaryColorRGB)
            );
            // photo will be null if uri is null or there is no photo for the linked contact
            letter = photo == null
                            ? String.valueOf(viewModel.getEditedPerson().name.charAt(0)).toUpperCase()
                            : null;

            // fix tint in avatar_circle_mask.xml.xml not being respected
            avatarDrawable.findDrawableByLayerId(R.id.avatar_mask).setColorFilter(
                    Utilities.getAttributeColor(requireContext(), R.attr.colorSurface),
                    PorterDuff.Mode.SRC_ATOP
            );

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
                        isGranted -> viewModel.setContactLinkingEnabled(isGranted));

        // check and ask for permission
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            viewModel.setContactLinkingEnabled(true);
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }
}