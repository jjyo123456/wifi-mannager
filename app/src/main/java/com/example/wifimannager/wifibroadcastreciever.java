package com.example.wifimannager;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.widget.Toast;

public class wifibroadcastreciever extends BroadcastReceiver {
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel channel;
    public Context context;

    public wifibroadcastreciever(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, Context context, MainActivity mainActivity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public MainActivity mainActivity;




    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)){

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                Toast.makeText(context, "yessssss", Toast.LENGTH_SHORT).show();
            }

         // to check if the wifi p2p is enabled
        }
        else if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
            //to check if there are any new connections or diconnection
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                    // Handle connection info here
                    Toast.makeText(mainActivity, "Connection info received", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)){
            //toi see that is there a change in the peers with qwhom someone can connect for p2p wifi direct

            wifiP2pManager.requestPeers(channel,mainActivity.listener);

        }
        else if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){
            // to check any change in the configration related to the setting of wifi direct p2p
        }

       // WifiP2pManager manager = (WifiP2pManager)  getSystemService(context.getApplicationContext(), Context.WIFI_P2P_SERVICE);



    }
}
