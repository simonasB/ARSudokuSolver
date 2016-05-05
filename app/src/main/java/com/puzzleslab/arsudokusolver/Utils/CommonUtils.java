package com.puzzleslab.arsudokusolver.Utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.puzzleslab.arsudokusolver.BuildConfig;

import org.apache.commons.io.FileUtils;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by simonas_b on 3/31/2016.
 */
public final class CommonUtils {


    public static Mat convertFileToMat(String fileAbsolutePath, String tag) {
        byte[] bytes = new byte[0];
        try {
            bytes = FileUtils.readFileToByteArray(new File(fileAbsolutePath));
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e(tag, "Could not find file to convert to Map object.", e);
            }
        }
        return Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public static void printMatToPicture(Mat mat, String path) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        try {
            FileOutputStream fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
        } catch (IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }

    public static void printMatToFile(Mat mat, String path) {
        byte[] responseBytes = new byte[(int) (mat.total() * mat.channels())];
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(responseBytes);
            fos.close();
        } catch (IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
