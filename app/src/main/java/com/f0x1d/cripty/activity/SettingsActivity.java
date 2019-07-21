package com.f0x1d.cripty.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.view.CenteredToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends PreferenceActivity {

    public CenteredToolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);

        addPreferencesFromResource(R.xml.settings);

        Preference defKey = findPreference("defKey");
        defKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View v = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.edit_text, null);
                final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
                editTextLayout.setHint(getString(R.string.key));

                ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString("defKey", ""));

                new MaterialAlertDialogBuilder(SettingsActivity.this)
                        .setTitle(R.string.choose_default_key)
                        .setMessage(R.string.set_empty_to_set_manually)
                        .setView(v)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                                        .putString("defKey", ((EditText) v.findViewById(R.id.edittext)).getText().toString()).apply();
                                dialog.cancel();
                            }
                        })
                        .show();
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
    }
}
