package com.example.da_chuang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.example.da_chuang.game.view.GameView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class GameActivity extends AppCompatActivity {
    private static final int WHAT_REFRESH = 300;
    private double x;

    private GameView gameView;
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            super.handleMessage(msg);
            if (WHAT_REFRESH == msg.what) {
                boolean isFailed = gameView.refreshView(-1);    //FIXME:这里的-1以后会替换为x
                if (!isFailed) {//没有结束继续发线程
                    sendControlMessage();
                } else {// 结束告诉程序你输了并返回主界面
                    Intent intent = new Intent();
                    intent.setClass(GameActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.service.bleGet");
        super.onCreate(savedInstanceState);
        GameActivity.this.registerReceiver(new BroadcastReceiver() {
            /**
             * 接收蓝牙线程的广播<BR/>
             * 蓝牙广播了两个数据startPre为1则表示检测到了手势<BR/>
             * rawData是原始数据
             *
             * @param context
             * @param intent
             */
            @Override
            public void onReceive(Context context, Intent intent) {

                int startPre = intent.getIntExtra("startPre", 0);
//                double x = 0;
                File f =
                        new File(Objects.requireNonNull(getApplicationContext().getFilesDir().getParentFile()).getPath() +
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
            }
        }, filter);

        gameView = new GameView(this);
        gameView.setClickable(true);
        setContentView(gameView);
        sendControlMessage();
    }

    private void sendControlMessage() {
        // 每次刷新时间间隔
        int time = 300;
        handler.postDelayed(() -> handler.sendEmptyMessage(WHAT_REFRESH), time);
    }
}