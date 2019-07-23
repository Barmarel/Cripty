package com.f0x1d.cripty.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.activity.MainActivity;
import com.f0x1d.cripty.receiver.CopyTextReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class CriptingService extends Service {

    public NotificationManager notificationManager;

    public final int ENCRYPT_CODE = 0;
    public final int DECRYPT_CODE = 1;

    public Handler handler;

    private List<Integer> currentWorks = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        createChannel();

        int id = 0;
        if (!currentWorks.isEmpty()){
            while (currentWorks.contains(id)){
                id++;
            }
        }
        currentWorks.add(id);

        NotificationCompat.Builder foregroundBuilder = new NotificationCompat.Builder(getApplicationContext());
        foregroundBuilder.setContentTitle(getString(R.string.cripty_working));
        foregroundBuilder.setContentText(getString(R.string.please_wait));
        foregroundBuilder.setSmallIcon(R.drawable.ic_sync_black_24dp);
        foregroundBuilder.setChannelId(getPackageName() + ".process");

        startForeground(-1, foregroundBuilder.build());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.loading) + " " + ((File) intent.getExtras().get("file")).getName() + "...");
        builder.setSmallIcon(R.drawable.ic_sync_black_24dp);
        builder.setChannelId(getPackageName() + ".process");
        builder.setOngoing(true);

        notificationManager.notify(id, builder.build());
        startCripting(id, builder, (File) intent.getExtras().get("file"), intent.getExtras().getString("key"), intent.getExtras().getInt("mode"));

        return START_NOT_STICKY;
    }

    public void updateCounter(int id, NotificationCompat.Builder builder, int max, int count){
        createChannel();

        builder.setSmallIcon(R.drawable.ic_sync_black_24dp);
        builder.setChannelId(getPackageName() + ".process");
        builder.setProgress(max, count, false);
        builder.setOngoing(true);

        notificationManager.notify(id, builder.build());
    }

    private void createChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getPackageName() + ".process", getString(R.string.loading), NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startCripting(int id, NotificationCompat.Builder builder, File file, String key, int currentMode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File appFolder = new File(Environment.getExternalStorageDirectory() + "/Cripty");
                    if (!appFolder.exists())
                        appFolder.mkdirs();

                    File cryptedFile = null;

                    if (currentMode == ENCRYPT_CODE){
                        String defFileName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("enFileName", "");
                        String fileName = defFileName.isEmpty() ? "encrypted_" + file.getName() : defFileName;

                        cryptedFile = new File(appFolder, getNameForFile(fileName));
                    } else if (currentMode == DECRYPT_CODE){
                        String defFileName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("deFileName", "");
                        String fileName = defFileName.isEmpty() ? "decrypted_" + file.getName() : defFileName;

                        cryptedFile = new File(appFolder, getNameForFile(fileName));
                    }

                    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    if (currentMode == ENCRYPT_CODE)
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    else if (currentMode == DECRYPT_CODE)
                        cipher.init(Cipher.DECRYPT_MODE, secretKey);

                    FileInputStream inputStream = new FileInputStream(file);
                    CipherOutputStream cipherOutputStream = new CipherOutputStream(new FileOutputStream(cryptedFile), cipher);

                    long total = file.length();
                    int count = 0;
                    byte[] buffer = new byte[1024 * 1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        count += len;
                        cipherOutputStream.write(buffer, 0, len);
                        updateCounter(id, builder, (int) total, count);
                    }

                    inputStream.close();
                    cipherOutputStream.close();

                    File finalCryptedFile = cryptedFile;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.saved_to) + " " + finalCryptedFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        }
                    });

                    builder.setSmallIcon(R.drawable.ic_done_black_24dp);
                    builder.setContentText(getString(R.string.saved_to) + " " + cryptedFile.getAbsolutePath());
                    builder.setChannelId(getPackageName() + ".process");
                    builder.setContentTitle(getString(R.string.successfully));
                    builder.setOngoing(false);

                    notificationManager.notify(id, builder.build());
                    currentWorks.remove(id);

                    if (currentWorks.isEmpty())
                        stopForeground(true);
                } catch (Exception e) {
                    processError(e, builder, id);
                }
            }
        }).start();
    }

    public String getNameForFile(String fileName){
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
        for (int i = 0; true; i++){
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

    public void processError(Exception e, NotificationCompat.Builder builder, int id) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();

        createChannel();

        builder.setContentTitle("Error!");
        builder.setOngoing(false);
        builder.setContentText(sStackTrace);
        builder.setStyle(new NotificationCompat.BigTextStyle(builder).bigText(sStackTrace));
        builder.setSmallIcon(R.drawable.ic_warning_black_24dp);
        builder.setChannelId(getPackageName() + ".process");
        builder.addAction(new NotificationCompat.Action(0, getString(R.string.copy), PendingIntent.getBroadcast(
                getApplicationContext(), 1, new Intent(getApplicationContext(), CopyTextReceiver.class).putExtra("text", sStackTrace), 0)));

        notificationManager.notify(id, builder.build());

        currentWorks.remove(id);

        if (currentWorks.isEmpty())
            stopForeground(true);

        e.printStackTrace();
    }
}
