package com.puzzleslab.arsudokusolver.Activities;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;

import com.dropbox.core.v2.files.FileMetadata;
import com.puzzleslab.arsudokusolver.Modules.Config;
import com.puzzleslab.arsudokusolver.Modules.DropBoxClientFactory;
import com.puzzleslab.arsudokusolver.Modules.FramePipeline;
import com.puzzleslab.arsudokusolver.Modules.Solution;
import com.puzzleslab.arsudokusolver.Modules.SudokuException;
import com.puzzleslab.arsudokusolver.R;
import com.puzzleslab.arsudokusolver.Tasks.UploadFileTask;
import com.puzzleslab.arsudokusolver.Utils.Parameters;
import com.puzzleslab.arsudokusolver.Utils.SudokuUtils;
import com.puzzleslab.arsudokusolver.Views.SudokuView;

import java.text.DateFormat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String TAG = "MainActivity";

    private SudokuView cameraView;
    private Button scanButton;
    private Mat solution;
    private boolean isCameraSettingsSet = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                    Parameters.CONFIG = new Config(
                            SudokuUtils.getConfigValue(getBaseContext(), "api_url"),
                            SudokuUtils.getConfigValue(getBaseContext(), "auth_token"),
                            Boolean.valueOf(SudokuUtils.getConfigValue(getBaseContext(), "is_production")));
                    DropBoxClientFactory.init(Parameters.CONFIG.getAuthToken());
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.sudoku);

        cameraView = (SudokuView) findViewById(R.id.sudoku);
        cameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        scanButton = (Button) findViewById(R.id.button_rescan);
        verifyStoragePermissions(this);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solution != null) {
                    solution = null;
                    return;
                }
                scanButton.setVisibility(View.GONE);
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
                    Log.d(TAG, "External storage permission granted.");
                } else {
                    Log.e(TAG, "External storage permission denied.");
                }
            }
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
        if(!isCameraSettingsSet) {
            cameraView.setEffect(Camera.Parameters.FLASH_MODE_TORCH);
            isCameraSettingsSet = true;
        }
        if (solution == null) {
            if (scanButton.getVisibility() == View.GONE) {
                Log.i(TAG, "Starting to find sudoku");
                solution = detectSudoku(inputFrame);
                if (solution == null) {
                    return inputFrame.rgba();
                }
                Log.d(TAG, "Sudoku solved successfully.");
                Log.d(TAG, "Starting results uploading to Dropbox.");
                uploadFile(Parameters.EXTERNAL_STORAGE_PATH + Parameters.INITIAL_SUDOKU_FILE_NAME, "");
                uploadFile(Parameters.EXTERNAL_STORAGE_PATH + Parameters.SOLUTION_FILE_NAME, "r");
                return solution;
            }
            return inputFrame.rgba();
        }
        return solution;
    }

    public Mat detectSudoku(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        //Mat frame = SudokuUtils.convertFileToMat(Environment.getExternalStorageDirectory().getAbsolutePath() + "/unsolvedSudoku.png", "");
        Mat solution = null;
        try {
            if(!Parameters.CONFIG.isProduction()) {
                SudokuUtils.printMatToPicture(frame, "read.png");
            }
            solution = new Solution(new FramePipeline(frame), getBaseContext()).calculate();
        } catch (SudokuException e) {
            initPopup();
        } finally {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scanButton.setVisibility(View.VISIBLE);
                }
            });
        }
        return solution;
    }

    private void initPopup() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                final View popupView = layoutInflater.inflate(R.layout.popup, (ViewGroup)cameraView.getParent(), false);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

                Button btnDismiss = (Button)popupView.findViewById(R.id.button_dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                popupWindow.showAtLocation(cameraView, Gravity.CENTER, 0, 0);
            }
        });
    }

    private void uploadFile(String fileUri, String fileType) {
        new UploadFileTask(DropBoxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                Log.i(TAG, result.getName() + " size " + result.getSize() + " modified " +
                        DateFormat.getDateTimeInstance().format(result.getClientModified()));
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to upload file.", e);
            }
        }).execute(fileUri, fileType);
    }
}
