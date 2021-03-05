package com.example.da_chuang.gesture;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.IOException;

public class PredictService extends Service {
    public PredictService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //处理具体的逻辑
                InteractionSpec interactionSpec = new InteractionSpec();
                try {
                    interactionSpec.last_predict();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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