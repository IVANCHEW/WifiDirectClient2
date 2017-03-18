package com.example.ivan.wifidirectclient2;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.util.List;

//Preview class used to start the camera preview
public class Preview extends GLSurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private static final String TAG = "NEUTRAL";

    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.Parameters param;
    Camera.Size previewSize;

    List<Camera.Size> resSize;
    List<int[]> fpsList;

    int count = 0;

    //DATA TRANSMISSION
    DataManagement dm;

    public Preview(Context context) {
        super(context);

        safeCameraOpen();

        mHolder=getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
            mCamera = Camera.open();
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.d(TAG, "failed to open Camera: " + e.toString());
        }

        return qOpened;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            Log.d(TAG,"Surface Created");
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
        }catch(Exception e) {
            Log.d(TAG, "Error setting holder: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        param = mCamera.getParameters();
        previewSize = getSmallestPreviewSize();
        //previewSize = getBestPreviewSize();
        //param.setPreviewSize(previewSize.width,previewSize.height);

        getFPS();
        getResSize();
        param.setPreviewFpsRange(fpsList.get(0)[0],fpsList.get(0)[1]);
        //previewSize = resSize.get(7);
        //param.setPreviewSize(previewSize.width,previewSize.height);
        //Constant for NV21 format is 17
        param.setPreviewFormat(17);

        mCamera.setDisplayOrientation(0);
        //For Portrait modes: mCamera.setDisplayOrientation(90);
        mCamera.setParameters(param);

        try{
            mCamera.startPreview();
            Log.d(TAG, "Camera Preview Size: " + previewSize.width + " x " + previewSize.height);
            Log.d(TAG, "Preview Format: " + param.getPreviewFormat());
        }catch(Exception e){
            Log.d(TAG,"Error starting preview");
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            Log.d(TAG,"Surface Destroyed");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }catch(Exception e){
            Log.d(TAG,"Error stoppipng camera on surface destroy");
        }
    }

    private Camera.Size getBestPreviewSize (){

        Camera.Size result = null;
        Camera.Parameters param = mCamera.getParameters();
        for (Camera.Size size : param.getSupportedPreviewSizes()){
            if (result==null){result=size;}
            else{
                int resultArea = result.width*result.height;
                int newArea = size.width*size.height;

                if (newArea>resultArea){
                    result = size;
                }
            }
        }

        return result;
    }

    private Camera.Size getSmallestPreviewSize (){

        Camera.Size result = null;
        Camera.Parameters param = mCamera.getParameters();
        for (Camera.Size size : param.getSupportedPreviewSizes()){
            if (result==null){result=size;}
            else{

                int resultArea = result.width*result.height;
                int newArea = size.width*size.height;

                if (newArea<resultArea){
                    result = size;
                }
            }
        }

        return result;
    }

    public void getFPS(){
        try{
            fpsList=param.getSupportedPreviewFpsRange();
            int x1 = 0;
            for (int[] x: fpsList) {
                int[] y = fpsList.get(x1);
                x1 = x1 + 1;
                Log.d(TAG, "FPS List: " + y[0] + "," + y[1]);
            }
        }catch(Exception e){
            Log.d(TAG,"Error in getting FPS: " + e.getMessage());
        }

    }

    private void getResSize(){
        Log.d(TAG,"Available Resolution Sizes");
        try{
            resSize = param.getSupportedPreviewSizes();

            for (Camera.Size x: resSize){
                Log.d(TAG,"Width: " + x.width + " Height: " + x.height);
            }
        }catch(Exception e){
            Log.d(TAG,"Error in getting Res Size: " + e.getMessage());
        }
    }

    public void setRes(Camera.Size s){
        previewSize=s;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //dm.ping();
        try{
            count = count + 1;
            if (!dm.getImageLoadStatus()) {
                //Log.d(TAG, "Frame received: " + count);
                YuvImage yuv = new YuvImage(data, param.getPreviewFormat(), previewSize.width, previewSize.height, null);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 30, out);

                byte[] bytes = out.toByteArray();
                //Log.d(TAG,"Length of byte: " + bytes.length);
                dm.loadImage(bytes);
            }

        }catch(Exception e){
            Log.d(TAG,"Error in Preview Callback: " + e.getMessage());
        }
    }

    public void pausePreview(){
        mCamera.stopPreview();
        mCamera.release();
    }

    public void resumePreview(){
        Log.d(TAG,"Preview Class: Resume Preview Called");
        safeCameraOpen();
        mCamera.startPreview();
    }

    public void setDataManager(DataManagement d){
        dm = d;
        dm.testDataManagerCall();
    }

}
