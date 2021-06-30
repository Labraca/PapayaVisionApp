package com.example.papayavision.regUtilities;

import android.content.Context;
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
    private static Scalar YELLOW = new Scalar(30,30,30) ;
    private static Scalar GREEN = new Scalar(105,80,80);
    private static Scalar STAGE4 = new Scalar(50,30,30);
    private static Scalar STAGE2 = new Scalar(85,80,80);
    private static final ExecutorService cvExecutor = Executors.newCachedThreadPool();
    private OpenCVModuleMT CV;

    private OpenCVModuleMT(){
    }

    public OpenCVModuleMT getOpenCVInstance(Context context){
        if (CV == null) {
            synchronized (OpenCVModuleMT.class) {
                if (CV == null) {
                    CV = new OpenCVModuleMT();
                }
            }
        }
        return CV;
    }

    public Foto calculatePercents(File file){
        Foto foto = new Foto();
        //Leemos la imagen
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat hsv_img = imageCodecs.imread(file.getAbsolutePath());
        if (hsv_img == null){
            Log.e("CV","No se pudo leer la imagen");
            return foto;
        }

        //Recortamos solo la zona que queremos analizar
        hsv_img = cropImage(hsv_img);

        //Pasamos al espacio de colorHSV
        Imgproc.cvtColor(hsv_img,hsv_img,Imgproc.COLOR_BGR2HSV);

        // Rango de colores para ser considerado papaya
        Scalar green = parseHSV2OpenHSV(GREEN);
        Scalar yellow = parseHSV2OpenHSV(YELLOW);

        //Rango de colores para ser considerado enviable
        Scalar stage2 = parseHSV2OpenHSV(STAGE2);
        Scalar stage4 = parseHSV2OpenHSV(STAGE4);

        // Filtramos los valores para identificar pixeles de papaya
        Mat papayas = new Mat();
        Core.inRange(hsv_img,yellow,green,papayas); //TODO: IGUAL NO HAY PAPAYAS
        //Comprobamos que no es una matriz muy vacia
        float totalPxPapayas = Core.countNonZero(papayas);
        float totalPx = hsv_img.width()*hsv_img.height();
        if (totalPxPapayas < 0.3*totalPx){
            return foto;
        }

        //Core.inRange(hsv_img,stage4,stage2,enviablesMask);
        Future<Mat> enviablesMask =
                cvExecutor.submit(new GenerateMaskCallable(hsv_img,stage4,stage2,new Mat(),
                        GenerateMaskCallable.IN_RANGE));

        //Filtramos para saber cuales seran inmaduras;
        Future<Mat> inmadurasMask =
                cvExecutor.submit(new GenerateMaskCallable(hsv_img,stage2,green,new Mat(),
                        GenerateMaskCallable.IN_RANGE));

        //Filtramos para saber cuales seran maduras;
        Future<Mat> madurasMask =
                cvExecutor.submit(new GenerateMaskCallable(hsv_img,yellow,stage4,new Mat(),
                GenerateMaskCallable.IN_RANGE));

        //Filtramos los valores acoplados entre las Enviables y las Inmaduras
        Mat inmadurasMaskInst = new Mat();
        Mat enviablesMaskInst = new Mat();

        try {
            inmadurasMaskInst = inmadurasMask.get();
            enviablesMaskInst = enviablesMask.get();
        }catch (Exception e){
            e.printStackTrace();
        }

        Future<Mat> entreIyE =
                cvExecutor.submit(new GenerateMaskCallable(enviablesMaskInst,null,null,inmadurasMaskInst,
                        GenerateMaskCallable.BITWISE_AND));
        //Core.bitwise_and(enviablesMask.get(),enviablesMask.get(),entreIyE,inmadurasMask);
        Mat HSVentreIyEInst = new Mat();
        Mat entreIyEInst = new Mat();

        try{
            entreIyEInst = entreIyE.get();
        }catch (Exception e){
            e.printStackTrace();
        }

        //Core.bitwise_and(hsv_img,hsv_img,HSVentreIyE,entreIyE);
        Future<Mat> HSVentreIyE =
                cvExecutor.submit(new GenerateMaskCallable(hsv_img,null,null,entreIyEInst,
                        GenerateMaskCallable.BITWISE_AND));

        //Filtramos los valores acoplados entre las Enviables y las Maduras
        Mat entreEyM = new Mat();
        Mat madurasMaskInst = new Mat();
        try {
            madurasMaskInst = inmadurasMask.get();
            HSVentreIyEInst = HSVentreIyE.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        Core.bitwise_and(enviablesMaskInst,enviablesMaskInst,entreEyM,madurasMaskInst);
        Mat HSVentreEyM = new Mat();
        Core.bitwise_and(hsv_img,hsv_img,HSVentreEyM,entreEyM);

        //Obtenemos el valor medio de los acoplados
        double meanIyE =  getMeanOfMask(HSVentreIyEInst);
        double meanEyM = getMeanOfMask(HSVentreEyM);

        //Hacemos una mascara solo de la mitad de los acoplados en el rango de las Inmaduras y las Maduras
        Mat acopInm = new Mat();
        Core.inRange(hsv_img,new Scalar(meanIyE,74,74),green,entreIyEInst);
        Mat acopMad = new Mat();
        Core.inRange(hsv_img,yellow,new Scalar(meanEyM,204,204),entreEyM);

        //Calculo de los pixeles de cada zona
        float pxAcEntreIyE = Core.countNonZero(entreIyEInst);
        float pxAcEntreEyM = Core.countNonZero(entreEyM);
        float pxAcEnI = Core.countNonZero(acopInm);
        float pxAcEnM = Core.countNonZero(acopMad);

        float totalPxInmaduras = Core.countNonZero(inmadurasMaskInst) - (pxAcEntreIyE - pxAcEnI);
        float totalPxMaduras = Core.countNonZero(madurasMaskInst) - (pxAcEntreEyM - pxAcEnM);
        // Si los totales anteriores dan 0 significa que los pixeles corresponden enteramente a los enviables
        if (totalPxInmaduras == 0)
            pxAcEnI = 0;
        if (totalPxMaduras == 0)
            pxAcEnM = 0;

        float totalPxEnviables = Core.countNonZero(enviablesMaskInst) - pxAcEnI - pxAcEnM;

        //calculo de los porcentajes
        float perEnviables = round((totalPxEnviables/totalPxPapayas)*100,2);
        float perInmaduras = round((totalPxInmaduras/totalPxPapayas)*100,2);
        float perMaduras = round((totalPxMaduras/totalPxPapayas)*100,2);

        //Asignamos valores a la foto y la enviamos
        foto.setPerEnvio(perEnviables);
        foto.setPerInmadura(perInmaduras);
        foto.setPerInmadura(perMaduras);

        return foto;

    }
    private static Mat cropImage(Mat img){
        int w = img.width();
        int h = img.height();

        int x0 = w/2;
        int y0 = (h/2)-(h/8);
        int dx = w/3;
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
        low.val[1] = 76;
        low.val[2] = 76;

        up.val[1] = 204;
        up.val[2] = 204;

        Scalar[] result = {low,up};
        return result;
    }
    private double getMeanOfMask(Mat mask){
        //divido los canales
        ArrayList<Mat> canales = new ArrayList<>(3);
        Core.split(mask,canales);
        Mat hue = canales.get(0);

        double mean = minMax(hue);

        return mean;
    }
    private double minMax(Mat hue){
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
    private float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    public static Mat getInrange(Mat src,Scalar lower,Scalar upper,Mat dst) {

        Core.inRange(src,lower,upper,dst);

        return dst;
    }
    private static Mat getBitwise_and(Mat src, Mat mask) {
        Mat dst = new Mat();
        Core.bitwise_and(src,mask,dst);
        return dst;
    }
    private static class GenerateMaskCallable implements Callable<Mat> {

        Mat src,dst;
        Scalar lower,upper;
        int op;
        public static final int IN_RANGE = 0;
        public static final int BITWISE_AND = 1;
        public GenerateMaskCallable(Mat src,Scalar lower,Scalar upper,Mat dst,int op) {
            this.src = src;
            this.dst = dst;
            Scalar[] thresh = putRole(lower,upper);
            this.lower = thresh[0];
            this.upper = thresh[1];
            this.op = op;
        }

        @Override
        public Mat call() throws Exception {
            if(op == IN_RANGE)
                return getInrange(src,lower,upper,dst);
            else if(op == BITWISE_AND)
                return getBitwise_and(src,dst);
            else
                return dst;
        }



    }
}
