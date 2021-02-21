package com.example.da_chuang;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

public class TrainService extends Service {
    public TrainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InteractionSpec interactionSpec = new InteractionSpec();
                try {
                    interactionSpec.last_train();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //处理具体的逻辑
                stopSelf();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}