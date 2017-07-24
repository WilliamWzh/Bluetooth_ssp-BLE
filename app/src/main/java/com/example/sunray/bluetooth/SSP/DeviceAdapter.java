package com.example.sunray.bluetooth.SSP;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.sunray.bluetooth.R;
import com.example.sunray.bluetooth.SSP.Device;

import java.util.List;

/**
 * Created by sunray on 2017-7-20.
 */

public class DeviceAdapter extends ArrayAdapter<Device>{
    private int resourceId;
    public DeviceAdapter(Context context,   int textResourceId, List<Device> data) {
        super(context, textResourceId,data);
        resourceId = textResourceId;
    }

    @Override
    public View getView(int position, View convertView,ViewGroup parent) {
        Device device = getItem(position);
        View view ;
        ViewHolder viewHolder = new ViewHolder();
        if(convertView == null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder.deviceAddress = (TextView)view.findViewById(R.id.content_view);
            viewHolder.deviceName = (TextView)view.findViewById(R.id.name_view);
            view.setTag(viewHolder);
        }else
        {
            view= convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.deviceName.setText(device.getmDeviceName());
        viewHolder.deviceAddress.setText(device.getmMacAddress());
        return view;
    }

    class ViewHolder{
        TextView deviceName;
        TextView deviceAddress;
    }
}
