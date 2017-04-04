package com.example.ivan.wifidirectclient2;

import android.util.Log;

/**
 * Created by ivan on 17/3/17.
 */
public class DataManagement {

    private static final String TAG = "NEUTRAL";

    //DEBUGGING
    int                 test_count = 0;
    int                 load_count = 0;

    //SYSTEM DATA
    byte[]              image_holder;
    byte[]              audio_holder;
    int                 image_height;
    int                 image_width;

    //SYSTEM MANAGEMENT
    boolean             image_loaded = false;
    boolean             audio_loaded = false;
    boolean             wifi_connected = false;
    private             MainActivity mActivity;

    public DataManagement(MainActivity activity){
        Log.d(TAG, "Data Manager Called");
        this.mActivity = activity;
    }

    public void loadImage(byte[] b){
        if (!image_loaded){
            //image_holder = Arrays.copyOf(b, b.length);
            image_holder = b;
            image_loaded = true;
            //Log.d(TAG, "Data Manager, Image loaded: " + load_count);
            load_count = load_count + 1;
        }else{
            Log.d(TAG,"Image not loaded, holder full");
        }
    }

    public boolean loadAudio (byte[] b){
        if(!audio_loaded){
            audio_holder = b;
            audio_loaded = true;
            //Log.d(TAG,"Data Manager, Audio Loaded: " + load_count);
            return true;
        }else {
            Log.d(TAG,"Audio not loaded, holder full");
            return false;
        }
    }

    public byte[] getImage(){
        if(image_loaded){
            /*
            YuvImage yuv = new YuvImage(image_holder, 17, 640, 480, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(100, 100, 540, 380), 50, out);
            byte[] image_buffer = out.toByteArray();
            return  image_buffer;
            */
            return image_holder;
        }else{
            Log.d(TAG,"No Image Loaded");
            return null;
        }
    }

    public byte[] getAudio(){
        if(audio_loaded){
            return audio_holder;
        }else{
            Log.d(TAG,"No Audio Loaded");
            return null;
        }
    }

    public void unloadImage(){
        image_loaded = false;
    }

    public void unloadAudio(){
        audio_loaded = false;
    }

    public boolean getImageLoadStatus(){
        return image_loaded;
    }

    public boolean getConnectionStatus(){
        return wifi_connected;
    }

    public boolean getLoadStatus(){
        if (image_loaded){
            return true;
        }else{
            return false;
        }
    }

    public int getImageHeight(){return image_height;}

    public int getImageWidth(){return image_width;}

    public void setConnectionStatus(boolean b){
        wifi_connected = b;
    }

    public void setImageSize(int w, int h){
        image_height = h;
        image_width = w;
    }

    public void testDataManagerCall(){
        Log.d(TAG,"Data Manager Call: " + test_count);
        test_count = test_count + 1;
    }


}
