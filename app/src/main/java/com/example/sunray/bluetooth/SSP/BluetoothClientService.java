package com.example.sunray.bluetooth.SSP;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.example.sunray.bluetooth.BluetoothTools.BluetoothTools;

import java.io.UnsupportedEncodingException;


/**
 * Created by sunray on 2017-7-21.
 */

public class BluetoothClientService extends Service {

    public String actionCon;
    public BluetoothCommunThread communThread;
    private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            actionCon = intent.getAction();

            //如果匹配成功
            if (BluetoothTools.ACTION_PAIRING_SUCC.equals(actionCon)){
                Bundle mBundle = intent.getExtras();
                BluetoothDevice mBluetoothDevice = mBundle.getParcelable("Pairing_Succ");
                BluetoothClientConnThread mThread = new BluetoothClientConnThread(handler,mBluetoothDevice);
                mThread.start();
            }

            //如果键盘点击send发送数据
            if(BluetoothTools.ACTION_DATA_TO_GAME.equals(actionCon)){

                try {
                    String editData = (String)intent.getExtras().get("editViewData");

                    byte[] bytes = editData.getBytes("gbk");
                    communThread.write(bytes);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Service创建时的回调函数
     */
    @Override
    public void onCreate() {

        //controlReceiver的IntentFilter
        IntentFilter controlFilter = new IntentFilter();
        controlFilter.addAction(BluetoothTools.ACTION_PAIRING_SUCC);
        controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);

        //注册BroadcastReceiver
        registerReceiver(controlReceiver, controlFilter);
        super.onCreate();
    }


    Handler handler = new Handler()    {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                //子线程socket连接成功
                case BluetoothTools.MESSAGE_CONNECT_SUCCESS:{
                    Intent mIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUC);
                    sendBroadcast(mIntent);
                    communThread  = new BluetoothCommunThread(handler,(BluetoothSocket)msg.obj);
                    communThread.start();
                    break;

                //子线程连接错误
                } case BluetoothTools.MESSAGE_CONNECT_ERROR:{
                    Intent mIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
                    sendBroadcast(mIntent);
                    break;

                //子线程读取到数据
                } case BluetoothTools.MESSAGE_READ_OBJECT: {
                    //读取到对象
                    //发送数据广播（包含数据对象）
                    byte[] readBuf = (byte[]) msg.obj;
                    String ss = new String(readBuf, 0,msg.arg1);
                    Intent recIntent = new Intent(BluetoothTools.ACTION_RECEIVE_DATA);
                    recIntent.putExtra("recData",ss);
                    sendBroadcast(recIntent);
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public void onDestroy() {
        communThread.cancel();
        unregisterReceiver(controlReceiver);
        super.onDestroy();
    }
}
