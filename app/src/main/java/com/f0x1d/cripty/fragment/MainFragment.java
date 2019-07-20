package com.f0x1d.cripty.fragment;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.activity.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainFragment extends Fragment {

    public final int ENCRYPT_CODE = 0;
    public final int DECRYPT_CODE = 1;
    public MaterialButton encrypt;
    public MaterialButton decrypt;
    public Toolbar toolbar;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
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
                if (v.getId() == R.id.decrypt)
                    openFile("*/*", DECRYPT_CODE, getActivity());
                else if (v.getId() == R.id.encrypt)
                    openFile("*/*", ENCRYPT_CODE, getActivity());
            }
        };

        encrypt.setOnClickListener(listener);
        decrypt.setOnClickListener(listener);

        toolbar.setTitle(R.string.app_name);

        return v;
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;

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

                        try {
                            File appFolder = new File(Environment.getExternalStorageDirectory() + "/Crypty");
                            if (!appFolder.exists())
                                appFolder.mkdirs();

                            InputStream realInputStream = getMainActivity().getContentResolver().openInputStream(data.getData());

                            File realFile = null;

                            try {
                                realFile = new File(getMainActivity().getCacheDir(), "cache");
                                OutputStream output = new FileOutputStream(realFile);
                                try {
                                    byte[] buffer = new byte[4 * 1024];
                                    int read;

                                    while ((read = realInputStream.read(buffer)) != -1) {
                                        output.write(buffer, 0, read);
                                    }

                                    output.flush();
                                } finally {
                                    output.close();
                                }
                            } finally {
                                realInputStream.close();
                            }

                            File cryptedFile = null;

                            if (requestCode == ENCRYPT_CODE)
                                cryptedFile = new File(appFolder, "encrypted_" + getFileName(data.getData()));
                            else if (requestCode == DECRYPT_CODE)
                                cryptedFile = new File(appFolder, "decrypted_" + getFileName(data.getData()));

                            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
                            Cipher cipher = Cipher.getInstance("AES");
                            if (requestCode == ENCRYPT_CODE)
                                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                            else if (requestCode == DECRYPT_CODE)
                                cipher.init(Cipher.DECRYPT_MODE, secretKey);

                            FileInputStream inputStream = new FileInputStream(realFile);
                            byte[] inputBytes = new byte[(int) realFile.length()];
                            inputStream.read(inputBytes);

                            byte[] outputBytes = cipher.doFinal(inputBytes);

                            FileOutputStream outputStream = new FileOutputStream(cryptedFile);
                            outputStream.write(outputBytes);

                            inputStream.close();
                            outputStream.close();

                            progressDialog.cancel();

                            Snackbar.make(getView(), getString(R.string.saved_to) + " " + cryptedFile.getAbsolutePath(), Snackbar.LENGTH_LONG).show();
                        } catch (Exception e) {
                            progressDialog.cancel();
                            processError(e);
                        }
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
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

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void openFile(String minmeType, int requestCode, Context c) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.f0x1d.notes.main.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (c.getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }
}
