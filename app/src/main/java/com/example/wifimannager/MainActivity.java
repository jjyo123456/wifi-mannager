package com.example.wifimannager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public WifiP2pManager.PeerListListener listener;
    List<WifiP2pDevice> peerslist = new ArrayList<>();
    // to store the list of the peers .
    public String[] devicenamee;
    public WifiP2pDevice[] devices;
   public ListView listView;
   public final int MESSAGE_READ_CODE_DYNAMIC = 1;
   public Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        listView = findViewById(R.id.listView);
        b  =  findViewById(R.id.button2);

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch(msg.what){
                    case MESSAGE_READ_CODE_DYNAMIC:
                        byte[] readerbuffobject = (byte[]) msg.obj;
                        // above the msg recieved to handler is being stored and converted to a byte type array object .
                        // now this byte tye object can be used to read the meassage
                        // byte is used as the message can contain data of different datatypes string int ,ect and byte can accomodate anything hence .
                        String theactualmessage  = new String(readerbuffobject,0,msg.arg1);
                        // here the readerbuffobject is being read from the index 0 till the length of the object which will be passed as argument 1 while the creation of object and is accessed with msg.arg1.
                        // now to be displayed in a textview
                }
                return false;
            }
        });

         class sendrecieve extends Thread{
             public Socket socket;
             public OutputStream outputStream;
             public InputStream inputStream;


             public sendrecieve(Socket socket) {
                 this.socket = socket;
                 try {
                     this.inputStream = socket.getInputStream();
                     this.outputStream = socket.getOutputStream();
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }

             }

             @Override
             public void run() {

                 byte[] theobjectinwhichthethemessageofinputstreamwouldbestorefortransfer = new byte[1024];
                 int lengthofmessage;
                 while(socket!=null){
                     try {
                         lengthofmessage = inputStream.read(theobjectinwhichthethemessageofinputstreamwouldbestorefortransfer);
                        // here the the message is stored in the byte type object from the inputstream and the length of the message is stored in the lengthofmessage object
                         if(lengthofmessage>0){
                             handler.obtainMessage(MESSAGE_READ_CODE_DYNAMIC,lengthofmessage,-1,theobjectinwhichthethemessageofinputstreamwouldbestorefortransfer).sendToTarget();
                             // this message hat format (what,arg1,arg2,objectcinsistingmessage) , since there is no arg2 hence -1 is passed
                         }
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }

                 }

                 super.run();
             }

             public void write(byte[] writtenmessage){
                 try {
                     outputStream.write(writtenmessage);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }


             // here there should be the part where on button click the content is stracted from the editext which is then sent to the write method to be sent as a message .





         }


        // wifi on/of code
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }

        WifiP2pManager manager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        // this class provides the api for mannaging wifi peer to peer connectivity
        WifiP2pManager.Channel channel = manager.initialize(getApplicationContext().getApplicationContext(), Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            //channel allow the app to connect to the wifi p2p framework so that it can discver connect diconnect to other devices
            public void onChannelDisconnected() {
                // any changes to channel will be recievde in the form of callback to the channellistener
            }
        });

        BroadcastReceiver broadcastReceiver = new wifibroadcastreciever(manager,channel,getApplicationContext(),this);
        // here a broadcastreciever object is created for the class wifibroadcastreciever  .

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // discovery started successfully
            }

            @Override
            public void onFailure(int reason) {
                //discovery failedto start
            }
        });

        listener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                if(peers.getDeviceList().equals(peerslist)){
                    //peers consist of the list of devices as peers
                    peerslist.clear();
                    peerslist.addAll(peers.getDeviceList());

                    devicenamee = new String[peers.getDeviceList().size()];
                    devices = new WifiP2pDevice[peers.getDeviceList().size()];
                    int i =0;
                    for(WifiP2pDevice device:peers.getDeviceList()){
                        devicenamee[i] = device.deviceName;
                        devices[i] = device;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.single_itemm,devicenamee);
                    listView.setAdapter(adapter);
                }
            }
        };
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 'position' variable contains the position of the item clicked
                // You can use this position to retrieve the corresponding object from your adapter or data source
                // For example:
              //  Object clickedObject = adapter.getItem(position);
                final WifiP2pDevice device  = devices[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });
                WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if(info.isGroupOwner){
                            Toast.makeText(MainActivity.this, "owner", Toast.LENGTH_SHORT).show();
                        }
                    }
                };



                // Now you can perform actions based on the clicked item
            }
        });
        // so the connectioninfo listener is being specified in the brodcast reciever which recieves any broadcast for any change then it reuests for the info of connection and specifies the listener whom the info has to be sent
// channel acts as the connection between the device and the whole wifip2p framework so that it can command to for example to request connection info
         class serversidethread extends Thread{
             @Override
             public void run() {
                 Socket socket = new Socket();
                 try {
                     ServerSocket serverSocket = new ServerSocket(8080);
                     socket = serverSocket.accept();
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
                 super.run();
             }
         }

         class clientclass extends Thread{
             Socket socket;
             // thois sockey would be mostly used to connect to the inputstream 
             String hostadd;
             public clientclass(InetAddress hostipaddress){
                 this.hostadd = hostipaddress.getHostAddress();
                 this.socket = new Socket();
             }

             @Override
             public void run() {
                 try {
                     socket.connect(new InetSocketAddress(hostadd, 8080),500);

                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }

                 super.run();
             }
         }
    }
}