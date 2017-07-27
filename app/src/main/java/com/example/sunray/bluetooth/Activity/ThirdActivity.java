package com.example.sunray.bluetooth.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunray.bluetooth.BLE.BluetoothLEService;
import com.example.sunray.bluetooth.BLE.LEDeviceAdapter;
import com.example.sunray.bluetooth.R;
import com.example.sunray.bluetooth.BluetoothTools.BluetoothTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunray on 2017-7-21.
 */

public class ThirdActivity extends AppCompatActivity implements View.OnClickListener {
    public String TAG = "ThirdActivity.class";
    public int REQUEST_ENABLE = 1;
    public boolean switchSearchBt = true;

    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeScanner mBluetoothLeScanner;
    public BluetoothDevice mLastBluetoothDevice;

    public TextView stateView;
    public TextView contentView;
    public EditText editView;
    public ListView showView;
    public Button search_bt;
    public Button send_bt;

    private LEDeviceAdapter adapter;
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();


    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothTools.ACTION_LEDATA_SEND.equals(action)){
                BluetoothDevice mBluetoothDevice = intent.getParcelableExtra("Device_Data");
                deviceList.add(mBluetoothDevice);
                adapter.notifyDataSetChanged();
            }
            if(BluetoothTools.ACTION_OPEN_BLE.equals(action)){
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE);
            }

            if(BluetoothTools.ACTION_RECEIVELE_DATA.equals(action)){
                String receData = (String)intent.getExtras().get("receiveData");
//                Log.e(TAG,"11111"+receData);
                contentView.setText(receData);
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initLayout();
        showView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice mLastBluetoothDevice = deviceList.get(position);

                Intent deviceIntent = new Intent(BluetoothTools.ACTION_DEVICE_SEND);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("BluetoothDevice",mLastBluetoothDevice);
                deviceIntent.putExtras(mBundle);
                sendBroadcast(deviceIntent);
            }
        });


    }

    @Override
    protected void onStart() {
        Intent conIntent = new Intent(ThirdActivity.this, BluetoothLEService.class);
        startService(conIntent);

        IntentFilter discoveryFilter = new IntentFilter(BluetoothTools.ACTION_RECEIVELE_DATA);
        discoveryFilter.addAction(BluetoothTools.ACTION_LEDATA_SEND);
        discoveryFilter.addAction(BluetoothTools.ACTION_OPEN_BLE);
//        discoveryFilter.addAction();
        registerReceiver(mBluetoothReceiver, discoveryFilter);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Intent conIntent = new Intent(ThirdActivity.this, BluetoothLEService.class);
        stopService(conIntent);
//        scanLeDevice(false);
        unregisterReceiver(mBluetoothReceiver);
        super.onDestroy();
    }


    /**
     * 初始化布局
     */
    private void initLayout() {

        adapter = new LEDeviceAdapter(ThirdActivity.this, R.layout.device_item, deviceList);

        stateView = (TextView) findViewById(R.id.state_view);
        showView = (ListView) findViewById(R.id.my_listview);
        editView = (EditText) findViewById(R.id.edit_view);
        contentView = (TextView) findViewById(R.id.content_view);
        send_bt = (Button) findViewById(R.id.send_bt);
        search_bt = (Button) findViewById(R.id.search_bt);
        send_bt.setOnClickListener(this);
        search_bt.setOnClickListener(this);
        showView.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_bt: {
                if (switchSearchBt == true) {
                    Intent searchIntent = new Intent(BluetoothTools.ACTION_LESTART_SEARCH);
                    sendBroadcast(searchIntent);
                    switchSearchBt = false;

                } else{
                    switchSearchBt = true;
                }
                break;
            }

            case R.id.send_bt: {
                if ("".equals(editView.getText().toString().trim())) {
                    Toast.makeText(ThirdActivity.this, "输入不能为空",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent sendIntent = new Intent(BluetoothTools.ACTION_DATALE_TO_GAME);
                    sendIntent.putExtra("editViewData", editView.getText().toString());
                    sendBroadcast(sendIntent);
                    editView.getText().clear();
                    Log.e("BROADCAST", "--->");
                }
            }
        }
    }
}
