package com.example.sunray.bluetooth.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.sunray.bluetooth.SSP.BluetoothClientService;
import com.example.sunray.bluetooth.SSP.BluetoothClientService;
import com.example.sunray.bluetooth.SSP.BluetoothTools;
import com.example.sunray.bluetooth.SSP.ClsUtils;
import com.example.sunray.bluetooth.SSP.Device;
import com.example.sunray.bluetooth.SSP.DeviceAdapter;
import com.example.sunray.bluetooth.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunray on 2017-7-19.
 */

public class SecondActivity extends AppCompatActivity implements View.OnClickListener{
    private String pin = "0000";
    private String TAG = "TAG";
    public String action;
    public int REQUEST_ENABLE = 1;

    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private List<Device> deviList = new ArrayList<Device>();

    public TextView stateView;
    public TextView contentView;
    public EditText editView;
    public ListView showView;
    public Button search_bt;
    public Button send_bt;

    public BluetoothDevice mBluetoothDevice = null;
    public BluetoothDevice mLastBluetoothDevice = null;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public DeviceAdapter adapter;


    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, final Intent intent) {
            action = intent.getAction();

            //如果找到蓝牙设备
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.add(mBluetoothDevice);
                Device device = new Device(mBluetoothDevice.getName(),mBluetoothDevice.getAddress());
                if(!deviList.contains(device)) {
                    deviList.add(device);
                }
                adapter.notifyDataSetChanged();
                showView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mLastBluetoothDevice = deviceList.get(position);
                        try
                        {
                            ClsUtils.createBond(mLastBluetoothDevice.getClass(), mLastBluetoothDevice);
                            ClsUtils.setPin(mLastBluetoothDevice.getClass(), mLastBluetoothDevice,pin);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        finally {
                            Intent succIntent = new Intent();
                            Bundle mBundle = new Bundle();
                            mBundle.putParcelable("Pairing_Succ",mLastBluetoothDevice);
                            succIntent.setAction(BluetoothTools.ACTION_PAIRING_SUCC);
                            succIntent.putExtras(mBundle);
                            sendBroadcast(succIntent);
                        }
                    }
                });
            }


            //如果收到数据
            if(BluetoothTools.ACTION_RECEIVE_DATA.equals(action)){
                String recData = (String)intent.getExtras().get("recData");
                Toast.makeText(context,recData,Toast.LENGTH_LONG).show();
                contentView.setText(recData);
            }


            //如果连接失败
            if(BluetoothTools.ACTION_CONNECT_ERROR.equals(action)){
                Toast.makeText(context,"连接失败",Toast.LENGTH_SHORT).show();
            }


            //如果连接成功
            if(BluetoothTools.ACTION_CONNECT_SUC.equals(action)){
                Toast.makeText(context,"连接成功",Toast.LENGTH_LONG).show();
            }
        }
    };


    //UI初始化
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initBluetooth();
        adapter = new DeviceAdapter(SecondActivity.this,R.layout.device_item,deviList);

        stateView = (TextView)findViewById(R.id.state_view);
        showView = (ListView)findViewById(R.id.my_listview);
        editView = (EditText)findViewById(R.id.edit_view);
        contentView = (TextView)findViewById(R.id.content_view);
        send_bt = (Button)findViewById(R.id.send_bt);
        search_bt = (Button)findViewById(R.id.search_bt);
        send_bt.setOnClickListener(this);
        search_bt.setOnClickListener(this);
        showView.setAdapter(adapter);
    }


    //进行服务注册、广播注册
    @Override
    protected void onStart() {
        //启动服务
        Intent startServ = new Intent(SecondActivity.this, BluetoothClientService.class);
        startService(startServ);

        //注册广播
        IntentFilter discoveryFilter = new IntentFilter();
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothTools.ACTION_CONNECT_ERROR);
        discoveryFilter.addAction(BluetoothTools.ACTION_CONNECT_SUC);
        discoveryFilter.addAction(BluetoothTools.ACTION_RECEIVE_DATA);
        registerReceiver(mBluetoothReceiver, discoveryFilter);

        super.onStart();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search_bt: {
                //若蓝牙适配器未启动，则自动启动
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();//异步的，不会等待结果，直接返回。

                    //如果设备正在寻找
                } else if(!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();

                } break;


            } case R.id.send_bt: {
                if ("".equals(editView.getText().toString().trim())) {
                    Toast.makeText(SecondActivity.this, "输入不能为空",
                            Toast.LENGTH_SHORT).show();

                }else {
                    //发送数据的广播
                    Intent actIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
                    actIntent.putExtra("editViewData",editView.getText().toString());
                    sendBroadcast(actIntent);
                }
            }
        }
    }



    //获取蓝牙适配器，当没有开启时开启
    private void initBluetooth() {
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);
    }
}
