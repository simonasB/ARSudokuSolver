package com.puzzleslab.arsudokusolver.Views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

/**
 * Created by Simonas on 2016-05-17.
 */
public class SudokuView extends JavaCameraView implements Camera.PictureCallback{

    private static final String TAG = "SudokuView";

    public SudokuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        if(params.getFlashMode() != null) {
            params.setFlashMode(effect);
            mCamera.setParameters(params);
        } else {
            Log.w(TAG, "Flashlight is not supported on this device.");
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }
}
