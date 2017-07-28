package com.example.sunray.bluetooth.SSP;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.example.sunray.bluetooth.BluetoothTools.BluetoothTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 蓝牙通讯线程
 */
public class BluetoothCommunThread extends Thread {

    private Handler serviceHandler;        //与Service通信的Handler
    private BluetoothSocket socket;
    private InputStream mmInStream;        //对象输入流
    private OutputStream mmOutStream;    //对象输出流
    public volatile boolean isRun = true;    //运行标志位

    /**
     * 构造函数
     *
     * @param handler 用于接收消息
     * @param socket
     */
    public BluetoothCommunThread(Handler handler, BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.serviceHandler = handler;
        this.socket = socket;
        try {
            tmpOut = socket.getOutputStream();
            tmpIn = socket.getInputStream();
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //发送连接失败消息
//            serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
            e.printStackTrace();
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {

         // bytes returned from read()


        while (true) {
            try {
                byte[] buffer = new byte[1024];  // buffer store for the stream
                int bytes;
                bytes = mmInStream.read(buffer);
                serviceHandler.obtainMessage(BluetoothTools.MESSAGE_READ_OBJECT, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                break;
            }
        }

        //关闭流
        if (mmInStream != null) {
            try {
                mmInStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mmOutStream != null) {
            try {
                mmOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }


    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }
}
