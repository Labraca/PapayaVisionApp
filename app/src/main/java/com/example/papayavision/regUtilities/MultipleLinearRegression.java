package com.example.papayavision.regUtilities;

import android.content.Context;
import android.util.Pair;

import androidx.room.Room;

import com.example.papayavision.DBUtilities.AppDatabase;
import com.example.papayavision.DBUtilities.QueryPreferencias;
import com.example.papayavision.DBUtilities.RegRepository;
import com.example.papayavision.entidades.Registro;
import com.google.common.collect.Lists;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MultipleLinearRegression {
    private double[] pesos = new double[8];
    private double bias = 0.0;
    private static MultipleLinearRegression INSTANCIA;

    private MultipleLinearRegression(Context context){
        leerPesosPref(context);
    }

    public static MultipleLinearRegression getINSTANCIA(Context context){
        if (INSTANCIA == null) {
            synchronized (MultipleLinearRegression.class) {
                if (INSTANCIA == null) {
                    INSTANCIA = new MultipleLinearRegression(context);
                }
            }
        }
        return INSTANCIA;
    }

    //forwardPropogate
    public int calcularVolumenEstimado(double[] datos){
        double[] x = this.pesos;
        double[] y = datos;
        if (x.length != y.length)
            throw new RuntimeException("Arrays must be same size");
        double sum = 0;
        for (int i = 0; i < x.length; i++)
            sum += x[i] * y[i];
        return (int)(sum + this.bias);
    }

    private Pair<Double[],Double> calculateGradients(double[] inputs, double predY,double targetY){

        double dJ_dPred = meanSquaredErrorDerivative(predY, targetY); //MSE'
        double[] dPred_dW = inputs; //X1...X8
        double[] dJ_dW = multiplyScalar(dPred_dW,dJ_dPred); //MSE'· X
        double dJ_dB = dJ_dPred;
        Double[] result = new Double[8];
        int j = 0;
        for (double i:dJ_dW){
            Double x = new Double(i);
            result[j++] = x;
        }

        Double x = new Double(dJ_dB);
        Pair<Double[],Double> weightBias = new Pair<>(result,x);

        return weightBias;
     }
    public void optimizeParameters(ArrayList<Pair<Double[],Double>> gradients, double learningRate){
        ArrayList<Double[]> weightGradientsList = new ArrayList<>();
        ArrayList<Double> biasGradientsList = new ArrayList<>();

        for(Pair<Double[],Double> i : gradients){
            Double[] x = i.first;
            weightGradientsList.add(x);
            Double b = i.second;
            biasGradientsList.add(b);
        }

        ArrayList<Double> weightGradients = multidimMean(weightGradientsList);
        double biasGradients = calculateAverage(biasGradientsList);

        this.pesos = substract(this.pesos,multiplyScalar(weightGradients,learningRate));
        this.bias = this.bias - (biasGradients*learningRate);

    }

    public void fit (ArrayList<double[]> x,double[] y, int epoch,int batchSize,Context context){
        List<List<Pair<double[],Double>>> batches = batch(x,y,batchSize);
        for (int i = 0;i < epoch;i++){
            for (List<Pair<double[],Double>> batch: batches){
                ArrayList<Pair<Double[],Double>> gradients = new ArrayList<>();
                for(Pair<double[],Double> pair:batch){
                    int prediction = calcularVolumenEstimado(pair.first);
                    Pair<Double[],Double> gradientes = calculateGradients(pair.first,prediction,pair.second);
                    //Double[] data=new Double[gradientes.size()];
                    //data = gradientes.toArray(data);
                    gradients.add(gradientes);
                }
                optimizeParameters(gradients,0.00009);
            }
        }
        QueryPreferencias.guardarPesos(context,this.pesos,this.bias);
        leerPesosPref(context);
    }


    private List<List<Pair<double[],Double>>> batch(ArrayList<double[]> x, double[] y, int batchSize){
        if (x.size() != y.length)
            throw new RuntimeException("Arrays must be same size");
        ArrayList<Pair<double[],Double>> data = new ArrayList<>();

        for(int i = 0;i < x.size();i++){
            Pair<double[],Double> z = new Pair<>(x.get(i),y[i]);
            data.add(z);
        }
        return Lists.partition(data,batchSize);
    }
    private double[] substract(double[] a, double[] b){
        double[] difference = new double[a.length];

        for(int i = 0; i < a.length; i++){
            difference[i] = (a[i] - b[i]);
        }
        return difference;
    }
    private double calculateAverage( ArrayList<Double> marks) {
        Double sum = 0.0;
        if(!marks.isEmpty()) {
            for (Double mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }
    private ArrayList<Double> multidimMean(ArrayList<Double[]> x){
        ArrayList<Double> mean = new ArrayList<Double>();
        for(int i = 0 ; i < x.get(0).length;i++){
            double suma = 0.0;
            for(Double[] array:x){
                suma += array[i];
            }
            mean.add(suma/x.size());
        }
        return mean;
    }
    private double meanSquaredErrorDerivative (double predY,double targetY){
        return 2* (predY - targetY);
    }
    private double[] multiplyScalar(double[] array,double scalar){
        double[] x = new double[array.length];
        for(int i = 0; i < array.length; i++){
            x[i] = array[i]*scalar;
        }
        return x;
    }
    private double[] multiplyScalar(ArrayList<Double> array,double scalar){
        double[] x = new double[array.size()];
        for(int i = 0; i < array.size(); i++){
            x[i] = array.get(i)*scalar;
        }
        return x;
    }
    public void leerPesosPref(Context context){
        double[] datos = parseString2Double(QueryPreferencias.cargarPesos(context));
        for(int i = 0 ; i < this.pesos.length-1; i++){
            pesos[i] = datos[i];
        }
        this.bias = datos[datos.length-1];
    }
    private double[] parseString2Double(String[] pesos){
        double[] pesosDouble = new double[pesos.length];
        for(int i = 0; i < pesos.length;i++){
            pesosDouble[i] = Double.parseDouble(pesos[i]);
        }
        return pesosDouble;
    }
}
