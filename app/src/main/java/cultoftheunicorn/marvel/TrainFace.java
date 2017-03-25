package cultoftheunicorn.marvel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrainFace extends AppCompatActivity {

    private static final String TAG = "TrainFace";
    private CascadeClassifier cascadeClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_face);


        AppCompatButton btn = (AppCompatButton) findViewById(R.id.startCam);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            Log.i(TAG, "Image path: " + mCurrentPhotoPath);
            File img = new File(mCurrentPhotoPath);

            Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
            Log.i(TAG, "1");
            Mat imgMat = new Mat(myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8UC4);

            Log.i(TAG, "2");
            Mat greyscaleImage = new Mat(myBitmap.getHeight(), myBitmap.getWidth(), CvType.CV_8UC4);


            try {
                // Copy the resource into a temp file so OpenCV can load it
                InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                FileOutputStream os = new FileOutputStream(mCascadeFile);


                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();

                // Load the cascade classifier
                cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e("OpenCVActivity", "Error loading cascade", e);
            }

            Log.i(TAG, "3");


            Utils.bitmapToMat(myBitmap, imgMat);
            Imgproc.cvtColor(imgMat, greyscaleImage, Imgproc.COLOR_RGBA2RGB);

            Log.i(TAG, "4");


            MatOfRect faces = new MatOfRect();

            int absoluteFaceSize = (int) (greyscaleImage.height() * 0.2);


            Log.i(TAG, "5");


            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(greyscaleImage, faces, 1.1, 2, 2,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());
            }

            Log.i(TAG, "6");
            // If there are any faces found, draw a rectangle around it
            Rect[] facesArray = faces.toArray();

//            TODO Camera doesn't detect in portrait. Using landscape for now

            Log.i(TAG, facesArray[0].x + " " + facesArray[0].y + " " + facesArray[0].width +  " " + facesArray[0].height);


//            TODO detect multiple faces and give a warming message

            Rect roi = new Rect(facesArray[0].x, facesArray[0].y, facesArray[0].width, facesArray[0].height);
            Mat cropped = new Mat(greyscaleImage, roi);

            Bitmap bmp = null;
            try {
//                Imgproc.cvtColor(cropped, cropped, Imgproc.COLOR_GRAY2RGBA, 4);
                bmp = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropped, bmp);
            }
            catch (CvException e){Log.d("Exception",e.getMessage());}


            ImageView myImage = (ImageView) findViewById(R.id.imageView);
            myImage.setImageBitmap(bmp);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
