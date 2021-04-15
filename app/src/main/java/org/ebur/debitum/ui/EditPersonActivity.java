package org.ebur.debitum.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.ebur.debitum.R;
import org.ebur.debitum.database.Person;
import org.ebur.debitum.viewModel.EditPersonViewModel;

import java.util.concurrent.ExecutionException;

import static org.ebur.debitum.ui.PersonSumListFragment.EXTRA_EDITED_PERSON;

public class EditPersonActivity extends AppCompatActivity {

    private EditPersonViewModel viewModel;

    private EditText nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person);
        nameView = findViewById(R.id.edit_person_name);

        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(this).get(EditPersonViewModel.class);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // pre-populate name view if a Person is edited
        Person editedPerson = getIntent().getParcelableExtra(EXTRA_EDITED_PERSON);
        viewModel.setEditedPerson(editedPerson);

        if(editedPerson != null) nameView.setText(editedPerson.name);
        else getSupportActionBar().setTitle(R.string.title_activity_edit_person_add);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_person, menu);

        //remove delete menu item when creating new person
        if(viewModel.isNewPerson()) menu.removeItem(R.id.miDeletePerson);

        return true;
    }

    public void onSavePersonAction(MenuItem item) {
        String name;

        // check if nameView has contents
        if(TextUtils.isEmpty(nameView.getText())) {
            String errorMessage = getResources().getString(R.string.error_message_enter_name);
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            return;
        }
        else name = nameView.getText().toString();

        // check if Person with that name already exists
        try {
            if(viewModel.personExists(name)) {
                String errorMessage = getResources().getString(R.string.error_message_name_exists, name);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
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
                finish();
            }
        } catch (ExecutionException | InterruptedException e) {
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Toast.makeText(getApplicationContext(),  errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    public void onDeletePersonAction(MenuItem item) {
        // TODO ask for confirmation
        viewModel.delete(viewModel.getEditedPerson());
        // TODO snackbar confirming deletion
    }
}