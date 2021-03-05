package com.example.da_chuang.gesture;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

/**
 * 接收蓝牙数据的服务线程
 */

public class BleGetService extends Service {
    //蓝牙设备的Service的UUID
    public final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805F9B34FB");
    public final static UUID UUID_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805F9B34FB");
    /*如果你的设备没修改默认设置的话，这里的UUID就不用改*/
    //蓝牙设备的notify的UUID

    public final String address = "3C:A5:19:7B:09:67";//改成要连接的设备的物理地址(Mac地址)
    int count = 0;
    final int judgePort=0; //用于初步阈值判断的端口 TODO:判断端口的设置
    final int thea=0; //用于初步阈值判断的阈值大小 TODO:thea的设置
    String rawData;
    String xzy = "";
    Intent intent = new Intent("com.example.service.bleGet");
    private BluetoothGatt mBluetoothGattOne;
    private final BluetoothGattCallback bluetoothGattCallbackOne = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("geanwen", "设备一连接成功");
                    //搜索Service
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mBluetoothGattOne.close();
                    Log.i("geanwen", "设备一连接断开");
                    Log.w("gea", "123");
                }
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //根据UUID获取Service中的Characteristic,并传入Gatt中
            super.onServicesDiscovered(gatt, status);
            BluetoothGattService bluetoothGattService = gatt.getService(UUID_SERVICE);
            BluetoothGattCharacteristic bluetoothGattCharacteristic =
                    bluetoothGattService.getCharacteristic(UUID_NOTIFY);
            boolean isConnect = gatt.setCharacteristicNotification(bluetoothGattCharacteristic,
                    true);
            if (isConnect) {
                Log.i("geanwen", "onServicesDiscovered: 设备一连接notify成功");
            } else {
                Log.i("geanwen", "onServicesDiscovered: 设备一连接notify失败");
            }
            enableNotification(mBluetoothGattOne, bluetoothGattCharacteristic);
        }

        /**
         * 蓝牙数据改变时回调执行该函数
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            /*数据改变时回调该函数*/
            super.onCharacteristicChanged(gatt, characteristic);
            String data = new String(characteristic.getValue());
            Log.i("geanwen",data+" ");
            if (!data.contains("\n")) {
                xzy = xzy + data;
            } else {
                xzy = xzy + data;
                String[] rawDataString = xzy.split(",");
                intent.putExtra("startPre", 0);
                if ((rawDataString.length == 18 && Double.parseDouble(rawDataString[judgePort]) >= thea) || count >= 5) {
                    count++;
                    //推进文件
                    if (count == 1) {
                        rawData = xzy;
                    } else {
                        rawData += xzy;
                    }
                    if (count == 128) {     //TODO：初始取128个点
                        count = 0;
                        //开启预处理进程
                        intent.putExtra("rawData", rawData);
                        intent.putExtra("startPre", 1);
                    }
                } else {
                    count = 0;
                }
                Log.i("geanwen", "onCharacteristicChanged:\n" + xzy);
                xzy = "";
                sendBroadcast(intent);
            }
        }

    };

    private void enableNotification(BluetoothGatt gatt,
                                    BluetoothGattCharacteristic characteristic) {
        if (gatt == null || characteristic == null) return; //这一步必须要有 否则收不到通知
        /*这个for循环是因为华为手机必须得这样设置才能收到数据*/
        for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGattOne.writeDescriptor(dp);
        }

        gatt.setCharacteristicNotification(characteristic, true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            //处理具体的逻辑
            //获取BluetoothManager
            BluetoothManager mBluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            //获取BluetoothAdapter
            BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();

            //如果蓝牙没有打开 打开蓝牙
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            //获取BlutoothDevice对象
            BluetoothDevice bluetoothDeviceOne = mBluetoothAdapter.getRemoteDevice(address);
            //如果Gatt在运行,将其关闭
            if (mBluetoothGattOne != null) {
                mBluetoothGattOne.close();
                mBluetoothGattOne = null;
            }
            //连接蓝牙设备并获取Gatt对象
            mBluetoothGattOne = bluetoothDeviceOne.connectGatt(BleGetService.this, true,
                    bluetoothGattCallbackOne);
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mBluetoothGattOne.disconnect();
        mBluetoothGattOne.close();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}