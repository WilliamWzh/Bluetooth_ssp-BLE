package com.example.sunray.bluetooth.BLE;

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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.sunray.bluetooth.BluetoothTools.BluetoothTools;
import com.example.sunray.bluetooth.SSP.Device;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by sunray on 2017-7-25.
 */

public class BluetoothLEService extends Service {
    public int state;
    public int SCAN_PERIOD = 10000;
    public boolean isSend = false;
    public boolean mScanning;
    public String TAG = "BluetoothBLEService";
    public String receiveData;
    public byte[] bytes;

    public BluetoothDevice mBluetoothDevice;
    public BluetoothDevice mLastBluetoothDevice;
    public BluetoothGatt mGatt = null;
    public BluetoothGattService service;
    public boolean sendData = true;
    public boolean scanBLE = false;

    //蓝牙适配器为单例，全局只有一个
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeScanner mBluetoothLeScanner;
    public BluetoothGattCharacteristic characteristicWrite;

    public List<BluetoothDevice> devList = new ArrayList<BluetoothDevice>();
//    public List<Device> deviceList = new ArrayList<Device>();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        initBLE();

        //controlReceiver的IntentFilter
        IntentFilter controlFilter = new IntentFilter();
        controlFilter.addAction(BluetoothTools.ACTION_DATALE_TO_GAME);
        controlFilter.addAction(BluetoothTools.ACTION_LESTART_SEARCH);
        controlFilter.addAction(BluetoothTools.ACTION_DEVICE_SEND);
        //注册BroadcastReceiver
        registerReceiver(controlReceiver, controlFilter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {

        mGatt.close();
        unregisterReceiver(controlReceiver);
        super.onDestroy();
        stopSelf();
    }

    private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothTools.ACTION_DATALE_TO_GAME.equals(action)){
                isSend = true;
                Log.e(TAG,"------------>send");
                try {
                    String editData = (String)intent.getExtras().get("editViewData");
                    bytes = editData.getBytes("gbk");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            if (BluetoothTools.ACTION_LESTART_SEARCH.equals(action)) {
                scanBLE = !mScanning;
                scanLeDevice(scanBLE);
            }

            if (BluetoothTools.ACTION_DEVICE_SEND.equals(action)) {
                mLastBluetoothDevice = intent.getParcelableExtra("BluetoothDevice");
//                Toast.makeText(getApplicationContext(), mLastBluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                mGatt = mLastBluetoothDevice.connectGatt(getApplicationContext(), true, mGattCallback);
                if (mGatt != null) {
                    if (mGatt.connect()) {
                        Log.e(TAG, "-------------------------Connect succeed.");
                    } else {
                        Log.e(TAG, "-------------------------Connect fail.");
                    }
                } else {
                    Log.e(TAG, "----------------------------BluetoothGatt null.");
                }
            }
        }
    };


    /**
     * BluetoothGatt回调
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                mGatt.discoverServices(); //执行到这里其实蓝牙已经连接成功
                Log.e(TAG, "Connected to GATT server.");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mBluetoothDevice != null) {
                    Log.e(TAG, "重新连接");
                    mGatt.connect();
                } else {
                    Log.e(TAG, "Disconnected from GATT server.");
                }
            }
        }


        /**
         * 发现服务的回调
         *
         * @param gatt
         * @param status
         */
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //成功发现服务后可以调用相应方法得到该BLE设备的所有服务，并且打印每一个服务的UUID和每个服务下各个特征的UUID
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT_connect_succeed");
                service = mGatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                if (service != null) {
                    BluetoothGattCharacteristic characteristicRead = service.getCharacteristic(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));
                    if (characteristicRead != null) {
                        gatt.setCharacteristicNotification(characteristicRead, true);
                        BluetoothGattDescriptor descriptor = characteristicRead.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                        Log.e("TAG","CharacteristicRead has saved");
                    }
                    characteristicWrite = service.getCharacteristic(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"));
                    if (characteristicWrite != null ) {
                        mGatt.setCharacteristicNotification(characteristicWrite, true);
//                        将指令放置进特征中
//                        characteristicWrite.setValue(new byte[] {0x7e});
//                        设置回复形式
//                        characteristicWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//                        开始写数据
//                        writeCharacteristic(new byte[] {0x7e});
                        Log.e("TAG","CharacteristicWrite has saved");
                    }
                }
                createWriteDataThread();
            }
        }


        /**
         * 读操作的回调
         *
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        /**
         * 通知回调
         *
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            Log.e(TAG, "读取成功" + characteristic.getValue().toString());
//            Log.e(TAG, "读取成功" + byte2hex(characteristic.getValue()));
            try {
                receiveData = new String(characteristic.getValue(),"gbk");

                Intent receiveIntent = new Intent(BluetoothTools.ACTION_RECEIVELE_DATA);
                receiveIntent.putExtra("receiveData",receiveData);
                sendBroadcast(receiveIntent);
                Log.e(TAG, "读取成功" + new String(characteristic.getValue(), "gbk"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        /**
         * 写操作回调
         *
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            try {
                boolean isBoolean = false;
//                isBoolean = mGatt.writeCharacteristic(characteristic);
                Log.e(TAG, "BluetoothAdapter_writeCharacteristic = " + isBoolean);  //如果isBoolean返回的是true则写入成功
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };




    /**
     * 进行BLE支持检以及初始化
     */
    private boolean initBLE() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //检测是否支持蓝牙ble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getApplicationContext(), "ble_not_supported_in_this_phone", Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        //如果没有打开蓝牙，则打开,两种情况，一种是强制打开，一种是对话框打开
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent openBlEIntent = new Intent(BluetoothTools.ACTION_OPEN_BLE);
            sendBroadcast(openBlEIntent);
        } else {
            Log.e(TAG, "蓝牙已经打开");
//            stopSelf();
            return false;
        }
        return true;

    }

    /**
     * 蓝牙BLE扫描
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        Log.e(TAG, "START_TO_SCAN");
        Handler mHandler = new Handler();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);
            Toast.makeText(getApplicationContext(), "扫描10s", Toast.LENGTH_SHORT).show();
            mScanning = true;
            mBluetoothLeScanner.startScan(mScanCallback);
        } else {
            Toast.makeText(getApplicationContext(), "停止扫描", Toast.LENGTH_SHORT).show();
            mScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
    }


    /**
     * 蓝牙Ble扫描结果回调函数
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        boolean flag = true;

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
            if (result != null) {
                BluetoothDevice device = result.getDevice();
                if ((device != null) && (devList != null)) {
                    for (BluetoothDevice de : devList) {
                        if (device.getName().equals(de.getName())) {
                            flag = false;
                            break;
                        } else {
                            flag = true;
                        }
                    }
                    if (true == flag) {
                        devList.add(device);
                        Intent dataListIntent = new Intent(BluetoothTools.ACTION_LEDATA_SEND);
                        Bundle mBundle = new Bundle();
                        mBundle.putParcelable("Device_Data", device);
                        dataListIntent.putExtras(mBundle);
                        sendBroadcast(dataListIntent);
                    }
                }
            }
        }
    };

    public void createWriteDataThread() {
        Log.e(TAG,"-------> createWriteDataThread()");
//        setCharacteristicNotification(characteristicWrite, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (isSend == true) {
                        characteristicWrite.setValue(bytes);
                        mGatt.writeCharacteristic(characteristicWrite);
                        Log.e(TAG, "------->write");
                        isSend = false;
                    }
                }
            }
        }).start();
    }
}
