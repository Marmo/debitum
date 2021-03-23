package org.ebur.debitum.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.ebur.debitum.R;
import org.ebur.debitum.model.Transaction;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "org.ebur.debitum.transactionlistsql.REPLY";

    private EditText editNameView;
    private EditText editAmountView;
    private EditText editDescView;
    private EditText editTimestampView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        // TODO use toolbar button instead of separate save button
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        editNameView = findViewById(R.id.edit_name);
        editAmountView = findViewById(R.id.edit_amount);
        editDescView = findViewById(R.id.edit_description);
        editTimestampView = findViewById(R.id.edit_timestamp);

        final Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(editNameView.getText()) ||
                    TextUtils.isEmpty(editAmountView.getText()) ||
                    TextUtils.isEmpty(editDescView.getText()) ||
                    TextUtils.isEmpty(editTimestampView.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
            } else {
                Bundle extras = new Bundle();
                extras.putString("NAME", editNameView.getText().toString());
                // TODO handle different input possibilities, including not parseable ones
                extras.putInt("AMOUNT", Integer.parseInt(editAmountView.getText().toString()));
                extras.putString("DESC", editDescView.getText().toString());
                extras.putString("TIMESTAMP", editTimestampView.getText().toString());
                replyIntent.putExtras(extras);
                setResult(RESULT_OK, replyIntent);
            }
            finish();
        });

    }
}