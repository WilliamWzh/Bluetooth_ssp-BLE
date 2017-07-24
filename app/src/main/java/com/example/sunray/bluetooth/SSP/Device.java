package com.example.sunray.bluetooth.SSP;

/**
 * Created by sunray on 2017-7-20.
 */

public class Device{
    private String mDeviceName;
    private String mMacAddress;

    public Device(String mDeviceName,String mMacAddress){
        this.mDeviceName = mDeviceName;
        this.mMacAddress = mMacAddress;
    }

    public String getmDeviceName() {
        return mDeviceName;
    }

    public String getmMacAddress() {
        return mMacAddress;
    }
}
