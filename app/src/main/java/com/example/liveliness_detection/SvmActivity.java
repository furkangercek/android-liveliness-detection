package com.example.liveliness_detection;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.utils.Converters;

public class SvmActivity extends AppCompatActivity {
    static final String LOG_TAG = "LibSVMApp";
    private ImageView imgView,imgViewPredict;
    private TextView txtView;
    private Button button;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svm);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final Context env = this.getApplicationContext();
        imgView = findViewById(R.id.imageView4);
        imgViewPredict = findViewById(R.id.imageView6);
        txtView = findViewById(R.id.textView);
        button = findViewById(R.id.btn);


        String systemPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        String appFolderPath = systemPath + "libsvm/";
        String predictFile = appFolderPath + "predict.png";
        String lbpFile = appFolderPath + "lbpImage.png";
        String modelPath = appFolderPath + "model.xml";
        String testImagePath = appFolderPath + "00331.png";

        //Reading the detected and cropped face, converting type and applying LBP.
        Mat image = Highgui.imread(predictFile);
        //Mat image = Highgui.imread(testImagePath);

        Imgproc.cvtColor(image,image,Imgproc.COLOR_BGR2GRAY);
        image.convertTo(image, CvType.CV_32FC1); // New line added.

        Mat lbpImage2 = convertLBP(image);
        Highgui.imwrite( lbpFile, lbpImage2);

        //Initilization of SVM for OpenCV 2.4
        CvSVM mySVM1 = new CvSVM();
        Log.d(LOG_TAG,"PATH : " + modelPath);
        mySVM1.load(modelPath);
        Log.d(LOG_TAG,"Model is loaded succesfully");

        //Getting the features from the LBP image, converting it to one dimensional vector to predict.
        ArrayList<Integer> features = getFeatures(lbpImage2);
        Mat mymat = Converters.vector_int_to_Mat(features);

        mymat.convertTo(mymat, CvType.CV_32FC1); // New line added.
        float predictValue = mySVM1.predict(mymat);
        Log.d(LOG_TAG,"PredictValue is :" + predictValue);

        Bitmap myBitmap = BitmapFactory.decodeFile(lbpFile);
        imgView.setImageBitmap(myBitmap);
        if(predictValue > 0){
            imgViewPredict.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            txtView.setText("This is a real face.");
        }
        else{
            imgViewPredict.setImageDrawable(getResources().getDrawable(R.drawable.no));
            txtView.setText("This is a fake face.");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent testIntent = new Intent(env, MainActivity.class);
                startActivity(testIntent);
            }
        });

    }

    private Mat convertLBP(Mat input){
        int rows = input.rows();
        int cols = input.cols();

        Mat output = new Mat( rows , cols, CvType.CV_32F );

        double center[];
        double index[];
        int[] BIT = new int[8];

        for (int m = 0; m<(rows - 2); m++){
            for (int n = 0; n<(cols - 2); n++){
                center = input.get(m + 1, n + 1);

                index = input.get(m, n);
                if ( index[0] > center[0])
                    BIT[0] = 1; else BIT[0] = 0;

                index = input.get(m, n + 1);
                if ( index[0] >= center[0])
                    BIT[1] = 1; else BIT[1] = 0;

                index = input.get(m, n + 2);
                if (index[0] >= center[0])
                    BIT[2] = 1; else BIT[2] = 0;

                index = input.get(m + 1, n + 2);
                if (index[0] >= center[0])
                    BIT[3] = 1; else BIT[3] = 0;

                index = input.get(m + 2, n + 2);
                if (index[0] >= center[0])
                    BIT[4] = 1; else BIT[4] = 0;

                index = input.get(m + 2, n + 1);
                if (index[0] >= center[0])
                    BIT[5] = 1; else BIT[5] = 0;

                index = input.get(m + 2, n);
                if (index[0] >= center[0])
                    BIT[6] = 1; else BIT[6] = 0;

                index = input.get(m + 1, n);
                if (index[0] >= center[0])
                    BIT[7] = 1; else BIT[7] = 0;

                float data[] = new float[4];
                float data_s = (float)(BIT[7] + 2 * BIT[6] + 4 * BIT[5] + 8 * BIT[4] + 16 * BIT[3] + 32 * BIT[2] + 64 * BIT[1] + 128 * BIT[0]);
                data[0] = data_s;
                data[1] = data_s;
                data[2] = data_s;
                data[3] = data_s;
                output.put(m, n, data );
            }
        }
        return output;
    }

    private ArrayList<Integer> initHistogram(){
        ArrayList<Integer> bins = new ArrayList<Integer>(Collections.nCopies(59, 0));
        int[] BIT = new int[8];
        BIT[0] = 0b00000001; //0x1;
        BIT[1] = 0b00000010; //0x2;
        BIT[2] = 0b00000100; //0x4;
        BIT[3] = 0b00001000; //0x8;
        BIT[4] = 0b00010000; //0x10;
        BIT[5] = 0b00100000; //0x20;
        BIT[6] = 0b01000000; //0x40;
        BIT[7] = 0b10000000; //0x80;

        int[] val = new int[8];
        int index = 0;
        int cont = 0;

        for (int i = 0; i < 256; i++){
            for (int k = 0; k < 8; k++){
                if ((i & BIT[k]) != 0)
                //if (i & BIT[k])
                    val[k] = 1;
                else
                    val[k] = 0;
            }

            if (val[7] != val[0]){ cont++; }
            if (val[0] != val[1]){ cont++; }
            if (val[1] != val[2]){ cont++; }
            if (val[2] != val[3]){ cont++; }
            if (val[3] != val[4]){ cont++; }
            if (val[4] != val[5]){ cont++; }
            if (val[5] != val[6]){ cont++; }
            if (val[6] != val[7]){ cont++; }

            if (index < 59){
                if (cont < 3){
                    bins.set(index, i);
                    //bins[index] = i;
                    Log.d("bins:","bins["+ index + "]" + " = " + i);
                    //cout << "bins[" << index << "] = " << i << endl;
                    index++;
                }
            }
            cont = 0;
        }
        Log.d(LOG_TAG,"RETURNING BINS");

        return bins;
    }

    ArrayList<Integer> histLBP(Mat input, ArrayList<Integer> bins){
        ArrayList<Integer> hist = new ArrayList<Integer>(Collections.nCopies(59, 0));
        boolean isUniform = false;
        double pixel[];
        for (int i = 0; i < input.rows(); i++){
            for (int j = 0; j < input.cols(); j++){
                pixel = input.get(i, j);
                for (int k = 0; k < 58; k++){
                    if (pixel[0] == bins.get(k)){
                        //hist[k]++;;
                        Integer value = hist.get(k); // get value
                        value = value + 1; // increment value
                        hist.set(k, value); // replace value
                        isUniform = true;
                    }
                }
                if (!isUniform){
                    Integer value = hist.get(58); // get value
                    value = value + 1; // increment value
                    hist.set(58, value); // replace value
                }
                isUniform = false;
            }
        }

        return hist;
    }

    ArrayList<Integer> getFeatures(Mat imLBP){
        int m = imLBP.rows() / 2;
        int n = imLBP.cols() / 2;

        ArrayList<Integer> bins = initHistogram();


        Mat quad1 = new Mat(m, n , CvType.CV_32FC1);
        Mat quad2 = new Mat(m, n , CvType.CV_32FC1);
        Mat quad3 = new Mat(m, n , CvType.CV_32FC1);
        Mat quad4 = new Mat(m, n , CvType.CV_32FC1);


        Log.d(LOG_TAG,"Mat_Quad type : " + quad1.type() + "imLBP_type : " + imLBP.type());


        for (int i = 0; i < m; i++){
            for (int j = 0; j < n; j++){
                quad1.put(i, j,imLBP.get(i, j));
                quad2.put(i, j,imLBP.get(i, j+n));
                quad3.put(i, j,imLBP.get(i+m, j));
                quad4.put(i, j,imLBP.get(i+m, j+n));
            }
        }

        ArrayList<Integer> histLBP_1 = histLBP(quad1, bins);
        ArrayList<Integer> histLBP_2 = histLBP(quad2, bins);
        ArrayList<Integer> histLBP_3 = histLBP(quad3, bins);
        ArrayList<Integer> histLBP_4 = histLBP(quad3, bins);

        ArrayList<Integer> feature = new ArrayList<Integer>(histLBP_1.size() * 4 );

        feature.addAll(histLBP_1);
        feature.addAll(histLBP_2);
        feature.addAll(histLBP_3);
        feature.addAll(histLBP_4);


        Log.v("quarter ", "");
        for (int i : histLBP_1){
            String member = Integer.toString(i);
            Log.v("quarter val: ", member);

        }

        Log.d("quarter ", "");

        for (int i : histLBP_2){
            String member = Integer.toString(i);
            Log.d("quarter val: ", member);

        }

        Log.e("quarter ", "");

        for (int i : histLBP_3){
            String member = Integer.toString(i);
            Log.e("quarter val: ", member);

        }

        Log.w("quarter ", "");
        for (int i : histLBP_4){
            String member = Integer.toString(i);
            Log.w("quarter val: ", member);

        }


        Log.i("TOTAL FEATURES : ", "");
        for (int i : feature){
            String member = Integer.toString(i);
            Log.i("Member: ", member);

        }




        return feature;


    }

}


