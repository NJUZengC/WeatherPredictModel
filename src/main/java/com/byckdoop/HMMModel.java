package com.byckdoop;

import org.apache.hadoop.io.ArrayWritable;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zengc
 * @date 2018/2/3 10:42
 */
public class HMMModel {

    private int observeSize; // M
    private int hiddenSize;  // N

    private List<Double> pi; // N
    private List<List<Double>> a;  //  N * N
    private List<List<Double>> b; // N * M

    public void init(int observeSize,int hiddenSize){
        this.observeSize = observeSize;
        this.hiddenSize = hiddenSize;
        this.pi = new ArrayList<Double>();
        this.a = new ArrayList<List<Double>>();
        this.b = new ArrayList<List<Double>>();

        for(int i=0;i<hiddenSize;i++){
            pi.add(Math.random());

            List<Double> rowA = new ArrayList<Double>();
            for(int j=0;j<hiddenSize;j++){
                rowA.add(Math.random());
            }
            a.add(rowA);

            List<Double> rowB = new ArrayList<Double>();
            for(int k=0;k<observeSize;k++){
                rowB.add(Math.random());
            }
            b.add(rowB);
        }
    }

    //return  N * T
    public List<ArrayWritable> forward(List<Double> sequence){

        return null;

    }

    //return N * T
    public List<ArrayWritable> backward(List<Double> sequence){

        return null;

    }

    //return T * 1
    public List<Double> gamma(List<Double> sequence,int i,List<ArrayWritable> alpha,List<ArrayWritable> beta){

        return null;
    }

    //return T * 1
    public List<Double> sigma(List<Double> sequence,int i,int j,List<ArrayWritable> alpha,List<ArrayWritable> beta){

        return null;
    }

}
