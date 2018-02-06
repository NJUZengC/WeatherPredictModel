package com.byckdoop;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zengc
 * @date 2018/2/3 10:42
 */
public class HMMModel {

    private int observeSize; // M
    private int hiddenSize;  // N



    private double[] pi; // N
    private double[][] a;  //  N * N
    private double[][] b; // N * M


    public void init(int observeSize,int hiddenSize){
        this.observeSize = observeSize;
        this.hiddenSize = hiddenSize;
        this.pi = new double[hiddenSize];
        this.a = new double[hiddenSize][hiddenSize];
        this.b = new double[hiddenSize][observeSize];

        for(int i=0;i<hiddenSize;i++){
            pi[i] = (Math.random());


            for(int j=0;j<hiddenSize;j++){
                a[i][j] = (Math.random());
            }

            for(int k=0;k<observeSize;k++){
                b[i][k] = (Math.random());
            }
        }
    }

    public DoubleWritable[] getWritablePi(){
        DoubleWritable []doubleWritables = new DoubleWritable[pi.length];
        for (int i=0;i<doubleWritables.length;i++){
            doubleWritables[i] = new DoubleWritable(pi[i]);
        }
        return doubleWritables;
    }


    public double[] getPi() {
        return pi;
    }

    public void setPi(double[] pi) {
        this.pi = pi;
    }


    public double[][] getA() {
        return a;
    }

    public void setA(double[][] a) {
        this.a = a;
    }


    public double[][] getB() {
        return b;
    }

    public void setB(double[][] b) {
        this.b = b;
    }

    public int getObserveSize() {
        return observeSize;
    }


    public void setObserveSize(int observeSize) {
        this.observeSize = observeSize;
    }

    public int getHiddenSize() {
        return hiddenSize;
    }

    public void setHiddenSize(int hiddenSize) {
        this.hiddenSize = hiddenSize;
    }


}
