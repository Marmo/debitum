package org.ebur.debitum.ui;

import android.os.Bundle;
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

import com.google.android.material.transition.MaterialContainerTransform;

import org.ebur.debitum.R;
import org.ebur.debitum.databinding.FragmentEditPersonBinding;
import org.ebur.debitum.util.ContactsHelper;
import org.ebur.debitum.viewModel.EditPersonViewModel;
import org.ebur.debitum.viewModel.NewPersonRequestViewModel;

import java.util.concurrent.ExecutionException;

public class EditPersonFragment extends DialogFragment {

    private final static String TAG = "EditPersonFragment";

    public static final String ARG_EDITED_PERSON = "editedPerson";
    public static final String ARG_NEW_PERSON_REQUESTED = "newPersonRequested";

    private EditPersonViewModel viewModel;

    FragmentEditPersonBinding binding;

    private Toolbar toolbar;

    ActivityResultLauncher<Void> getContact = registerForActivityResult(new ActivityResultContracts.PickContact(),
            uri -> {
                // uri will be null if the user cancels the contact-picking. In that case we
                // do not want to do anything (not even set the contactUri to null!)
                if (uri != null) {
                    viewModel.setContactUri(uri);
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

        binding = FragmentEditPersonBinding.inflate(inflater, container, false);
        binding.setViewmodel(new ViewModelProvider(this).get(EditPersonViewModel.class));
        binding.setLifecycleOwner(this);

        viewModel = binding.getViewmodel();

        toolbar = binding.toolbar.findViewById(R.id.dialog_toolbar);
        binding.linkedContact.setOnClickListener(view -> getContact.launch(null));
        binding.linkedContactLayout.setEndIconOnClickListener(view -> {
            viewModel.setContactUri(null);
        });

        viewModel.setEditedPerson(requireArguments().getParcelable(ARG_EDITED_PERSON));

        // check permission to enable/disable contact linking feature
        // IMPORTANT: we need to check permission _before_ getting the uri and
        // calling handleChangedContactUri(), as there we need to know about that permission
        checkReadContactsPermission();
        subscribeToViewModel();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.inflateMenu(R.menu.menu_edit_person);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        binding.name.requestFocus();
    }

    private void subscribeToViewModel() {
        ContactsHelper.isContactLinkingEnabled().observe(getViewLifecycleOwner(), enabled -> {
            binding.linkedContactLayout.setHelperTextEnabled(!enabled);
            binding.linkedContactLayout.setEnabled(enabled);
        });
        viewModel.isNewPerson().observe(getViewLifecycleOwner(), isNewPerson -> {
            toolbar.setTitle(
                    isNewPerson ? R.string.title_fragment_edit_person_create
                    : R.string.title_fragment_edit_person
            );
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
        try {
            viewModel.savePerson();
            // return the new name back to the calling fragment via NewPersonRequestViewModel
            // (currently only EditTransactionFragement uses this)
            // only set name if a new person was requested
            if(requireArguments().getBoolean(ARG_NEW_PERSON_REQUESTED)
                    && viewModel.isNewPerson().getValue()) {
                NavBackStackEntry requester = NavHostFragment.findNavController(this).getPreviousBackStackEntry();
                if (requester != null) {
                    NewPersonRequestViewModel requestViewModel =
                            new ViewModelProvider(requester).get(NewPersonRequestViewModel.class);
                    requestViewModel.setNewPersonName(viewModel.getName().getValue());
                }
            }

            NavHostFragment.findNavController(this).navigateUp();

        } catch (ExecutionException | InterruptedException e) {
            String errorMessage = getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Log.e(TAG, errorMessage);
        }
    }

    private void checkReadContactsPermission() {
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher, as an instance variable.
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> ContactsHelper.setContactLinkingEnabled(isGranted));

        // check and ask for permission
        ContactsHelper.checkReadContactsPermission(requestPermissionLauncher, requireContext());
    }
}