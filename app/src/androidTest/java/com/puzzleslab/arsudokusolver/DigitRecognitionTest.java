package com.puzzleslab.arsudokusolver;

/**
 * Created by simonas_b on 3/31/2016.
 */
import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.puzzleslab.arsudokusolver.Activities.MainActivity;
import com.puzzleslab.arsudokusolver.Modules.FramePipeline;
import com.puzzleslab.arsudokusolver.Modules.Solution;
import com.puzzleslab.arsudokusolver.Modules.SudokuException;
import com.puzzleslab.arsudokusolver.Utils.SudokuUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@RunWith(AndroidJUnit4.class)
public class DigitRecognitionTest {
    @Test
    public void DigitRecognition() {
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
        Mat src = SudokuUtils.convertFileToMat(Environment.getExternalStorageDirectory().getAbsolutePath() + "/numbers.png", "");
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, thr, 200, 255, Imgproc.THRESH_BINARY_INV);
        thr.copyTo(con);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat sample = new Mat();
        Mat response_array = new Mat();

        Imgproc.findContours(con, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < contours.size(); i++) {
            Rect r = Imgproc.boundingRect(contours.get(i)); //Find bounding rect for each contour
            Imgproc.rectangle(src, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0,0,255),2,8,0);
            Mat ROI = thr.submat(r); // Crop the image
            Mat tmp1 = new Mat();
            Mat tmp2 = new Mat();
            Imgproc.resize(ROI, tmp1, new Size(10, 10), 0, 0, Imgproc.INTER_LINEAR); //resize to 10x10
            tmp1.convertTo(tmp2, CvType.CV_32FC1); //convert to float
            sample.push_back(tmp2.reshape(1, 1)); // Store sample data
            response_array.push_back(Imgcodecs.imdecode(new MatOfByte(numbers.get(i).byteValue()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED));
            Imgproc.rectangle(src, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 0, 255), 2, 8, 0);
        }

        Mat response = new Mat();
        Mat tmp = response_array.reshape(1,1); // make continuous
        tmp.convertTo(response, CvType.CV_32FC1); // Convert to float

        byte[] sampleBytes = new byte[(int) (sample.total() * sample.channels())];
        byte[] responseBytes = new byte[(int) (response.total() * response.channels())];

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "data.yml");
            fos.write(sampleBytes);
            fos.close();
            fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "label.yml");
            fos.write(responseBytes);
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Test
    public void testPaintToSolution() throws SudokuException {
        Mat frame = SudokuUtils.convertFileToMat(Environment.getExternalStorageDirectory().getAbsolutePath() + "/unsolvedSudoku.png", "");
        Mat solution = new Solution(new FramePipeline(frame), activityTestRule.getActivity().getBaseContext()).calculate();
    }
}
