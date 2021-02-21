//
// Created by ac034 on 2020/11/1.
//

#ifndef DA_CHUANG_NATIVE_DA_CHUANG_H
#define DA_CHUANG_NATIVE_DA_CHUANG_H

#include <vector>
#include <cmath>
#include<algorithm>

#define FEA_NUM 12
#define CHAN_NUM 4
using namespace std;
typedef double FFT_TYPE;
typedef struct complex_st {
    //复数结构体，用于计算fft
    FFT_TYPE real;
    FFT_TYPE img;
} complex;
typedef struct {
    //特征结构体，用于存储数据的均值、方差等特征
    double mean;
    double std;
    double skewness;
    double kurtosis;
    double rms;
    double median;
    double pp;
} features;
typedef struct {
    //时域和频域特征
    features time;
    features fre;
} Features;




Features get_Features(vector<double> &data);

//获取数据的时域和频域特征
vector<vector<double>> normalize(vector<vector<double>> &x, vector<vector<double>> &F4);

//x是一个特征个数*样本个数的矩阵，返回值是每个特征归一化后的矩阵
vector<double> Change_Format(Features &F4);
//将Features改变成vector模式
#endif //DA_CHUANG_NATIVE_DA_CHUANG_H
