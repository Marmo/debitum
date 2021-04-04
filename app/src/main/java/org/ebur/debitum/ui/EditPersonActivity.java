package org.ebur.debitum.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import org.ebur.debitum.viewModel.AddTransactionViewModel;
import org.ebur.debitum.viewModel.EditPersonViewModel;

import java.util.concurrent.ExecutionException;

public class EditPersonActivity extends AppCompatActivity {

    private EditPersonViewModel viewModel;

    private EditText nameView;
    private Toolbar toolbar;

    // TODO put this to viewModel?
    private boolean newPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person);

        nameView = (EditText) findViewById(R.id.edit_person_name);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPerson = getIntent().getBooleanExtra(MainActivity.EXTRA_NEW_PERSON, false);
        if(newPerson) toolbar.setTitle(R.string.title_activity_edit_person_add);

        // observe ViewModel's LiveData
        viewModel = new ViewModelProvider(this).get(EditPersonViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_person, menu);

        //remove delete menu item when creating new person
        if(newPerson) menu.removeItem(R.id.miDeletePerson);

        return true;
    }

    public void onSavePersonAction(MenuItem item) {
        String name;

        // check if nameView has contents
        if(TextUtils.isEmpty(nameView.getText())) {
            // TODO extract string resource
            Toast.makeText(getApplicationContext(), "Please enter a name!", Toast.LENGTH_SHORT).show();
            return;
        }
        else name = nameView.getText().toString();

        // check if Person with that name already exists
        try {
            if(viewModel.personExists(name)) {
                // TODO extract string resource
                Toast.makeText(getApplicationContext(), name + " already exists, please enter a different name!", Toast.LENGTH_SHORT).show();
            }
            else {
                // insert new person (via viewModel) and finish activity
                viewModel.addPerson(name);
                finish();
            }
        } catch (ExecutionException | InterruptedException e) {
            // TODO extract string resource
            Toast.makeText(getApplicationContext(),  "Error trying to access database, please try again!\n" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onDeletePersonAction(MenuItem item) {
    }
}