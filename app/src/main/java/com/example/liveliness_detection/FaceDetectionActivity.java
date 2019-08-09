package com.example.liveliness_detection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class FaceDetectionActivity extends AppCompatActivity {

    private static final String TAG = "FaceDetection";
    public ImageView imageView,imageView2,imageView3;
    private Button button2;
    public int faceCount;
    public int cx,cy;
    Face face;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_face_detection);

        final FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                //.setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        Intent intent = getIntent();
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        imageView3  = findViewById(R.id.imageView3);
        button2 = findViewById(R.id.button2);
        final Context env = this.getApplicationContext();
        final String mCurrentPhotoPath = intent.getStringExtra("mCurrentPhotoPath");

        Bitmap myBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        imageView.setImageBitmap(myBitmap);



        // run image related code after the view was laid out
        // to have all dimensions calculated
        imageView.post(new Runnable() {
            @Override
            public void run() {
                if (mCurrentPhotoPath != null) {
                    Log.d(TAG, "PATH NULL DEGIL:");
                    Bitmap bitmap = getBitmapFromPathForImageView(mCurrentPhotoPath, imageView);
                    imageView.setImageBitmap(bitmap);

                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Face> faces = detector.detect(frame);

                    Log.d(TAG, "Faces detected: " + String.valueOf(faces.size()));

                    Paint paint = new Paint();
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);

                    Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(mutableBitmap);

                    for (int i = 0; i < faces.size(); ++i) {
                        face = faces.valueAt(i);
                        for (Landmark landmark : face.getLandmarks()) {
                            cx = (int) (landmark.getPosition().x);
                            cy = (int) (landmark.getPosition().y);
                            canvas.drawCircle(cx, cy, 10, paint);
                        }

                        Path path = new Path();
                        path.moveTo(face.getPosition().x, face.getPosition().y);
                        path.lineTo(face.getPosition().x + face.getWidth(), face.getPosition().y);
                        path.lineTo(face.getPosition().x + face.getWidth(), face.getPosition().y + face.getHeight());
                        path.lineTo(face.getPosition().x, face.getPosition().y + face.getHeight());
                        path.close();

                        Paint redPaint = new Paint();
                        redPaint.setColor(0XFFFF0000);
                        redPaint.setStyle(Paint.Style.STROKE);
                        redPaint.setStrokeWidth(8.0f);
                        canvas.drawPath(path, redPaint);
                    }
                    faceCount = faces.size();
                    if (faceCount == 0){
                        button2.setText("TAKE ANOTHER PICTURE");
                    }

                    if (faces.size() > 0) { // Buraya for dongusu uzerinde listview ile fotolarin eklenmesi yapilacak.



                        // Cropping the image.
                        int xxx = (int) (face.getPosition().x + (0.150 * face.getWidth()));
                        if (xxx >= mutableBitmap.getWidth()) xxx = mutableBitmap.getWidth();
                        int yyy = (int) (face.getPosition().y + (0.2 * face.getWidth()));
                        if (yyy >= mutableBitmap.getHeight()) yyy = mutableBitmap.getHeight();
                        int www = (int) (face.getWidth() * 0.700);
                        while (xxx + www > mutableBitmap.getWidth() && www > 0) www--;

                        imageView.setImageBitmap(mutableBitmap);
                        Log.d(TAG, "mutableBitmap SET EDILDI" + "CX : " + face.getPosition().x + " CY : " + face.getPosition().y + "GENISLIK " + face.getWidth());
                        Log.d(TAG, "mutableBitmap SET EDILDI" + "CX : " + face.getPosition().x + " CY : " + face.getPosition().y + "GENISLIK " + face.getWidth());


                        Bitmap svmCrop = Bitmap.createBitmap(mutableBitmap, xxx, yyy, www, www);
                        imageView2.setImageBitmap(svmCrop);

                        Bitmap svmGray = toGrayscale(svmCrop);
                        imageView3.setImageBitmap(svmGray);

                        String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                        String appFolderPath = systemPath+"libsvm";
                        String fname = "predict.png" ;
                        File file = new File(appFolderPath, fname);
                        Log.i(TAG, "" + file);

                        if (file.exists())
                            file.delete();
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            svmGray.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (faceCount > 0) {
                    Intent svmIntent = new Intent(env, SvmActivity.class);
                    startActivity(svmIntent);
                }
                else {
                    Intent backIntent = new Intent(env, MainActivity.class);
                    startActivity(backIntent);

                }
                Log.d(TAG, "Starting the intent");
                Log.d(TAG, "Started the intent");
            }
        });
    }





    private Bitmap getBitmapFromPathForImageView(String mCurrentPhotoPath, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Bitmap rotatedBitmap = bitmap;

        // rotate bitmap if needed
        try {
            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

}