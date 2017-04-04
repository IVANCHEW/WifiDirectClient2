package com.example.ivan.wifidirectclient2;

import android.content.Context;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

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
    boolean skip_frame = true;
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

        /*
        List<String> epson_supported = param.getSupportedEpsonCameraModes();
        Log.d(TAG, "Supported Modes:");
        for (String s : epson_supported){
            Log.d(TAG, "Mode: " + s);
        }
        Log.d(TAG,"Selected Mode: " + epson_supported.get(2));
        param.setEpsonCameraMode(epson_supported.get(2));
        previewSize = getSmallestPreviewSize();
        */

        //previewSize = getBestPreviewSize();

        getFPS();
        param.setPreviewFpsRange(fpsList.get(0)[0],fpsList.get(0)[1]);
        //Log.d(TAG,"Preview fps Selected: " + fpsList.get(0)[0] + " to " + fpsList.get(0)[1]);
        getResSize();
        // FOR SAMSUNG GALAXy S4
        //previewSize = resSize.get(7);

        // FOR BT-200
        previewSize = resSize.get(0);

        //Constant for NV21 format is 17
        //param.setPreviewFormat(17);
        param.setPreviewSize(previewSize.width,previewSize.height);
        mCamera.setDisplayOrientation(0);
        //For Portrait modes: mCamera.setDisplayOrientation(90);
        mCamera.setParameters(param);
        dm.setImageSize(previewSize.width, previewSize.height);

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

        //Log.d(TAG,"Preview Data Length: " + data.length);

        count = count + 1;

        if (!dm.getImageLoadStatus()) {
        /*
        //Log.d(TAG, "Frame received: " + count);
        YuvImage yuv = new YuvImage(data, param.getPreviewFormat(), previewSize.width, previewSize.height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 20, out);

        byte[] bytes = out.toByteArray();
        //Log.d(TAG,"Length of byte: " + bytes.length);
        dm.loadImage(bytes);
        */
            dm.loadImage(data);
        }
        /*
        if(skip_frame){
            skip_frame = false;
        }else {
            skip_frame = true;
            if (!dm.getImageLoadStatus()) {
                dm.loadImage(data);
            }
        }
        */
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

