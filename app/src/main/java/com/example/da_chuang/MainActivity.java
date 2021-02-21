package com.example.da_chuang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private Intent intentBle;
    private Intent preDeal;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InteractionSpec interactionSpec = new InteractionSpec();

        Button train_button = findViewById(R.id.button_train);
        Button predict_button = findViewById(R.id.button_predict);
        Button startBle_button = findViewById(R.id.button_startble);
        Button stopBle_button = findViewById(R.id.button_stopble);
        intentBle = new Intent(this, BleGetService.class);
        preDeal = new Intent(this, PreDealService.class);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.service.bleGet");
        MainActivity.this.registerReceiver(new BroadcastReceiver() {
            /**
             * 接收蓝牙线程的广播<BR/>
             * 蓝牙广播了两个数据startPre为1则表示检测到了手势<BR/>
             * rawData是原始数据
             * @param context
             * @param intent
             */
            @Override
            public void onReceive(Context context, Intent intent) {
//                long ttt=System.currentTimeMillis();
//                Log.w("Time",ttt+"ms");
                int startPre = intent.getIntExtra("startPre", 0);
                if (startPre == 1) {
                    long ttt = System.currentTimeMillis();
                    Log.w("Time", ttt + "ms");
                    String rawData = intent.getStringExtra("rawData");
                    File rawFile =
                            new File(Objects.requireNonNull(getApplication().getFilesDir().getParentFile()).getPath() +
                                    "/data/rawData.txt");
                    try {
                        FileWriter rawFileWriter = new FileWriter(rawFile);
                        rawFileWriter.write(rawData);
                        rawFileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startService(preDeal);
                }
            }
        }, filter);

        train_button.setOnClickListener(v -> {
            try {
                interactionSpec.last_train();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "Train over", Toast.LENGTH_LONG).show();
        });

        predict_button.setOnClickListener(v -> {
            long startTime = System.currentTimeMillis();
            //起始时间
            try {
                interactionSpec.last_predict();
            } catch (IOException e) {
                e.printStackTrace();
            }
            double x = 0;
            File f = new File(getApplicationContext().getFilesDir().getParentFile().getPath() +
                    "/data/result.txt");
            try {
                FileReader fileReader = new FileReader(f);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                x = Double.parseDouble(bufferedReader.readLine());
                fileReader.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis(); //结束时间
            long runTime = endTime - startTime;
            Toast.makeText(getApplicationContext(),
                    "Predict over!Result is " + x + ".takes " + runTime + "ms",
                    Toast.LENGTH_LONG).show();
        });

        startBle_button.setOnClickListener(v -> startService(intentBle));

        stopBle_button.setOnClickListener(v -> stopService(intentBle));
    }

    @Override
    protected void onDestroy() {
        stopService(intentBle);
        stopService(preDeal);
        super.onDestroy();
    }
}