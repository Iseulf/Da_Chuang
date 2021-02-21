//
// Created by ac034 on 2020/11/1.
//

/*一些数字信号处理模块，如fft、离散小波变换、归一化*/
#include <iostream>
#include "native-Da_Chuang.h"

#ifndef PI
#define PI (3.14159265)
#endif


int set_complex(vector<double> y, complex x[]) {
    //为complex结构体数组赋值
    int len = y.size();
    if (len == 0)
        return 0;
    for (int i = 0; i < len; i++) {
        x[i].real = y[i];
        x[i].img = 0;
    }
    return 1;
}

static void BitReverse(complex *x, complex *r, int n, int l) {
    int i = 0;
    int j = 0;
    short stk = 0;
    static complex *temp = nullptr;

    temp = (complex *) malloc(sizeof(complex) * n);
    if (!temp) {
        return;
    }

    for (i = 0; i < n; i++) {
        stk = 0;
        j = 0;
        do {
            stk |= (i >> (j++)) & 0x01;
            if (j < l) {
                stk <<= 1;
            }
        } while (j < l);

        if (stk < n) { /* 满足倒位序输出 */
            temp[stk] = x[i];
        }
    }
    for (i = 0; i < n; i++) {
        r[i] = temp[i];
    }
    free(temp);
}

int fft(complex *x, int N) {
    //模2fft算法,结果依然存储在x中
    //注意这个算法只能算2的整数次幂长度的数组
    int i, j, l, ip;
    static int M = 0;
    static int le, le2;
    static FFT_TYPE sR, sI, tR, tI, uR, uI;

    M = (int) (log(N) / log(2));

    BitReverse(x, x, N, M);

    for (l = 1; l <= M; l++) {
        le = (int) pow(2, l);
        le2 = (int) (le / 2);
        uR = 1;
        uI = 0;
        sR = cos(PI / le2);
        sI = -sin(PI / le2);
        for (j = 1; j <= le2; j++) {
            for (i = j - 1; i <= N - 1; i += le) {
                ip = i + le2;
                tR = x[ip].real * uR - x[ip].img * uI;
                tI = x[ip].real * uI + x[ip].img * uR;
                x[ip].real = x[i].real - tR;
                x[ip].img = x[i].img - tI;
                x[i].real += tR;
                x[i].img += tI;
            }
            tR = uR;
            uR = tR * sR - uI * sI;
            uI = tR * sI + uI * sR;
        }
    }
    return 0;
}

vector<double> get_fft_abs(const vector<double> &data) {
    //获取data的fft的幅度值
    double tmp1, tmp2;
    int n = data.size();
    complex x[n];
    vector<double> abs;
    set_complex(data, x);
    fft(x, n);
    for (int i = 0; i < n; i++) {
        tmp1 = pow(x[i].img, 2);
        tmp2 = pow(x[i].real, 2);
        abs.push_back(pow(tmp1 + tmp2, 0.5));
    }
    return abs;
}

complex *dft(const vector<double> &x, int N) {
    auto *dft = (complex *) malloc(N * sizeof(complex));
    for (int k = 0; k < N; k++) {
        dft[k].img = 0;
        dft[k].real = 0;
        for (int n = 0; n < N; n++) {
            dft[k].real += x[n] * cos(2 * PI / N * k * n);
            dft[k].img -= x[n] * sin(2 * PI / N * k * n);
        }
    }
    return dft;
}

vector<double> get_dft_abs(const vector<double> &data) {
    //获取data的fft的幅度值
    double tmp1, tmp2;
    int n = data.size();
    vector<double> abs;
    //set_complex(data, x);
    auto x = dft(data, n);
    for (int i = 0; i < n; i++) {
        tmp1 = pow(x[i].img, 2);
        tmp2 = pow(x[i].real, 2);
        abs.push_back(pow(tmp1 + tmp2, 0.5));
    }
    return abs;
}

vector<double> get_abs(const vector<double> &data) {
    int n = data.size();
    if (((n & (n - 1)) == 0))
        return get_fft_abs(data);
    else
        return get_dft_abs(data);
}

features get_features(vector<double> &data) {
    //获取数据特征
    //vector<double> tmp;
    int n = data.size();
    features data_features;
    data_features = {0};
    for (double i : data)
        data_features.mean += i;
    data_features.mean /= data.size();
    double sum_3 = 0, sum_4 = 0, sum_2 = 0, SUM_2 = 0, std;
    for (double i : data) {
        sum_2 += pow(i - data_features.mean, 2);
        sum_3 += pow(i - data_features.mean, 3);
        sum_4 += pow(i - data_features.mean, 4);
        SUM_2 += pow(i, 2);
    }
    data_features.std = sum_2 / (n - 1);
    data_features.std = pow(data_features.std, 0.5);
    std = sum_2 / n;
    data_features.skewness = sum_3 / n / pow(std, 1.5);
    data_features.kurtosis = sum_4 / n / pow(std, 2);
    data_features.rms = pow(SUM_2 / n, 0.5);
    sort(data.begin(), data.end());
    if (data.size() % 2)
        data_features.median = data[(n - 1) / 2];
    else
        data_features.median = (data[n / 2] + data[n / 2 - 1]) / 2;
    return data_features;
}


Features get_Features(vector<double> &data) {
    //获取数据的时域和频域特征
    Features f;
    f.time = get_features(data);
    vector<double> data_fft;
    for (double &i : data)
        i = i - f.time.mean;
    data_fft = get_abs(data);
    f.fre = get_features(data_fft);
    return f;
}

vector<vector<double>> normalize(vector<vector<double>> &x, vector<vector<double>> &F4) {
    //x是一个特征个数*样本个数的矩阵,将x归一化
    vector<double> data;
    vector<vector<double>> result;
    features f;
    double temp[FEA_NUM * CHAN_NUM][2];
    for (int j = 0; j < FEA_NUM * CHAN_NUM; j++) {
        data = x[j];
        vector<double> nor_data;
        f = get_features(data);
        temp[j][0] = f.mean;
        temp[j][1] = f.std;
        nor_data.reserve(data.size());
        for (double i : data)
            nor_data.push_back((i - f.mean) / f.std);
        result.push_back(nor_data);
    }
    //应该定义一个nor_data数组
    for (int i = 0; i < CHAN_NUM; i++) {
        for (int j = 0; j < FEA_NUM * 2; j += 2) {
            F4[i][j] = temp[j / 2 + FEA_NUM * i][0];//mean
            F4[i][j + 1] = temp[j / 2 + FEA_NUM * i][1];//std
        }
    }
    return result;
}


vector<double> Change_Format(Features &F44) {
    vector<double> x;
    features time = F44.time;
    features fre = F44.fre;
    x.push_back(time.kurtosis);
    x.push_back(time.mean);
    x.push_back(time.median);
    x.push_back(time.skewness);
    x.push_back(time.std);
    x.push_back(time.rms);
    x.push_back(fre.std);
    x.push_back(fre.rms);
    x.push_back(fre.skewness);
    x.push_back(fre.median);
    x.push_back(fre.mean);
    x.push_back(fre.kurtosis);
    return x;
}