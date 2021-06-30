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
    private double[] pesos = new double[6];
    private double bias = 0.0;
    private MultipleLinearRegression INSTANCIA;
    private MultipleLinearRegression(Context context){
        leerPesosPref(context);
    }
    public MultipleLinearRegression getINSTANCIA(Context context){
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

    private ArrayList<Double> calculateGradients(double[] inputs, double predY,double targetY){
        double dJ_dPred = meanSquaredErrorDerivative(predY, targetY);
        double[] dPred_dW = inputs;
        double[] dJ_dW = multiplyScalar(dPred_dW,dJ_dPred);
        double dJ_dB = dJ_dPred;
        return new ArrayList<Double>((Collection<? extends Double>) Arrays.asList(dJ_dW, dJ_dB));
     }
    public void optimizeParameters(ArrayList<Double[]> gradients, double learningRate){
        ArrayList<double[]> weightGradientsList = new ArrayList<double[]>();

        for(Double[] gradient:gradients){
            double[] x = {gradient[0]};
            weightGradientsList.add(x);
        }
        ArrayList<Double> weightGradients = multidimMean(weightGradientsList);

        ArrayList<Double> biasGradientsList = new ArrayList<>();
        for(Double[] gradient:gradients){
            double x = gradient[1];
            biasGradientsList.add(x);
        }
        double biasGradients = calculateAverage(biasGradientsList);
        this.pesos = substract(this.pesos,multiplyScalar(weightGradients,learningRate));
        this.bias = this.bias - (biasGradients*learningRate);

    }

    /*
     fit
     @param x: Array de Arrays que contiene los datos de lo que serian los registros
     @param y: Array conteniendo los volumenes conseguidos de esos registros
     @param epochs: Cantidad de registros(?)
     @param batchSize: es el numero de features que tiene el modelo(?)
     */
    public void fit (ArrayList<double[]> x,double[] y, int epoch,int batchSize,Context context){
        List<List<Pair<double[],Double>>> batches = batch(x,y,batchSize);
        for (int i = 0;i < epoch;i++){
            for (List<Pair<double[],Double>> batch: batches){
                ArrayList<Double[]> gradients = new ArrayList<>();
                for(Pair<double[],Double> pair:batch){
                    int prediction = calcularVolumenEstimado(pair.first);
                    ArrayList<Double> gradientes = calculateGradients(pair.first,prediction,pair.second);
                    Double[] data=new Double[gradientes.size()];
                    data = gradientes.toArray(data);
                    gradients.add(data);
                }
                optimizeParameters(gradients,0.01);
            }
        }
        QueryPreferencias.guardarPesos(context,this.pesos,this.bias);
    }
    //batch
    //@param x: Array de Arrays que contiene los datos de lo que serian los registros
    //@param y: Array conteniendo los volumenes conseguidos de esos registros
    //@param batchSize: es el numero de features que tiene el modelo(?)
    //return lista de listas de tamaño batchSize que contiene pares (array de features, volumen conseguido)
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
    private ArrayList<Double> multidimMean(ArrayList<double[]> x){
        ArrayList<Double> mean = new ArrayList<Double>();
        for(int i = 0 ; i < x.get(0).length;i++){
            double suma = 0.0;
            for(double[] array:x){
                suma += array[i];
            }
            mean.add(suma);
        }
        return mean;
    }
    private double meanSquaredErrorDerivative (double predY,double targetY){
        return 2* (predY - targetY);
    }
    private double[] multiplyScalar(double[] array,double scalar){
        for(int i = 0; i < array.length; i++){
            array[i] = array[i]*scalar;
        }
        return array;
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
        for(int i = 0 ; i < this.pesos.length; i++){
            pesos[i] = datos[i];
        }
        this.bias = datos[datos.length-1];
    }
    private double[] parseString2Double(String[] pesos){
        double[] pesosDouble = {};
        for(int i = 0; i < pesos.length;i++){
            pesosDouble[i] = Double.parseDouble(pesos[i]);
        }
        return pesosDouble;
    }
}
