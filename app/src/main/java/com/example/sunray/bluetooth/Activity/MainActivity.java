package com.example.sunray.bluetooth.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sunray.bluetooth.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public Button sspBt;
    public Button bleBt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sspBt = (Button)findViewById(R.id.ssp_bt);
        bleBt = (Button)findViewById(R.id.ble_bt);
        sspBt.setOnClickListener(this);
        bleBt.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ble_bt: {
                Intent bleIntent = new Intent(MainActivity.this, ThirdActivity.class);
                startActivity(bleIntent);
                break;
            }

            case R.id.ssp_bt:{
                Intent sspIntent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(sspIntent);
                break;
            }
        }
    }
}
