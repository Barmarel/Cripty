package com.f0x1d.cripty.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.view.CenteredToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

public class SettingsActivity extends PreferenceActivity {

    public CenteredToolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addPreferencesFromResource(R.xml.settings);

        Preference defKey = findPreference("defKey");
        defKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View v = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.edit_text, null);
                final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
                editTextLayout.setHint(getString(R.string.key));

                ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString("defKey", ""));

                ((EditText) v.findViewById(R.id.edittext)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        String key = s.toString();

                        if (key.length() != 16 && key.length() != 24 && key.length() != 32 && key.length() != 0){
                            ((EditText) v.findViewById(R.id.edittext)).setError(getString(R.string.invalid_key_length));
                        }
                    }
                });

                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle(R.string.choose_default_key)
                        .setMessage(R.string.set_empty_to_set_manually)
                        .setView(v)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String key = ((EditText) v.findViewById(R.id.edittext)).getText().toString();

                                if (key.length() != 16 && key.length() != 24 && key.length() != 32 && key.length() != 0){
                                    Snackbar.make(getListView(), R.string.invalid_key_length, Snackbar.LENGTH_SHORT).show();
                                    return;
                                }

                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                                        .putString("defKey", key).apply();
                                dialog.cancel();
                            }
                        }).show();
                return false;
            }
        });

        Preference enFileName = findPreference("enFileName");
        enFileName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View v = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.edit_text, null);
                final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
                editTextLayout.setHint(getString(R.string.file_name));

                ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString("enFileName", ""));

                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle(R.string.en_file_name)
                        .setMessage(R.string.set_empty_to_use_default)
                        .setView(v)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                                        .putString("enFileName", ((EditText) v.findViewById(R.id.edittext)).getText().toString()).apply();
                                dialog.cancel();
                            }
                        })
                        .show();
                return false;
            }
        });

        Preference deFileName = findPreference("deFileName");
        deFileName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View v = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.edit_text, null);
                final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
                editTextLayout.setHint(getString(R.string.file_name));

                ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString("deFileName", ""));

                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle(R.string.de_file_name)
                        .setMessage(R.string.set_empty_to_use_default)
                        .setView(v)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                                        .putString("deFileName", ((EditText) v.findViewById(R.id.edittext)).getText().toString()).apply();
                                dialog.cancel();
                            }
                        })
                        .show();
                return false;
            }
        });

        Preference deleteAll = findPreference("deleteAll");
        deleteAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();

                File appFolder = new File(Environment.getExternalStorageDirectory() + "/Cripty");
                if (!appFolder.exists()){
                    progressDialog.cancel();
                    return false;
                }

                if (appFolder.listFiles().length != 0){
                    for (File file : appFolder.listFiles()) {
                        file.delete();
                    }
                }
                appFolder.delete();

                progressDialog.cancel();
                return false;
            }
        });
    }
}
