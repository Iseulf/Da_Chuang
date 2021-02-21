#include <jni.h>
#include <cstdio>
#include <string>
#include "native-Da_Chuang.h"

/**
 * Java与C的接口
 * 一个是传入原始数据集进行预处理、获取特征集并返回特征集
 * 一个是传入测试信号的原始数据，返回特征集*/
vector<double> Translate(double *p, int len) {
    vector<double> x;
    x.reserve(len);
    for (int i = 0; i < len; i++)
        x.push_back(p[i]);
    return x;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_da_1chuang_InteractionSpec_get_1features(JNIEnv *env, jobject thiz,
                                                          jobjectArray data, jdoubleArray features,
                                                          jint col, jobjectArray tools) {
    // TODO: implement get_features()
    vector<double> f(FEA_NUM * CHAN_NUM);
    for (int i = 0; i < CHAN_NUM; i++) {
        auto q2 = static_cast<jarray>(env->GetObjectArrayElement(tools, i));
        double *p2 = env->GetDoubleArrayElements(static_cast<jdoubleArray>(q2), nullptr);
        auto q = static_cast<jarray>(env->GetObjectArrayElement(data, i));
        double *p = env->GetDoubleArrayElements(static_cast<jdoubleArray>(q), nullptr);
        vector<double> y1 = Translate(p, col);
        Features F = get_Features(y1);
        vector<double> y2 = Change_Format(F);
        for (int k = 0; k < FEA_NUM; k++)
            f[FEA_NUM * i + k] = (y2[k] - p2[2 * k]) / p2[2 * k + 1];
    }
    jdouble temp[FEA_NUM * CHAN_NUM];
    for (int i = 0; i < FEA_NUM * CHAN_NUM; i++)
        temp[i] = f[i];
    env->SetDoubleArrayRegion(features, 0, FEA_NUM * CHAN_NUM, temp);
    return 1;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_da_1chuang_InteractionSpec_get_1Features(JNIEnv *env, jobject thiz,
                                                          jobjectArray data, jint size, jint col,
                                                          jobjectArray res, jobjectArray tools) {
    // TODO: implement get_Features()
    vector<vector<double>> f(FEA_NUM * CHAN_NUM, vector<double>(size / CHAN_NUM));//存储数据特征
    for (int i = 0; i < size / CHAN_NUM; i++) {
        for (int j = 0; j < CHAN_NUM; j++) {
            auto q = static_cast<jarray>(env->GetObjectArrayElement(data,
                                                                    j * size / CHAN_NUM + i));
            double *p = env->GetDoubleArrayElements(static_cast<jdoubleArray>(q), nullptr);
            vector<double> y1 = Translate(p, col);
            Features F = get_Features(y1);
            vector<double> y2 = Change_Format(F);
            for (int k = 0; k < FEA_NUM; k++)
                f[FEA_NUM * j + k][i] = y2[k];
        }
    }
    vector<vector<double>> f2(CHAN_NUM, vector<double>(2 * FEA_NUM));
    f = normalize(f, f2);
    jdouble temp[1024];
    for (int i = 0; i < CHAN_NUM; i++) {
        for (int j = 0; j < FEA_NUM; j++) {
            temp[2 * j] = f2[i][2 * j];
            temp[2 * j + 1] = f2[i][2 * j + 1];
        }
        jdoubleArray doubleArr = env->NewDoubleArray(2 * FEA_NUM);
        env->SetDoubleArrayRegion(doubleArr, 0, 2 * FEA_NUM, temp);
        env->SetObjectArrayElement(tools, i, doubleArr);
        env->DeleteLocalRef(doubleArr);
    }
    for (int i = 0; i < FEA_NUM * CHAN_NUM; i++) {
        jdoubleArray doubleArr = env->NewDoubleArray(size / CHAN_NUM);
        for (int j = 0; j < size / CHAN_NUM; j++)
            temp[j] = (f[i][j]);
        env->SetDoubleArrayRegion(doubleArr, 0, size / CHAN_NUM, temp);
        env->SetObjectArrayElement(res, i, doubleArr);
        env->DeleteLocalRef(doubleArr);
    }
    return 1;
}