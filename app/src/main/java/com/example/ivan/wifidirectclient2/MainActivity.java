package com.example.ivan.wifidirectclient2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WifiP2pManager.PeerListListener {

    private static final String TAG = "NEUTRAL";

    //USER-INTERFACE
    FrameLayout                     frame_layout1;
    Button                          button_transmit, button_start_server;

    //CAMERA
    Preview                         mPreview;

    //AUDIO
    Audio                           audio;

    //CONNECTIVITY
    WifiP2pManager                  mManager;
    WifiP2pManager.Channel          mChannel;
    BroadcastReceiver               mReceiver;
    WifiP2pConfig                   config = new WifiP2pConfig();
    WifiP2pInfo                     wifiP2pInfo;
    WifiP2pDevice                   targetDevice;
    List<WifiP2pDevice>             peers = new ArrayList<WifiP2pDevice>();
    IntentFilter                    mIntentFilter;
    public final int                port = 7950;

    //SYSTEM MANAGEMENT
    Boolean                         transferReadyState=false;
    Boolean                         validPeers = false;
    boolean                         activeTransfer = false;

    //TRANSMISSION
    DataManagement                  dm;
    DataTransmission                dt;
    Thread                          dt_thread, a_thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SET UP WIFI-DIRECT
        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //CONDUCT PEER DISCOVERY ONE TO ENABLE DISCOVERY BY OTHER DEVICE
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                Log.d(TAG,"Wifi Direct Initiation: Peer Discovery Conducted");
            }
            @Override
            public void onFailure(int reason) {
                Log.d(TAG,"Wifi Direct Initiation: Peer Discovery Unsuccessful");
            }
        });

        //SET UP CAMERA
        frame_layout1 = (FrameLayout)findViewById(R.id.layout_frame1);
        mPreview = new Preview(this);
        frame_layout1.addView(mPreview);

        //SET DATA MANAGER
        dm = new DataManagement(this);
        dm.testDataManagerCall();
        mPreview.setDataManager(dm);

        //SET DATA TRANSMISSION
        dt = new DataTransmission(this, port,wifiP2pInfo,dm);

        //SET AUDIO
        //audio = new Audio(dm);

        button_start_server = (Button)findViewById(R.id.button_start_server);
        button_start_server.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG,"Starting Service");
                startService();
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
        dm.setConnectionStatus(false);
        mPreview.pausePreview();
        dt_thread.interrupt();
    }

    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device){
        Log.d(TAG, "Network Set to Ready");
        wifiP2pInfo=info;
        targetDevice=device;
        dm.setConnectionStatus(true);
    }

    public void setNetworkToPendingState(boolean status){
        Log.d(TAG, "Network Set to Pending");
    }

    public void setClientStatus(String message){
        Log.d(TAG, "Client Status Message: " + message);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.d(TAG,"onPeersAvailable Call Back");
        peers.clear();
        peers.addAll(wifiP2pDeviceList.getDeviceList());
        validPeers = true;
    }

    public void startService() {
        Log.d(TAG, "Start Service Function Called");
        dt.updateInitialisationData(wifiP2pInfo);
        dt_thread = new Thread(dt);
        dt_thread.start();

        //a_thread = new Thread(audio);
        //a_thread.start();
    }
}
