package com.f0x1d.cripty.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.activity.MainActivity;
import com.f0x1d.cripty.fragment.dialogs.FilePickerDialogFragment;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat implements FilePickerDialogFragment.OnFilesSelectedListener {

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        Preference defKey = findPreference("defKey");
        defKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_text, null);
                final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
                editTextLayout.setHint(getString(R.string.key));

                ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("defKey", ""));

                ((EditText) v.findViewById(R.id.edittext)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String key = s.toString();

                        if (key.length() != 16 && key.length() != 24 && key.length() != 32 && key.length() != 0) {
                            ((EditText) v.findViewById(R.id.edittext)).setError(getString(R.string.invalid_key_length));
                        }
                    }
                });

                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(R.string.choose_default_key)
                        .setMessage(R.string.set_empty_to_set_manually)
                        .setView(v)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String key = ((EditText) v.findViewById(R.id.edittext)).getText().toString();

                                if (key.length() != 16 && key.length() != 24 && key.length() != 32 && key.length() != 0) {
                                    Snackbar.make(getListView(), R.string.invalid_key_length, Snackbar.LENGTH_SHORT).show();
                                    return;
                                }

                                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
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
                View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_text, null);
                final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
                editTextLayout.setHint(getString(R.string.file_name));

                ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("enFileName", ""));

                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(R.string.en_file_name)
                        .setMessage(R.string.set_empty_to_use_default)
                        .setView(v)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
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
                View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_text, null);
                final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
                editTextLayout.setHint(getString(R.string.file_name));

                ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("deFileName", ""));

                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(R.string.de_file_name)
                        .setMessage(R.string.set_empty_to_use_default)
                        .setView(v)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
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
                ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();

                File appFolder = new File(Environment.getExternalStorageDirectory() + "/Cripty");
                if (!appFolder.exists()) {
                    progressDialog.cancel();
                    return false;
                }

                if (appFolder.listFiles().length != 0) {
                    for (File file : appFolder.listFiles()) {
                        file.delete();
                    }
                }
                appFolder.delete();

                progressDialog.cancel();
                return false;
            }
        });

        SwitchPreference darkTheme = (SwitchPreference) findPreference("night");
        darkTheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().moveTaskToBack(true);
                startActivity(new Intent(getContext(), MainActivity.class));
                return false;
            }
        });

        Preference defFolder = findPreference("def_folder");
        defFolder.setSummary(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("def_folder", Environment.getExternalStorageDirectory().getAbsolutePath()));
        defFolder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.DIR_SELECT;
                properties.root = Environment.getExternalStorageDirectory();

                FilePickerDialogFragment filePickerDialogFragment = FilePickerDialogFragment.newInstance(null, getString(R.string.choose_file), properties);
                filePickerDialogFragment.setListener(SettingsFragment.this);
                filePickerDialogFragment.show(getActivity().getSupportFragmentManager(), null);
                return false;
            }
        });
    }

    @Override
    public void onFilesSelected(String tag, List<File> files) {
        File file = files.get(0);
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putString("def_folder", file.getAbsolutePath())
                .apply();

        findPreference("def_folder").setSummary(file.getAbsolutePath());
    }
}
