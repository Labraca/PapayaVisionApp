package com.example.papayavision.regUtilities;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.room.Room;

import com.example.papayavision.DBUtilities.AppDatabase;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.opencv.core.CvType.CV_8U;

public class OpenCVModuleMT {
    private static Scalar YELLOW = new Scalar(30,19,25) ;
    private static Scalar GREEN = new Scalar(130,80,80);
    private static Scalar STAGE4 = new Scalar(50,19,25);
    private static Scalar STAGE2 = new Scalar(85,80,80);
    private static Scalar STAGE2m = new Scalar(87,19,25);
    private static Scalar STAGE4m = new Scalar(48,80,30);

    private static final ExecutorService cvExecutor = Executors.newCachedThreadPool();
    private static OpenCVModuleMT CV;
    private static float TotalPxPapayas;
    private static Context context;
    private OpenCVModuleMT(Context context){
        this.context = context;
    }

    public static OpenCVModuleMT getOpenCVInstance(Context context){
        if (CV == null) {
            synchronized (OpenCVModuleMT.class) {
                if (CV == null) {
                    CV = new OpenCVModuleMT(context);
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

        //Recortamos solo la zona que queremos analizar
        img = cropImage(img);

        Mat hsv_img = new Mat();
        //Pasamos al espacio de colorHSV
        Imgproc.cvtColor(img,hsv_img,Imgproc.COLOR_BGR2HSV);

        // Rango de colores para ser considerado papaya
        Scalar green = parseHSV2OpenHSV(GREEN);
        Scalar yellow = parseHSV2OpenHSV(YELLOW);

        //Rango de colores para ser considerado enviable
        Scalar stage2 = parseHSV2OpenHSV(STAGE2);
        Scalar stage4 = parseHSV2OpenHSV(STAGE4);

        Scalar stage2m = parseHSV2OpenHSV(STAGE2m);
        Scalar stage4m = parseHSV2OpenHSV(STAGE4m);

        // Filtramos los valores para identificar pixeles de papaya
        Mat papayas = new Mat();
        Core.inRange(hsv_img,yellow,green,papayas);
        //Comprobamos que no es una matriz muy vacia
        TotalPxPapayas = Core.countNonZero(papayas);
        if (TotalPxPapayas == 0){
            return foto;
        }

        //Core.inRange(hsv_img,stage4,stage2,enviablesMask);
        Future<Float> enviablesMask =
                cvExecutor.submit(new GetDiffMaskInHSVandMedia(hsv_img,stage4,stage2));

        //Filtramos para saber cuales seran inmaduras;
        Future<Float> inmadurasMask =
                cvExecutor.submit(new GetDiffMaskInHSVandMedia(hsv_img,stage2m,green));

        //Filtramos para saber cuales seran maduras;
        Future<Float> madurasMask =
                cvExecutor.submit(new GetDiffMaskInHSVandMedia(hsv_img,yellow,stage4m));

        // APARTIR DE AQUI ES SINCRONO
        float perEnviables = 0;
        float perInmaduras = 0;
        float perMaduras = 0;
        try {
            perInmaduras = inmadurasMask.get();
            perEnviables = enviablesMask.get();
            perMaduras = madurasMask.get();
        }catch (Exception e){
            e.printStackTrace();
        }

        //Asignamos valores a la foto y la enviamos
        foto.setPerm25(perEnviables);
        foto.setPer25_33(perInmaduras);
        foto.setPer33_50(perMaduras);
       // foto.setPer50_70();
        //foto.setPer70();

        return foto;

    }
    private static Mat cropImage(Mat img){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        double displayH = displayMetrics.heightPixels;
        double displayW = displayMetrics.widthPixels;

        int w = img.width();
        int h = img.height();
        int width =(int)(h*(displayW/displayH));

        int x0 = w/2;
        int y0 = (h/2)-(h/8);
        int dx = width/3;
        int dy = h/3;

        Mat roi = img.submat(y0-dy,y0+dy,x0-dx,x0+dx);
        return roi;
    }
    private Scalar parseHSV2OpenHSV(Scalar color){
        double h = color.val[0]*179/360;
        double s = color.val[1]*255/100;
        double v = color.val[2]*255/100;

        return new Scalar(h,s,v);
    }
    private static Scalar[] putRole(Scalar low, Scalar up){
        low.val[1] = 48;
        low.val[2] = 63;

        up.val[1] = 204;
        up.val[2] = 204;

        Scalar[] result = {low,up};
        return result;
    }
    private float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    public static Mat getInrange(Mat src,Scalar lower,Scalar upper,Mat dst) {

        Core.inRange(src,lower,upper,dst);

        return dst;
    }

    private class GetDiffMaskInHSVandMedia implements Callable<Float> {

        private Mat src;
        private Scalar low;
        private Scalar up;
        public GetDiffMaskInHSVandMedia(Mat src,
                                    Scalar low,Scalar up) {
            this.src = src;
            this.low = low;
            this.up = up;

        }

        @Override
        public Float call(){
            Scalar[] umbrales = putRole(low,up);

            Mat AcopSrc = new Mat();
            AcopSrc = getInrange(src,umbrales[0],umbrales[1],AcopSrc);

            float pxRangos = Core.countNonZero(AcopSrc);

            float porcentaje = round((pxRangos/TotalPxPapayas)*100,2);

            return porcentaje;
        }



    }
}
