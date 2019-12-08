package com.f0x1d.cripty.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.activity.MainActivity;
import com.f0x1d.cripty.activity.SettingsActivity;
import com.f0x1d.cripty.fragment.dialogs.FilePickerDialogFragment;
import com.f0x1d.cripty.service.CryptingService;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class MainFragment extends Fragment implements FilePickerDialogFragment.OnFilesSelectedListener {

    public final int ENCRYPT_CODE = 0;
    public final int DECRYPT_CODE = 1;

    public MaterialButton mEncryptButton;
    public MaterialButton mDecryptButton;
    public Toolbar mToolbar;

    public int mCurrentMode = -1;

    public CryptingService mCryptingService = null;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MainActivity getMainActivity() {
        return (MainActivity) requireActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, container, false);

        mEncryptButton = v.findViewById(R.id.encrypt);
        mDecryptButton = v.findViewById(R.id.decrypt);
        mToolbar = v.findViewById(R.id.toolbar);

        View.OnClickListener listener = view -> {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.root = Environment.getExternalStorageDirectory();
            properties.offset = new File(getMainActivity().getDefaultPreferences().getString("def_folder",
                    Environment.getExternalStorageDirectory().getAbsolutePath()));

            if (view.getId() == R.id.decrypt)
                mCurrentMode = DECRYPT_CODE;
            else if (view.getId() == R.id.encrypt)
                mCurrentMode = ENCRYPT_CODE;

            FilePickerDialogFragment filePickerDialogFragment = FilePickerDialogFragment.newInstance(null, getString(R.string.choose_file), properties);
            filePickerDialogFragment.setListener(MainFragment.this);
            filePickerDialogFragment.show(requireActivity().getSupportFragmentManager(), null);
        };

        mEncryptButton.setOnClickListener(listener);
        mDecryptButton.setOnClickListener(listener);

        mToolbar.setTitle(R.string.app_name);
        mToolbar.inflateMenu(R.menu.about);
        mToolbar.getMenu().findItem(R.id.about).setOnMenuItemClickListener(item -> {
            getMainActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.container, AboutAppFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
            return false;
        });
        mToolbar.getMenu().findItem(R.id.settings).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(getMainActivity(), SettingsActivity.class));
            return false;
        });

        return v;
    }

    @Override
    public void onFilesSelected(String tag, List<File> files) {
        File file = files.get(0);

        String defKey = getMainActivity().getDefaultPreferences().getString("defKey", "");

        if (!defKey.isEmpty()) {
            process(getMainActivity().getDefaultPreferences().getBoolean("b64_key", false) ? Base64.decode(defKey, Base64.DEFAULT) : defKey.getBytes(), file);
            return;
        }

        View v = LayoutInflater.from(getMainActivity()).inflate(R.layout.edit_text, null);
        final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
        editTextLayout.setHint(getString(R.string.key));
        EditText editText = v.findViewById(R.id.edittext);

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

                if (key.getBytes().length != 16 && key.getBytes().length != 24 && key.getBytes().length != 32) {
                    editText.setError(getString(R.string.invalid_key_length));
                } else {
                    editText.setError(null);
                }
            }
        });

        new MaterialAlertDialogBuilder(getMainActivity())
                .setTitle(R.string.choose_key)
                .setView(v)
                .setPositiveButton(R.string.ok, (dialog, which) -> process(editText.getText().toString().getBytes(), file))
                .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setNegativeButton(R.string.b64, (dialog, which) -> showBase64KeyDialog(file)).show();
    }

    public void showBase64KeyDialog(File file) {
        View v = LayoutInflater.from(getMainActivity()).inflate(R.layout.edit_text, null);
        final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
        editTextLayout.setHint(getString(R.string.key_b64));
        EditText editText = v.findViewById(R.id.edittext);

        new MaterialAlertDialogBuilder(getMainActivity())
                .setTitle(R.string.choose_key_b64)
                .setView(v)
                .setPositiveButton(R.string.ok, ((dialog, which) -> process(Base64.decode(editText.getText().toString(), Base64.DEFAULT), file)))
                .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.cancel()).show();
    }

    public void process(byte[] key, File file) {
        Intent intent = new Intent(getMainActivity(), CryptingService.class);
        intent.putExtra("file", file);
        intent.putExtra("key", key);
        intent.putExtra("mode", mCurrentMode);

        getMainActivity().startService(intent);
        getMainActivity().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MainFragment.this.mCryptingService = (CryptingService) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCryptingService = null;
            }
        }, BIND_AUTO_CREATE);
    }
}
