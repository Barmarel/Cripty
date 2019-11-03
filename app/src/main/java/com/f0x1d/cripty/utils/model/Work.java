package com.f0x1d.cripty.utils.model;

import java.io.File;

public class Work {

    public int notificationId;
    public int cryptType;
    public byte[] key;
    public File cryptingFile;

    public long lastNotificationUpdateTime;

    public Work(int notificationId, int cryptType, byte[] key, File cryptingFile, long lastNotificationUpdateTime) {
        this.notificationId = notificationId;
        this.cryptType = cryptType;
        this.key = key;
        this.cryptingFile = cryptingFile;
        this.lastNotificationUpdateTime = lastNotificationUpdateTime;
    }
}
