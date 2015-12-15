package com.github.peejweej.example;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Salut network;

    private ListView sendList;
    private ListView receiveList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendList = (ListView) findViewById(R.id.send_list_view);
        receiveList = (ListView) findViewById(R.id.receive_list_view);

    }

    @Override
    protected void onResume() {
        super.onResume();
        testSomeStuff();
    }

    private void testSomeStuff(){

        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, new SalutDataCallback() {
            @Override
            public void onDataReceived(Object data) {
                Log.e(TAG, "Received data!");
            }
        });

        SalutServiceData serviceData = new SalutServiceData("uw_transfer", 77777, "sending");

        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
            }
        });

    }

    public void sendClicked(View view) {
        sendData();
    }

    public void receiveClicked(View view) {
        discoverForReceive();
    }

    private void sendData(){

        discoverForSend();
    }

    private void discoverForReceive(){

        network.discoverNetworkServices(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice device) {
                updateDataForReceive(network.foundDevices);
                Log.d(TAG, "A device has connected with the name " + device.deviceName);
            }
        }, true);
    }

    private void updateDataForReceive(List<SalutDevice> devices){

        receiveList.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.row_device, R.id.row_device_text_view, devices));
    }

    private void discoverForSend(){

        network.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice device) {
                updateSendData(network.foundDevices);
                Log.d(TAG, device.readableName + " has connected!");
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                Log.d(TAG, "success connecting");
                updateSendingList();
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                Log.d(TAG, "failure connecting");
            }
        });
    }

    private void updateSendingList(){

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                synchronized (this){
                    try{
                        wait(1000);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                updateSendData(network.foundDevices);
                updateSendingList();
            }
        }.execute();
    }

    private void sendToDevice(SalutDevice device){

        network.sendToDevice(device, "Test Data".getBytes(), new SalutCallback() {
            @Override
            public void call() {
                Log.e(TAG, "Oh no! The data failed to send.");
            }
        });
    }

    private void updateSendData(List<SalutDevice> devices){

        sendList.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.row_device, R.id.row_device_text_view, devices));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if(MyApp.isHost) {
            network.stopNetworkService(true);
//        }
//        else {
            network.unregisterClient(false);
//        }
    }
}
