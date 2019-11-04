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
import java.util.Base64;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat implements FilePickerDialogFragment.OnFilesSelectedListener {

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void showBase64KeyDialog(Preference defKey) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.edit_text, null);
        final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
        editTextLayout.setHint(getString(R.string.key_b64));
        EditText editText = v.findViewById(R.id.edittext);

        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("b64_key", false))
            editText.setText(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("defKey", ""));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_default_key_b64)
                .setMessage(R.string.set_empty_to_set_manually)
                .setView(v)
                .setPositiveButton(R.string.ok, ((dialog, which) -> {
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                            .putString("defKey", editText.getText().toString())
                            .putBoolean("b64_key", true).apply();
                    defKey.setSummary(editText.getText().toString());

                    dialog.cancel();
                })).show();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        Preference defKey = findPreference("defKey");
        defKey.setSummary(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("defKey", ""));
        defKey.setOnPreferenceClickListener(preference -> {
            View v = LayoutInflater.from(requireContext()).inflate(R.layout.edit_text, null);
            final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
            editTextLayout.setHint(getString(R.string.key));
            EditText editText = v.findViewById(R.id.edittext);

            if (!PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("b64_key", false))
                editText.setText(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("defKey", ""));

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String key = s.toString();

                    if (key.getBytes().length != 16 && key.getBytes().length != 24 && key.getBytes().length != 32 && key.getBytes().length != 0) {
                        editText.setError(getString(R.string.invalid_key_length));
                    } else {
                        editText.setError(null);
                    }
                }
            });

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.choose_default_key_aes)
                    .setMessage(R.string.set_empty_to_set_manually)
                    .setView(v)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        String key = editText.getText().toString();

                        if (key.length() != 16 && key.length() != 24 && key.length() != 32 && key.length() != 0) {
                            Snackbar.make(getListView(), R.string.invalid_key_length, Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                                .putString("defKey", key)
                                .putBoolean("b64_key", false).apply();
                        defKey.setSummary(key);

                        dialog.cancel();
                    })
                    .setNegativeButton(R.string.b64, ((dialog, which) -> showBase64KeyDialog(defKey))).show();
            return false;
        });

        Preference enFileName = findPreference("enFileName");
        enFileName.setOnPreferenceClickListener(preference -> {
            View v = LayoutInflater.from(requireContext()).inflate(R.layout.edit_text, null);
            final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
            editTextLayout.setHint(getString(R.string.file_name));

            ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("enFileName", ""));

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.en_file_name)
                    .setMessage(R.string.set_empty_to_use_default)
                    .setView(v)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                                .putString("enFileName", ((EditText) v.findViewById(R.id.edittext)).getText().toString()).apply();
                        dialog.cancel();
                    })
                    .show();
            return false;
        });

        Preference deFileName = findPreference("deFileName");
        deFileName.setOnPreferenceClickListener(preference -> {
            View v = LayoutInflater.from(requireContext()).inflate(R.layout.edit_text, null);
            final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
            editTextLayout.setHint(getString(R.string.file_name));

            ((EditText) v.findViewById(R.id.edittext)).setText(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("deFileName", ""));

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.de_file_name)
                    .setMessage(R.string.set_empty_to_use_default)
                    .setView(v)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                                .putString("deFileName", ((EditText) v.findViewById(R.id.edittext)).getText().toString()).apply();
                        dialog.cancel();
                    })
                    .show();
            return false;
        });

        Preference deleteAll = findPreference("deleteAll");
        deleteAll.setOnPreferenceClickListener(preference -> {
            ProgressDialog progressDialog = new ProgressDialog(requireContext());
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
        });

        SwitchPreference darkTheme = (SwitchPreference) findPreference("night");
        darkTheme.setOnPreferenceClickListener(preference -> {
            getActivity().moveTaskToBack(true);
            startActivity(new Intent(requireContext(), MainActivity.class));
            return false;
        });

        Preference defFolder = findPreference("def_folder");
        defFolder.setSummary(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("def_folder", Environment.getExternalStorageDirectory().getAbsolutePath()));
        defFolder.setOnPreferenceClickListener(preference -> {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.DIR_SELECT;
            properties.root = Environment.getExternalStorageDirectory();

            FilePickerDialogFragment filePickerDialogFragment = FilePickerDialogFragment.newInstance(null, getString(R.string.choose_file), properties);
            filePickerDialogFragment.setListener(SettingsFragment.this);
            filePickerDialogFragment.show(getActivity().getSupportFragmentManager(), null);
            return false;
        });
    }

    @Override
    public void onFilesSelected(String tag, List<File> files) {
        File file = files.get(0);
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putString("def_folder", file.getAbsolutePath())
                .apply();

        findPreference("def_folder").setSummary(file.getAbsolutePath());
    }
}
