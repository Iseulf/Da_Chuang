/**
 * 用于处理经预处理且归一化后的数据
 * 以及提供和UI界面交互的逻辑
 */
package com.example.da_chuang;


import android.content.Context;
import android.util.Log;

import com.example.da_chuang.libsvm.svm_predict;
import com.example.da_chuang.libsvm.svm_train;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class InteractionSpec {
    static {
        System.loadLibrary("native-lib");
    }

    private final int RedPortNum = 4;
    private final int FEA_NUM = 12;
    private final int COL = 128;
    private final String[] args1;//调用train的参数
    private final String[] args2;//调用predict的参数
    private final String rawTrainFilePath;
    private final String rawTestFilePath;
    private final String labelFilePath;
    private final String trainFilePath;
    private final String testFilePath;
    private final String toolsFilePath;
    private final String resultFilePath;
    private final String modelFilePath;
    private int trainSampleNum;//训练样本数
    private int testSampleNum;//测试样本数


    public InteractionSpec() {
        System.out.println("");//TODO:这是一个测试,之后请删除这行代码
        Context c = MyApplication.getContext();
        rawTrainFilePath = Objects.requireNonNull(c.getFilesDir().getParentFile()).getPath() +
                "/data/trainyuan.txt";
        rawTestFilePath = c.getFilesDir().getParentFile().getPath() + "/data/testyuan.txt";
        labelFilePath = c.getFilesDir().getParentFile().getPath() + "/data/labelyuan.txt";
        trainFilePath = c.getFilesDir().getParentFile().getPath() + "/data/trainnn.txt";
        testFilePath = c.getFilesDir().getParentFile().getPath() + "/data/test.txt";
        toolsFilePath = c.getFilesDir().getParentFile().getPath() + "/data/tools.txt";
        resultFilePath = c.getFilesDir().getParentFile().getPath() + "/data/result.txt";
        modelFilePath = c.getFilesDir().getParentFile().getPath() + "/data/model.txt";
        args1 = new String[]{trainFilePath,
                modelFilePath, "-c 2.5"};
        args2 = new String[]{testFilePath,
                modelFilePath,
                resultFilePath};
    }

    public static double atof(String s) {
        /*
          将字符串s转换为浮点数
         */
        double d = Double.parseDouble(s);
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return (d);
    }

    /**
     * 根据文本数据来确定训练样本个数或测试样本个数
     *
     * @param FileName
     * @param b
     * @throws IOException
     */
    private void setSampleNum(String FileName, tag b) throws IOException {
        File file1 = new File(FileName);
        FileInputStream fis1 = new FileInputStream(file1);
        BufferedInputStream train_in = new BufferedInputStream(fis1);
        if (b == tag.Train) {
            while (train_in.read() != 35) {
                trainSampleNum++;
                while (train_in.read() != '\n')
                    ;
            }
            trainSampleNum = trainSampleNum / RedPortNum;
        } else {
            while (train_in.read() != 35) {
                testSampleNum++;
                while (train_in.read() != '\n')
                    ;
            }
            testSampleNum = testSampleNum / RedPortNum;
        }
        fis1.close();
        train_in.close();
    }

    /**
     * 读取文本数据流in的一行数据到t
     *
     * @param in
     * @param t
     * @throws IOException
     */
    public void readLine(BufferedInputStream in, byte[] t) throws IOException {
        int b;
        int j = 0;
        while (((byte) (b = in.read())) != '\n' && b != -1) {
            t[j] = (byte) b;
            j++;
        }
    }

    /**
     * features是特征个数*样本个数的矩阵=N*M，labels是标签数组<BR/>
     * N是特征个数*通道数，M是样本个数<BR/>
     * 把特征矩阵写成libsvm要求的格式<BR/>
     *
     * @param features
     * @param N
     * @param M
     * @param labels
     * @param trainDataFileName
     * @throws IOException
     */
    public void writer(double[][] features, int N, int M, double[] labels, String trainDataFileName)
            throws IOException {

        File newFile = new File(trainDataFileName);
        FileWriter write = new FileWriter(newFile, false);//false表示清空之前文件里的内容，重新写入
        BufferedWriter bufferedWriter = new BufferedWriter(write);
        for (int j = 0; j < M; j++) {//j代表第j个样本的特征
            bufferedWriter.write(labels[j] + " ");
            for (int i = 0; i < N; i++) {//i表示第i个特征
                bufferedWriter.write((i + 1) + ":" + features[i][j] + " ");
            }
            bufferedWriter.newLine();//换行
            bufferedWriter.flush();//刷新数据使之读入文件中
        }
        write.close();
        bufferedWriter.close();
    }

    /**
     * 写入单个测试样本的特征
     *
     * @param features
     * @param N
     * @param testDataFileName
     * @param fll
     * @throws IOException
     */
    public void writer(double[] features, int N, String testDataFileName, int fll) throws IOException {
        File newFile = new File(testDataFileName);
        FileWriter write = new FileWriter(newFile, true);//false表示清空之前文件里的内容，重新写入
        BufferedWriter bufferedWriter = new BufferedWriter(write);
        bufferedWriter.write((fll + 1) + " ");
        bufferedWriter.flush();
        for (int i = 0; i < N; i++) {
            bufferedWriter.write((i + 1) + ":" + features[i] + " ");
            bufferedWriter.flush();
        }
        bufferedWriter.write("\n");
        bufferedWriter.flush();
        write.close();
        bufferedWriter.close();
    }

    /**
     * 将用于测试数据归一化的tools(通道数*(2*特征数))写进fileName中
     *
     * @param tools
     * @param fileName
     * @throws IOException
     */
    public void saveTools(double[][] tools, String fileName) throws IOException {
        File newFile = new File(fileName);
        FileWriter write = new FileWriter(newFile, false);
        BufferedWriter bufferedWriter = new BufferedWriter(write);
        for (int i = 0; i < RedPortNum; i++) {
            for (int j = 0; j < FEA_NUM; j++) {
                bufferedWriter.write(tools[i][2 * j] + " " + tools[i][2 * j + 1] + " ");
            }
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
        write.close();
        bufferedWriter.close();
    }

    /**
     * 将存储在fileName中的tools矩阵读取出来
     *
     * @param tools
     * @param toolsFileName
     * @throws IOException
     */
    public void getTools(double[][] tools, String toolsFileName) throws IOException {

        File file1 = new File(toolsFileName);
        FileInputStream fis1 = new FileInputStream(file1);
        BufferedInputStream tools_in = new BufferedInputStream(fis1);
        byte[] temp = new byte[1200];
        for (int i = 0; i < RedPortNum; i++) {
            readLine(tools_in, temp);
            String x = new String(temp);
            String[] tools_data = x.split(" ");
            for (int j = 0; j < 2 * FEA_NUM; j++)
                tools[i][j] = atof(tools_data[j]);
        }
        fis1.close();
        tools_in.close();
    }

    /**
     * 从文件中读取训练data和对应的label矩阵，后续应该为蓝牙读取，不过我感觉也得先写入文件中
     *
     * @param traindatafileName
     * @param trainlabelfileName
     * @param data
     * @param labels
     * @throws IOException
     */
    public void getData(String traindatafileName, String trainlabelfileName, double[][] data,
                        double[] labels)
            throws IOException {
        File file1 = new File(traindatafileName);
        FileInputStream fis1 = new FileInputStream(file1);
        BufferedInputStream train_in = new BufferedInputStream(fis1);
        File file2 = new File(trainlabelfileName);
        FileInputStream fis2 = new FileInputStream(file2);
        BufferedInputStream label_in = new BufferedInputStream(fis2);
        byte[] temp = new byte[1500];
        for (int i = 0; i < RedPortNum * trainSampleNum; i++) {
            readLine(train_in, temp);
            String x = new String(temp);
            String[] train_data = x.split(" ");
            for (int j = 0; j < COL; j++) {
                data[i][j] = atof(train_data[j]);
            }
        }
        readLine(label_in, temp);
        String y = new String(temp);
        String[] label_data = y.split(" ");
        for (int i = 0; i < trainSampleNum; i++) {
            labels[i] = atof(label_data[i]);
        }
        fis1.close();
        fis2.close();
        train_in.close();
        label_in.close();
    }

    /**
     * 获取test矩阵，按libsvm要求的格式
     *
     * @param testDataFileName
     * @param data             单个测试数据集(通道数*col)
     * @throws IOException
     */
    public void getData(String testDataFileName, double[][] data) throws IOException {
        File file1 = new File(testDataFileName);
        FileInputStream fis1 = new FileInputStream(file1);
        BufferedInputStream test_in = new BufferedInputStream(fis1);
        byte[] temp = new byte[1200];
        for (int i = 0; i < testSampleNum * RedPortNum; i++) {
            readLine(test_in, temp);
            String x = new String(temp);
            String[] train_data = x.split(" ");
            for (int j = 0; j < COL; j++) {
                data[i][j] = atof(train_data[j]);
            }
        }
        fis1.close();
        test_in.close();
    }

    /**
     * train_button对应的逻辑
     *
     * @throws IOException
     */
    public void last_train() throws IOException {
        long startTime = System.currentTimeMillis();
        trainSampleNum = 0;
        setSampleNum(rawTrainFilePath, tag.Train);

        double[][] data = new double[RedPortNum * trainSampleNum][COL];
        double[] labels = new double[trainSampleNum];
        getData(rawTrainFilePath,
                labelFilePath, data, labels);

        double[][] features = new double[FEA_NUM * RedPortNum][trainSampleNum];
        double[][] tools = new double[RedPortNum][FEA_NUM * 2];
        get_Features(data, trainSampleNum * RedPortNum, COL, features, tools);
        saveTools(tools, toolsFilePath);

        writer(features, RedPortNum * FEA_NUM, trainSampleNum, labels,
                trainFilePath);

        //svm_train svm_train = new svm_train();

        svm_train.SVM_TRAIN(args1);

        long endTime = System.currentTimeMillis(); //结束时间
        long runTime = endTime - startTime;
        Log.i("train", String.format("方法使用时间 %d ms", runTime));
    }

    /**
     * predict_button对应的逻辑
     *
     * @throws IOException
     */
    public void last_predict() throws IOException {

        long startTime = System.currentTimeMillis(); //起始时间
        testSampleNum = 0;
        //svm_predict svm_predict = new svm_predict();
        double[][] y6 = new double[RedPortNum][2 * FEA_NUM];//用于归一化的数据
        getTools(y6, toolsFilePath);
        setSampleNum(rawTestFilePath, tag.Test);

        double[][] y3 = new double[RedPortNum * testSampleNum][COL];//test的数据
        double[][] y4 = new double[RedPortNum][COL];//用于测试的单个样本数据
        getData(rawTestFilePath, y3);
        for (int j = 0; j < testSampleNum; j++) {
            if (j == 0) {
                File log = new File(testFilePath);
                FileWriter fileWriter = new FileWriter(log, false);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
            for (int i = 0; i < RedPortNum; i++) {
                System.arraycopy(y3[testSampleNum * i + j], 0, y4[i], 0, COL);
            }
            double[] fe = new double[RedPortNum * FEA_NUM];//特征数组
            get_features(y4, fe, COL, y6);
            writer(fe, RedPortNum * FEA_NUM,
                    testFilePath, j / 10);
        }
        svm_predict.SVM_PREDICT(args2);
        long endTime = System.currentTimeMillis(); //结束时间
        long runTime = endTime - startTime;
        Log.i("predict", String.format("方法使用时间 %d ms", runTime));
    }

    /**
     * data是(样本个数*通道数)*col的原始数据，返回值res是(特征数*通道数)*(样本数)的特征矩阵<BR/>
     * data先全是通道1的数据，再全是通道2的数据...如下<BR/>
     * 第一行：样本1通道1<BR/>
     * 第二行：样本2通道1<BR/>
     * ...<BR/>
     *
     * @param data
     * @param size  样本个数*通道数
     * @param col
     * @param res   (特征数*通道数)*(样本数)的特征矩阵
     * @param tools 用于归一化的数据，为通道数*(特征数*2),如tools[1][0]和tools[1][1]就分别是第二个通道的第一个特征的mean和std
     * @return
     */
    public native int get_Features(double[][] data, int size, int col, double[][] res,
                                   double[][] tools);


    /**
     * 获取单个样本data的归一化特征集(特征数*通道数)<BR/>
     *
     * @param data     (样本个数1*通道数)*col的原始数据
     * @param features 特征数目*通道数的特征数组
     * @param col
     * @param tools    用于归一化的数据与get_Features中含义一样
     * @return
     */
    public native int get_features(double[][] data, double[] features, int col, double[][] tools);

    enum tag {Train, Test}


}
