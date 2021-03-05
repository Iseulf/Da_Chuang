package com.example.da_chuang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.example.da_chuang.game.view.GameView;

public class GameActivity extends AppCompatActivity {
    private static final int WHAT_REFRESH = 300;

    private GameView gameView;
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            super.handleMessage(msg);
            if (WHAT_REFRESH == msg.what) {
                boolean isFailed = gameView.refreshView();
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
        super.onCreate(savedInstanceState);

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