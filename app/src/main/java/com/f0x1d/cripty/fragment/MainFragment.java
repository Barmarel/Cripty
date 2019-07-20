package com.f0x1d.cripty.fragment;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.activity.MainActivity;
import com.f0x1d.cripty.fragment.dialogs.FilePickerDialogFragment;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class MainFragment extends Fragment implements FilePickerDialogFragment.OnFilesSelectedListener {

    public final int ENCRYPT_CODE = 0;
    public final int DECRYPT_CODE = 1;
    public MaterialButton encrypt;
    public MaterialButton decrypt;
    public Toolbar toolbar;

    public int currentMode = -1;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, container, false);

        encrypt = v.findViewById(R.id.encrypt);
        decrypt = v.findViewById(R.id.decrypt);
        toolbar = v.findViewById(R.id.toolbar);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = Environment.getExternalStorageDirectory();

                if (v.getId() == R.id.decrypt)
                    currentMode = DECRYPT_CODE;
                else if (v.getId() == R.id.encrypt)
                    currentMode = ENCRYPT_CODE;

                FilePickerDialogFragment filePickerDialogFragment = FilePickerDialogFragment.newInstance(null, getString(R.string.choose_file), properties);
                filePickerDialogFragment.setListener(MainFragment.this);
                filePickerDialogFragment.show(getActivity().getSupportFragmentManager(), null);
            }
        };

        encrypt.setOnClickListener(listener);
        decrypt.setOnClickListener(listener);

        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.about);
        toolbar.getMenu().findItem(R.id.about).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getMainActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.container, AboutAppFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                return false;
            }
        });

        return v;
    }

    public void processError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();

        new MaterialAlertDialogBuilder(getMainActivity())
                .setTitle("Error!")
                .setMessage(sStackTrace)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNeutralButton(R.string.copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager manager = (ClipboardManager) getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData data = ClipData.newPlainText("error", sStackTrace);
                        manager.setPrimaryClip(data);

                        Snackbar.make(getView(), R.string.copied, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .show();

        e.printStackTrace();
    }

    @Override
    public void onFilesSelected(String tag, List<File> files) {
        File file = files.get(0);

        View v = LayoutInflater.from(getMainActivity()).inflate(R.layout.edit_text, null);
        final TextInputLayout editTextLayout = v.findViewById(R.id.edittext_layout);
        editTextLayout.setHint(getString(R.string.key));

        new MaterialAlertDialogBuilder(getMainActivity())
                .setTitle(R.string.choose_key)
                .setView(v)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = new ProgressDialog(getMainActivity());
                        progressDialog.setMessage(getString(R.string.loading));
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        String key = ((EditText) v.findViewById(R.id.edittext)).getText().toString();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    File appFolder = new File(Environment.getExternalStorageDirectory() + "/Crypty");
                                    if (!appFolder.exists())
                                        appFolder.mkdirs();

                                    File cryptedFile = null;

                                    if (currentMode == ENCRYPT_CODE)
                                        cryptedFile = new File(appFolder, "encrypted_" + file.getName());
                                    else if (currentMode == DECRYPT_CODE)
                                        cryptedFile = new File(appFolder, "decrypted_" + file.getName());

                                    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
                                    Cipher cipher = Cipher.getInstance("AES");
                                    if (currentMode == ENCRYPT_CODE)
                                        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                                    else if (currentMode == DECRYPT_CODE)
                                        cipher.init(Cipher.DECRYPT_MODE, secretKey);

                                    FileInputStream inputStream = new FileInputStream(file);
                                    CipherOutputStream cipherOutputStream = new CipherOutputStream(new FileOutputStream(cryptedFile), cipher);

                                    byte[] buffer = new byte[4 * 1024];
                                    int len;
                                    while ((len = inputStream.read(buffer)) != -1) {
                                        cipherOutputStream.write(buffer, 0, len);
                                    }

                                    inputStream.close();
                                    cipherOutputStream.close();

                                    File finalCryptedFile = cryptedFile;
                                    getMainActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.cancel();

                                            Snackbar.make(getView(), getString(R.string.saved_to) + " " + finalCryptedFile.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    getMainActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.cancel();
                                            processError(e);
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }
}
