package com.example.da_chuang.gesture;
/**
 * 接收蓝牙线程传过来的粗略数据，进行下一步的刷选和预处理
 * 这个线程执行完之后，如果确实存在手势会将预处理后的归一化数据写入rawTestFile中，再开启预测
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class PreDealService extends Service {
    private final int PORT_NUM = 18;//数据端口个数
    private final int RedPortNum = 4;
    private final String rawDataFileName =
            Objects.requireNonNull(MyApplication.getContext().getFilesDir().getParentFile()).getPath() + "/data/rawData" +
                    ".txt";
    //存放原始数据的文件
    private final int WindowLength = 128;//原始数据列数


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            //TODO:预处理逻辑算法
            double[][] rawData = new double[RedPortNum][128];
            getGesture(rawData, 1);
            /*对获得的rawData进行预处理*/
            File rawTestFile =
                    new File(Objects.requireNonNull(MyApplication.getContext().getFilesDir().getParentFile()).getPath()
                            + "/data/testyuan.txt");
            try {
                FileWriter rawTestFileWriter = new FileWriter(rawTestFile);
                rawTestFileWriter.write(getGesture(rawData));
                rawTestFileWriter.close();
                InteractionSpec interactionSpec = new InteractionSpec();
                interactionSpec.last_predict();
                Log.e("finished", "predict over!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            stopSelf();
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 将gestureData转换为格式正确的写入rawTestFile的字符串
     *
     * @param gestureData
     * @return
     */
    public String getGesture(double[][] gestureData) {
        StringBuilder gestureDataString = new StringBuilder();
        for (int i = 0; i < RedPortNum; i++) {
            for (int j = 0; j < 128; j++) {
                gestureDataString.append(gestureData[i][j]).append(" ");
            }
            gestureDataString.append("\n");
        }
        gestureDataString.append("#");
        return gestureDataString.toString();
    }

    /**
     * 获取手势相关数据段gestureData
     *
     * @param gestureData
     * @param f
     */
    public void getGesture(double[][] gestureData, int f) {
        double[][] rawData = new double[PORT_NUM][WindowLength];
        getRawData(rawData);
        //手势相关数据段列数
        int gestureCol = 128;
        // double[][] gestureData = new double[4][gestureCol];
        for (int i = 0; i < RedPortNum; i++) {
            // int frontLength = 60;
            System.arraycopy(rawData[i], 0, gestureData[i], 0, gestureCol);
        }
    }

    /**
     * 一次性读取fileName里的所有内容
     *
     * @param fileName
     * @return
     */
    public String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        long filelength = file.length();
        byte[] filecontent = new byte[(int) filelength];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取原始数据到rawData中
     *
     * @param rawData
     */
    private void getRawData(double[][] rawData) {
        String raw_in = readToString(rawDataFileName);
        String[] y = raw_in.split("\r\n");
        for (int i = 0; i < WindowLength; i++) {
            String[] raw_data = y[i].split(",");
            for (int j = 0; j < PORT_NUM; j++) {
                rawData[j][i] = Double.parseDouble(raw_data[j]);
            }
        }
    }
}