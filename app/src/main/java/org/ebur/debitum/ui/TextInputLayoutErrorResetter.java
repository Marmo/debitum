package org.ebur.debitum.ui;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

public class TextInputLayoutErrorResetter implements TextWatcher {

    TextInputLayout layout;

    public TextInputLayoutErrorResetter(TextInputLayout layout) {
        this.layout = layout;
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        layout.setError(null);
    }
}
