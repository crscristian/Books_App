package com.example.library;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

public class DeleteConfirmationDialog extends AlertDialog {

    public DeleteConfirmationDialog(Context context, String title, String message, String positiveButtonText, String negativeButtonText) {
        super(context);
        setTitle(title);
        setMessage(message);

        setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText, (dialog, which) -> {
            boolean doNotAskAgain = ((CheckBox) findViewById(R.id.checkbox)).isChecked();

            if (doNotAskAgain) {
                SharedPreferences preferences = context.getSharedPreferences("deletePreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("doNotAskAgain", true);
                editor.apply();
            }

        });

        setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText, (dialog, which) -> {
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_delete_confirmation, null);
        setView(view);
    }
}
