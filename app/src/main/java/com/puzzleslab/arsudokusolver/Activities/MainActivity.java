package com.puzzleslab.arsudokusolver.Activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;

import com.puzzleslab.arsudokusolver.Modules.DigitLibrary;
import com.puzzleslab.arsudokusolver.Modules.FramePipeline;
import com.puzzleslab.arsudokusolver.Modules.HitCounters;
import com.puzzleslab.arsudokusolver.Modules.SCandidate;
import com.puzzleslab.arsudokusolver.Modules.SSuccess;
import com.puzzleslab.arsudokusolver.Modules.SudokuException;
import com.puzzleslab.arsudokusolver.Modules.SudokuState;
import com.puzzleslab.arsudokusolver.R;
import com.puzzleslab.arsudokusolver.Utils.Parameters;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String TAG = "MainActivity";

    private CameraBridgeViewBase cameraView;
    private Button scanButton;
    private int frameNr = 0;
    private Mat solution;
    private boolean calculationInProgress = false;
    private DigitLibrary defaultLibrary = new DigitLibrary();
    private HitCounters defaultHitCounts = new HitCounters();
    private SudokuState currState = new SudokuState();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                    //cameraView.setOnTouchListener(MainActivity.this);
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

        setContentView(R.layout.sudoku);

        cameraView = (CameraBridgeViewBase) findViewById(R.id.sudoku);
        cameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        scanButton = (Button) findViewById(R.id.button_rescan);
        verifyStoragePermissions(this);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currState = Parameters.DefaultState;
                scanButton.setVisibility(View.GONE);
                solution = null;
            }
        });
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cameraView != null)
            cameraView.disableView();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (solution != null) {
            return solution;
        } else {
            if (!calculationInProgress) {
                calculationInProgress = true;
                Log.i(TAG, "Starting to find sudoku");
                SSuccess result = detectSudoku(inputFrame);
                if(result == null) {
                    return null;
                }
                return result.getSolutionFrame().getSolutionMat();
            } else {
                Log.i(TAG, "Calculation in progress.");
                return inputFrame.gray();
            }
        }
    }

    public SSuccess detectSudoku(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        frameNr++;
        try {
            Pair<SSuccess, SudokuState> results = new SCandidate(frameNr, new FramePipeline(frame)).calc(Parameters.DefaultState, 8, 20, 5000L);
            currState = results.second;
            return results.first;
        } catch (SudokuException e) {
            //TODO: Create popup window to show error for user
            initPopup();
            scanButton.setVisibility(View.VISIBLE);
            return null;
        }
    }

    private void initPopup() {
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup, null);
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
        btnDismiss.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(cameraView, Gravity.CENTER, 0, 0);
            }
        });
    }
}
