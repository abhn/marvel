package cultoftheunicorn.marvel;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.MatVector;

import android.graphics.Bitmap;
import android.util.Log;

public class PersonRecognizer {

    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    private static final String TAG = "PersonRecognizer";

    private FaceRecognizer faceRecognizer;
    private String mPath;
    private int count = 0;
    private Labels labelsFile;
    private int mProb = 999;

    public PersonRecognizer(String path) {
        faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(2, 8, 8, 8, 200);
        mPath = path;
        labelsFile = new Labels(mPath);
    }

    public void add(Mat m, String description) {
        Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        bmp = Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

        try (FileOutputStream f = new FileOutputStream(mPath + description + "-" + count + ".jpg", true)) {
            count++;
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);
        } catch (Exception e) {
            Log.e(TAG, "Error saving image: " + e.getMessage(), e);
        }
    }

    public boolean train() {
        File root = new File(mPath);
        File[] imageFiles = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        });

        if (imageFiles == null || imageFiles.length == 0) {
            Log.e(TAG, "No images found for training.");
            return false;
        }

        MatVector images = new MatVector(imageFiles.length);
        int[] labels = new int[imageFiles.length];

        int counter = 0;

        for (File image : imageFiles) {
            String p = image.getAbsolutePath();
            IplImage img = cvLoadImage(p);

            if (img == null) {
                Log.e(TAG, "Error loading image: " + p);
                continue;
            }

            Log.i(TAG, "Processing image: " + p);
            String description = extractDescription(p);
            int label = getOrCreateLabel(description);

            IplImage grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
            cvCvtColor(img, grayImg, CV_BGR2GRAY);

            images.put(counter, grayImg);
            labels[counter] = label;
            counter++;
        }

        if (counter > 0 && labelsFile.max() > 1) {
            faceRecognizer.train(images, labels);
            labelsFile.Save();
            return true;
        }
        return false;
    }

    public boolean canPredict() {
        return labelsFile.max() > 1;
    }

    public String predict(Mat m) {
        if (!canPredict()) {
            return "";
        }

        int[] n = new int[1];
        double[] p = new double[1];
        IplImage ipl = matToIplImage(m, WIDTH, HEIGHT);
        faceRecognizer.predict(ipl, n, p);

        mProb = (n[0] != -1) ? (int) p[0] : -1;
        return (n[0] != -1) ? labelsFile.get(n
