package com.example.papayavision.regUtilities;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.papayavision.entidades.Foto;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.Manifest;

import static org.opencv.core.CvType.CV_8U;

public class OpenCVModule {
    private static Scalar YELLOW = new Scalar(30,19,25) ;
    private static Scalar GREEN = new Scalar(130,80,90);
    private static Scalar STAGE4 = new Scalar(60,30,30);
    private static Scalar STAGE2 = new Scalar(85,80,80);
    private static Scalar STAGE3 = new Scalar(73,19,25);
    private static Scalar STAGE5 = new Scalar(50,19,25);
    private static Scalar STAGE6 = new Scalar(43,19,23);
    private static final ExecutorService cvExecutor = Executors.newCachedThreadPool();
    private static OpenCVModule CV;
    private static Context context;
    private OpenCVModule(Context context){
        this.context = context;
    }

    public static OpenCVModule getOpenCVInstance(Context context){
        if (CV == null) {
            synchronized (OpenCVModuleMT.class) {
                if (CV == null) {
                    CV = new OpenCVModule(context);
                }
            }
        }
        return CV;
    }

    public Foto calculatePercents(File file){
        Foto foto = new Foto();
        //Leemos la imagen
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat img = imageCodecs.imread(file.getAbsolutePath());
        if (img == null){
            Log.e("CV","No se pudo leer la imagen");
            return foto;
        }
        img = cropImage(img);

        Mat hsv_img = new Mat();
        //Pasamos al espacio de colorHSV
        Imgproc.cvtColor(img,hsv_img,Imgproc.COLOR_BGR2HSV);

        // Rango de colores para ser considerado papaya
        Scalar green = parseHSV2OpenHSV(GREEN);
        Scalar yellow = parseHSV2OpenHSV(YELLOW);
        Scalar stage2 = parseHSV2OpenHSV(STAGE2);
        Scalar stage4 = parseHSV2OpenHSV(STAGE4);
        Scalar stage3 = parseHSV2OpenHSV(STAGE3);
        Scalar stage5 = parseHSV2OpenHSV(STAGE5);
        Scalar stage6 = parseHSV2OpenHSV(STAGE6);

        Scalar[] stages = {green,stage2,stage3,stage4,stage5,stage6,yellow};
        double[] percents;
        percents = getdataFromMatThreads(hsv_img,stages);

        //Asignamos valores a la foto y la enviamos
        foto.setPerm25(Float.parseFloat(""+percents[0]));
        foto.setPer25_33(Float.parseFloat(""+percents[1]));
        foto.setPer33_50(Float.parseFloat(""+percents[2]));
        foto.setPer50_70(Float.parseFloat(""+percents[3]));
        foto.setPer70((Float.parseFloat(""+percents[4])));

        return foto;

    }
    private Mat cropImage(Mat img){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        double displayH = displayMetrics.heightPixels;
        double displayW = displayMetrics.widthPixels;

        int w = img.width();
        int h = img.height();
        int width = (int)(h*(displayW/displayH));

        //center
        int x0 = w/2;
        int y0 = (h/2)-(h/8);

        //padding
        int dx = width/3;
        int dy = h/3;

        Mat roi = img.submat(y0-dy,y0+dy,x0-dx,x0+dx);
        return roi;
    }
    private static Scalar parseHSV2OpenHSV(Scalar color){
        double h = color.val[0]*179/360;
        double s = color.val[1]*255/100;
        double v = color.val[2]*255/100;

        return new Scalar(h,s,v);
    }
    private static Scalar[] putRole(Scalar low, Scalar up){
        low.val[1] = 53;
        low.val[2] = 63;

        up.val[1] = 204;
        up.val[2] = 204;

        Scalar[] result = {low,up};
        return result;
    }
    private static double getMeanOfMask(Mat mask){
        //divido los canales
        ArrayList<Mat> canales = new ArrayList<>(3);
        Core.split(mask,canales);
        Mat hue = canales.get(0);

        double mean = minMax(hue);

        return mean;
    }
    private static double minMax(Mat hue){
        double min=Double.MAX_VALUE;
        double max = 0;

        for(int i=0;i<hue.width();i++){
            for(int j=0;j<hue.height();j++){
                double hueVal = hue.get(j,i)[0];//solo tiene un canal
                if (hueVal != 0) {
                    if (hueVal < min)
                        min = hueVal;
                    if (hueVal > max)
                        max = hueVal;
                }
            }
        }

        return (double)(min+max)/2;
    }
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    private static double[] getdataFromMatThreads(Mat img, Scalar[] stages){
        int batch = 2;

        int heightpos = (img.height()/batch) - 1;
        ArrayList<Future<double[]>> submats = new ArrayList<>();

        int pos=0;
        for(int i = 0;i<batch;i++){
            Mat submat = img.submat(pos,pos += heightpos,0,img.width());
            pos++;

            Future<double[]> futurePercent =
                    cvExecutor.submit(new calculatePercent(submat,stages));
            submats.add(futurePercent);
        }

        double[] percents = {0,0,0,0,0};
        for(int i = 0;i<batch; i++){
            for(int j = 0;j<percents.length;j++){

                try {
                    percents[j] += submats.get(i).get()[j];
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        return percents;
    }

    private static class calculatePercent implements Callable<double[]>{

        Mat img;
        Scalar[] stages;
        public calculatePercent(Mat img,Scalar[] stages){
            this.img=img;
            this.stages = stages;
        }
        @Override
        public double[] call() throws Exception {
            return getDataFromMat(this.img,this.stages);
        }
    }
    private static double[] getDataFromMat(Mat img, Scalar[] stages){

        double minS = stages[stages.length-1].val[1];
        double minV = stages[stages.length-1].val[2];
        double maxS = 204.0;
        double maxV = 204.0;
        double sum= 0.0;
        double Pxm25 = 0.0;
        double Px25_33 = 0.0;
        double Px33_50 = 0.0;
        double Px50_70 = 0.0;
        double Px70 = 0.0;

        for(int i = 0; i < img.height(); i++) {
            for(int j = 0;j<img.width();j++){
                double[] px = img.get(i,j);
                if(!(px[1]<minS || px[1]> maxS || px[2]<minV || px[2]<maxV)){
                    if((stages[0].val[0] >= px[0]) && (px[0] > stages[1].val[0])){
                        sum++;
                        Pxm25++;
                    }else if ((stages[1].val[0] >= px[0]) && (px[0] > stages[3].val[0])){
                        sum++;
                        Px25_33++;
                    }else if ((stages[3].val[0] >= px[0]) && (px[0] > stages[4].val[0])){
                        sum++;
                        Px33_50++;
                    }else if ((stages[4].val[0] >= px[0]) && (px[0] > stages[5].val[0])){
                        sum++;
                        Px50_70++;
                    }else if ((stages[5].val[0] >= px[0]) && (px[0] > stages[6].val[0])){
                        sum++;
                        Px70++;
                    }
                }
            }
        }
        if(sum == 0)
            return new double[]{0,0,0,0,0};

        double perm25 = Pxm25/sum;
        double per25_33 = Px25_33/sum;
        double per33_50 = Px33_50/sum;
        double per50_70 = Px50_70/sum;
        double per70 = Px70/sum;

        double[] porcentajes = {perm25,per25_33,per33_50,per50_70,per70};

        return  porcentajes;
    }
}
