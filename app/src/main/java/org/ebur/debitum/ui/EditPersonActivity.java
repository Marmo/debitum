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
import org.ebur.debitum.viewModel.EditPersonViewModel;

import java.util.concurrent.ExecutionException;

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

        viewModel.setNewPerson(getIntent().getBooleanExtra(MainActivity.EXTRA_NEW_PERSON, false));
        if(viewModel.isNewPerson()) getSupportActionBar().setTitle(R.string.title_activity_edit_person_add);
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
                // insert new person (via viewModel) and finish activity
                viewModel.addPerson(name);
                finish();
            }
        } catch (ExecutionException | InterruptedException e) {
            String errorMessage = getResources().getString(R.string.error_message_database_access, e.getLocalizedMessage());
            Toast.makeText(getApplicationContext(),  errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    public void onDeletePersonAction(MenuItem item) {
    }
}