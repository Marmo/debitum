package org.ebur.debitum.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.ebur.debitum.R;

public class EditPersonActivity extends AppCompatActivity {

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

        newPerson = getIntent().getBooleanExtra(MainActivity.EXTRA_NEW_PERSON, false);
        if(newPerson) toolbar.setTitle(R.string.title_activity_edit_person_add);

        // observe LiveData of all persons
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
        Toast.makeText(getApplicationContext(), nameView.getText().toString() + " saved!", Toast.LENGTH_SHORT).show();
        // check if nameView has contents

        // check if Person with that name already exists

        // insert new person (via viewModel)
    }

    public void onDeletePersonAction(MenuItem item) {
    }
}