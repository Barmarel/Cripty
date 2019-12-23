package com.f0x1d.cripty.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.receiver.CopyTextReceiver;
import com.f0x1d.cripty.utils.model.Work;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class CryptingService extends Service {

    public final int ENCRYPT_CODE = 0;
    public final int DECRYPT_CODE = 1;
    public NotificationManager mNotificationManager;
    public Handler mHandler;

    private List<Work> mCurrentWorks = new ArrayList<>();
    private Executor mExecutor = Executors.newCachedThreadPool();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler = new Handler();

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        File file = (File) intent.getExtras().get("file");
        int mode = intent.getExtras().getInt("mode");
        byte[] key = intent.getExtras().getByteArray("key");

        int id = 0;
        for (int i = 0; i < mCurrentWorks.size(); i++) {
            Work work = mCurrentWorks.get(i);
            if (work.notificationId == id)
                id++;
            else
                break;
        }

        Work newWork = new Work(id, mode, key, file, System.currentTimeMillis());
        mCurrentWorks.add(newWork);

        createChannel(true);

        NotificationCompat.Builder foregroundBuilder = new NotificationCompat.Builder(getApplicationContext());
        foregroundBuilder.setContentTitle(getString(R.string.cripty_working));
        foregroundBuilder.setContentText(getString(R.string.please_wait));
        foregroundBuilder.setSmallIcon(R.drawable.ic_sync_black_24dp);
        foregroundBuilder.setChannelId(getPackageName() + ".process_foreground");

        startForeground(-1, foregroundBuilder.build());

        createChannel(false);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.loading) + " " + file.getName() + "...");
        builder.setSmallIcon(R.drawable.ic_sync_black_24dp);
        builder.setChannelId(getPackageName() + ".process");
        builder.setOngoing(true);

        mNotificationManager.notify(id, builder.build());
        startCrypting(builder, newWork);

        return START_NOT_STICKY;
    }

    public void updateCounter(Work work, NotificationCompat.Builder builder, int max, int count) {
        if (work.lastNotificationUpdateTime < System.currentTimeMillis() - 1000) {
            createChannel(false);

            builder.setSmallIcon(R.drawable.ic_sync_black_24dp);
            builder.setChannelId(getPackageName() + ".process");
            builder.setProgress(max, count, false);
            builder.setOngoing(true);

            mNotificationManager.notify(work.notificationId, builder.build());

            work.lastNotificationUpdateTime = System.currentTimeMillis();
        }
    }

    private void createChannel(boolean foreground) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(foreground ? getPackageName() + ".process_foreground" : getPackageName() + ".process",
                    foreground ? getString(R.string.loading_foreground) : getString(R.string.loading), NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public void startCrypting(NotificationCompat.Builder builder, Work work) {
        new CryptingTask(work, builder).executeOnExecutor(mExecutor);
    }

    public String getNameForFile(String fileName) {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("overwrite", true))
            return fileName;

        File appFolder = new File(Environment.getExternalStorageDirectory() + "/Cripty");

        String extension = "";
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos > 0) {
            extension = fileName.substring(dotPos);
            fileName = fileName.substring(0, dotPos);
        }

        String newFileName = fileName;
        for (int i = 0; true; i++) {
            if (i == 0) {
                File file = new File(appFolder, newFileName + extension);
                if (!file.exists())
                    return newFileName + extension;
            } else {
                newFileName = fileName + i;
                File file = new File(appFolder, newFileName + extension);
                if (!file.exists())
                    return newFileName + extension;
            }
        }
    }

    public void processError(Exception e, NotificationCompat.Builder builder, Work work) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();

        if (sStackTrace.contains("Key length not 128/192/256 bits."))
            sStackTrace = getString(R.string.invalid_key_length);

        createChannel(false);

        builder.setContentTitle(getString(R.string.error) + "!");
        builder.setOngoing(false);
        builder.setContentText(sStackTrace);
        builder.setStyle(new NotificationCompat.BigTextStyle(builder).bigText(sStackTrace));
        builder.setSmallIcon(R.drawable.ic_warning_black_24dp);
        builder.setChannelId(getPackageName() + ".process");
        builder.addAction(new NotificationCompat.Action(0, getString(R.string.copy), PendingIntent.getBroadcast(
                getApplicationContext(), 1, new Intent(getApplicationContext(), CopyTextReceiver.class).putExtra("text", sStackTrace), 0)));

        mNotificationManager.notify(work.notificationId, builder.build());

        e.printStackTrace();

        mCurrentWorks.remove(work);
        if (mCurrentWorks.isEmpty())
            stopForeground();
    }

    private void stopForeground() {
        stopForeground(true);
        NotificationManagerCompat.from(this).cancel(-1);
    }

    public class CryptingTask extends AsyncTask<Void, Void, Void> {

        public Work work;
        public NotificationCompat.Builder builder;

        public CryptingTask(Work work, NotificationCompat.Builder builder) {
            this.work = work;
            this.builder = builder;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                File appFolder = new File(Environment.getExternalStorageDirectory() + "/Cripty");
                if (!appFolder.exists())
                    appFolder.mkdirs();

                File cryptedFile = null;

                if (work.cryptType == ENCRYPT_CODE) {
                    String defFileName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("enFileName", "");
                    String fileName = defFileName.isEmpty() ? "encrypted_" + work.cryptingFile.getName() : defFileName;

                    cryptedFile = new File(appFolder, getNameForFile(fileName));
                } else if (work.cryptType == DECRYPT_CODE) {
                    String defFileName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("deFileName", "");
                    String fileName = defFileName.isEmpty() ? "decrypted_" + work.cryptingFile.getName() : defFileName;

                    cryptedFile = new File(appFolder, getNameForFile(fileName));
                }

                SecretKeySpec secretKey = new SecretKeySpec(work.key, "AES");
                Cipher cipher = Cipher.getInstance("AES");
                if (work.cryptType == ENCRYPT_CODE)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                else if (work.cryptType == DECRYPT_CODE)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);

                FileInputStream inputStream = new FileInputStream(work.cryptingFile);
                CipherOutputStream cipherOutputStream = new CipherOutputStream(new FileOutputStream(cryptedFile), cipher);

                long total = work.cryptingFile.length();
                int count = 0;
                byte[] buffer = new byte[1024 * 1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    count += len;
                    cipherOutputStream.write(buffer, 0, len);
                    updateCounter(work, builder, (int) total, count);
                }

                inputStream.close();
                cipherOutputStream.close();

                File finalCryptedFile = cryptedFile;
                mHandler.post(() -> Toast.makeText(getApplicationContext(), getString(R.string.saved_to) + " " + finalCryptedFile.getAbsolutePath(), Toast.LENGTH_LONG).show());

                builder.setSmallIcon(R.drawable.ic_done_black_24dp);
                builder.setContentText(getString(R.string.saved_to) + " " + cryptedFile.getAbsolutePath());
                builder.setChannelId(getPackageName() + ".process");
                builder.setContentTitle(getString(R.string.successfully));
                builder.setOngoing(false);

                mNotificationManager.notify(work.notificationId, builder.build());
                mCurrentWorks.remove(work);

                if (mCurrentWorks.isEmpty())
                    stopForeground();
            } catch (Exception e) {
                processError(e, builder, work);
            }
            return null;
        }
    }
}
