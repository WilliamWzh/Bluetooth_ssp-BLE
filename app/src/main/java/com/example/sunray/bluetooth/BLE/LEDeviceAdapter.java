package com.example.sunray.bluetooth.BLE;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.sunray.bluetooth.R;

import java.util.List;

/**
 * Created by sunray on 2017-7-27.
 */

public class LEDeviceAdapter extends ArrayAdapter {
    private int resourceId;

    public LEDeviceAdapter(Context context, int textResourceId, List<BluetoothDevice> data) {
        super(context, textResourceId, data);
        resourceId = textResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = (BluetoothDevice)getItem(position);
        View view;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.content_view);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.name_view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.deviceName.setText(device.getName());
        viewHolder.deviceAddress.setText(device.getAddress());
        return view;
    }

    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
