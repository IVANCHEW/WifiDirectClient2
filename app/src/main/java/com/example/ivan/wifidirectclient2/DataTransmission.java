package com.example.ivan.wifidirectclient2;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by ivan on 17/3/17.
 */
public class DataTransmission implements Runnable{
    private static final String TAG = "NEUTRAL";

    private Boolean transmissionReady=false;
    private int port;
    private int audioBufSize=0;
    private byte[] nv21_buffer;
    private byte[] pictureData, audioData;
    int image_height, image_width;
    InetAddress targetIP;
    WifiP2pInfo wifiP2pInfo;

    int transfer_count = 0;
    private MainActivity mActivity;
    DataManagement dm;
    OutputStream os = null;
    BufferedOutputStream bos = null;
    Socket clientSocket = null;


    public DataTransmission(MainActivity activity, int p, WifiP2pInfo info, DataManagement d){
        Log.d(TAG,"Data Transmission Class Called");
        this.mActivity = activity;
        this.port = p;
        this.wifiP2pInfo = info;
        this.dm = d;
    }

    public void updateInitialisationData(WifiP2pInfo info){
        this.wifiP2pInfo = info;
    }

    public void run(){
        Log.d(TAG,"Initialising Data Transmission Class");
        targetIP = wifiP2pInfo.groupOwnerAddress;
        image_height = dm.getImageHeight();
        image_width = dm.getImageWidth();
        audioBufSize = dm.getAudioBufSize();

        try {
            clientSocket = new Socket(targetIP,port);
            clientSocket.setPerformancePreferences(0 , 1, 1);
            clientSocket.setTcpNoDelay(true);
            clientSocket.setSendBufferSize(1024*1024);
            clientSocket.setReceiveBufferSize(1024*1024);
            Log.d(TAG,"=========Client Socket Details=========");
            Log.d(TAG,"Send Buffer Size: " + clientSocket.getSendBufferSize());
            Log.d(TAG,"Receive Buffer Size: " + clientSocket.getReceiveBufferSize());
            os = clientSocket.getOutputStream();
            os = new BufferedOutputStream(clientSocket.getOutputStream());

        }catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }

        transmissionReady = true;

        //Log.d(TAG,"Send Configuration Data to Server");
        /*
        if(dm.getConnectionStatus()){
            sendConfigurationData();
        }
        */

        Log.d(TAG,"Begin Transmission Wait Loop");
        while (true){
            //Check if Wifi P2P is connected
            if (dm.getConnectionStatus()){
                //Check if image and Audio are available
                if (dm.getLoadStatus()){
                    Log.d(TAG,"Initating transfer: " + transfer_count);
                    transfer_count = transfer_count + 1;
                    transfer();
                }
                else{
                    Log.d(TAG,"Image not ready");
                }
            }
            //Delay before retrieving next frame
            synchronized (this) {
                try {
                    wait(10);
                } catch (Exception e) {}
            }

            if (Thread.interrupted()) {
                Log.d(TAG,"Thread Interrupted");

                try{
                    clientSocket.close();
                }catch (IOException e) {
                    Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
                }

                return;
            }
        }
    }

    public void transfer(){
        //Log.d(TAG,"Client: Processing Image");
        int         write;
        int         marker=0;
        int         picture_length;

        nv21_buffer = dm.getImage();
        YuvImage yuv = new YuvImage(nv21_buffer, 17, image_width, image_height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, image_width, image_height), 70, out);
        pictureData = out.toByteArray();
        byte[] transfer_length  = ByteBuffer.allocate(4).putInt(pictureData.length).array();
        picture_length = pictureData.length;

        Log.d(TAG, "Sending First Packet");
        try {
            os.write(transfer_length, 0, transfer_length.length);
        } catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }

        Log.d(TAG, "Sending Second Packet, Length of data: " + picture_length);
        while(marker < picture_length){

            if(picture_length - marker >=1024){
                write = 1024;
            }else{
                write = picture_length - marker;
            }
            try{
                //Log.d(TAG,"Output stream, marker position: " + marker + " write: " + write);
                os.write(pictureData, marker, write);
            } catch (IOException e) {
                Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
            }
            marker = marker + write;
        }
        dm.unloadImage();

        /*
        audioData = dm.getAudio();
        while(audioData==null){
            //Log.d(TAG,"Null audio data");
            try{
                Thread.sleep(100);
            }catch (InterruptedException e) {
                Log.d(TAG,"Error sleeping thread: " + e.toString());
            }
            audioData = dm.getAudio();
        }

        Log.d(TAG,"Sending Third Packet");
        try {
            //Log.d(TAG,"Writing Audio Data of length: " + audioData.length);
            os.write(audioData, 0, audioData.length);
        } catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }
        dm.unloadAudio();
        */

        //Log.d(TAG,"Send Complete");
    }

    public void sendConfigurationData(){
        byte[] audioBufSizeData =  ByteBuffer.allocate(4).putInt(audioBufSize).array();
        try {
            os.write(audioBufSizeData, 0, audioBufSizeData.length);
        } catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }
    }

    public static int byteArrayToInt(byte[] b)
    {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
}
