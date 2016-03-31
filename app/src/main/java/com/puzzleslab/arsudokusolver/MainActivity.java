package com.puzzleslab.arsudokusolver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, OnTouchListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String TAG = "OCVSample::Activity";

    private MainView mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);

        mOpenCvCameraView = (MainView) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        verifyStoragePermissions(this);

        Training();
        TrainingAndTesting();
    }

    public void Training() {
        OpenCVLoader.initDebug();
        List<Integer> numbers = Arrays.asList(
                9, 8, 2, 1, 4, 8, 0, 8, 6, 5, 1, 3, 2, 8, 2, 3, 0, 6, 6, 4, 7, 0, 9, 3, 8,
                4, 4, 6, 0, 9, 5, 5, 0, 5, 8, 2, 2, 3, 1, 7, 2, 5, 3, 5, 9, 4, 0, 8, 1, 2,
                8, 4, 8, 1, 1, 1, 7, 4, 5, 0, 2, 8, 4, 1, 0, 2, 7, 0, 1, 9, 3, 8, 5, 2, 1,
                1, 0, 5, 5, 5, 9, 6, 4, 4, 6, 2, 2, 9, 4, 8, 9, 5, 4, 9, 3, 0, 3, 8, 1, 9,
                6, 4, 4, 2, 8, 8, 1, 0, 9, 7, 5, 6, 6, 5, 9, 3, 3, 4, 4, 6, 1, 2, 8, 4, 7);
        Mat thr = new Mat();
        Mat gray = new Mat();
        Mat con = new Mat();
        Mat src = CommonUtils.convertFileToMat(Environment.getExternalStorageDirectory().getAbsolutePath() + "/numbers.png", "");
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, thr, 200, 255, Imgproc.THRESH_BINARY_INV);
        thr.copyTo(con);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat sample = new Mat();
        Mat response_array = new Mat();

        Imgproc.findContours(con, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < contours.size(); i++) {
            if (Imgproc.contourArea(contours.get(i)) > 50){
                Rect r = Imgproc.boundingRect(contours.get(i)); //Find bounding rect for each contour
                if (r.height > 28) {
                    Imgproc.rectangle(src, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 0, 255), 2, 8, 0);
                    Mat ROI = thr.submat(r); // Crop the image
                    Mat tmp1 = new Mat();
                    Mat tmp2 = new Mat();
                    Imgproc.resize(ROI, tmp1, new Size(10, 10), 0, 0, Imgproc.INTER_LINEAR); //resize to 10x10
                    tmp1.convertTo(tmp2, CvType.CV_32FC1); //convert to float
                    sample.push_back(tmp2.reshape(1, 1)); // Store sample data
                    try {
                        //response_array.push_back(Imgcodecs.imdecode(new MatOfByte(numbers.get(i).byteValue()), Imgcodecs.IMWRITE_PXM_BINARY));
                        response_array.push_back(new MatOfInt(numbers.get(i)));
                    } catch (Exception ex) {
                        CommonUtils.printMatToPicture(src, Environment.getExternalStorageDirectory().getAbsolutePath() + "/editedSrc.jpg");
                    }
                    Imgproc.rectangle(src, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 0, 255), 2, 8, 0);
                }
            }
        }

        Mat response = new Mat();
        Mat tmp = response_array.reshape(1,1); // make continuous
        tmp.convertTo(response, CvType.CV_32FC1); // Convert to float

        CommonUtils.printMatToFile(sample, Environment.getExternalStorageDirectory().getAbsolutePath() + "/data.yml");
        CommonUtils.printMatToFile(response, Environment.getExternalStorageDirectory().getAbsolutePath() + "/label.yml");
    }

    public void TrainingAndTesting() {
        Mat thr = new Mat();
        Mat gray = new Mat();
        Mat con = new Mat();
        Mat src = CommonUtils.convertFileToMat(Environment.getExternalStorageDirectory().getAbsolutePath() + "/testing.png", "");
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, thr, 200, 255, Imgproc.THRESH_BINARY_INV);
        thr.copyTo(con);

        // Read stored sample and label for training
        //Imgcodecs.imread(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data.yml");
        Mat sample = CommonUtils.convertFileToMat(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data.yml", "");
        Mat response = CommonUtils.convertFileToMat(Environment.getExternalStorageDirectory().getAbsolutePath() + "/label.yml", "");

        KNearest knn = KNearest.create();
        knn.train(sample, 0, response);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(con, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat dst = new Mat(src.rows(), src.cols(), CvType.CV_8UC3, Scalar.all(0));

        for (int i = 0; i < contours.size(); i++) {
            if (Imgproc.contourArea(contours.get(i)) > 50){
                Rect r = Imgproc.boundingRect(contours.get(i)); //Find bounding rect for each contour
                if (r.height > 28) {
                    Mat ROI = thr.submat(r); // Crop the image
                    Mat tmp1 = new Mat();
                    Mat tmp2 = new Mat();
                    Mat results = new Mat();
                    Imgproc.resize(ROI, tmp1, new Size(10, 10), 0, 0, Imgproc.INTER_LINEAR); //resize to 10x10
                    tmp1.convertTo(tmp2, CvType.CV_32FC1); //convert to float
                    float p = knn.findNearest(tmp2.reshape(1,1), 1, results);
                    String name = String.valueOf(((int)p));
                    Imgproc.putText(dst, name,new Point(r.x, r.y + r.height), 0, 1, new Scalar(0, 255, 0), 2);
                }
            }
        }

        Imgcodecs.imwrite(Environment.getExternalStorageDirectory().getAbsolutePath() + "/results.jpg", dst);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }
}
