package com.example.sunray.bluetooth.SSP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.sunray.bluetooth.BluetoothTools.BluetoothTools;

import java.io.IOException;

/**
 * 蓝牙客户端连接线程
 */
public class BluetoothClientConnThread extends Thread{

	private Handler serviceHandler;		//用于向客户端Service回传消息的handler
	private BluetoothDevice serverDevice;	//服务器设备
	private BluetoothSocket socket;		//通信Socket

	/**
	 * 构造函数
	 * @param handler
	 * @param serverDevice
	 */
	public BluetoothClientConnThread(Handler handler, BluetoothDevice serverDevice) {
		this.serviceHandler = handler;
		this.serverDevice = serverDevice;
	}

	@Override
	public void run() {
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		try {
			//UUID匹配
			socket = serverDevice.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
			socket.connect();
			Log.e("Socket connect",String.valueOf(socket.isConnected()));


		} catch (Exception ex) {
			try {
				//如果失败，则关闭socket
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//发送连接失败消息
			
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			return;
		}


		//发送连接成功消息，消息的obj参数为连接的socket
		Message msg = serviceHandler.obtainMessage();
		msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
		msg.obj = socket;
		msg.sendToTarget();
	}
}
