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

    /*
    private List<Double> pi; // N
    private List<List<Double>> a;  //  N * N
    private List<List<Double>> b; // N * M
    */

    private Double[] pi; // N
    private Double[][] a;  //  N * N
    private Double[][] b; // N * M
    /*
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
    */

    public void init(int observeSize,int hiddenSize){
        this.observeSize = observeSize;
        this.hiddenSize = hiddenSize;
        this.pi = new Double[hiddenSize];
        this.a = new Double[hiddenSize][hiddenSize];
        this.b = new Double[hiddenSize][observeSize];

        for(int i=0;i<hiddenSize;i++){
            pi[i] = (Math.random());


            for(int j=0;j<hiddenSize;j++){
                a[i][j] = (Math.random());
            }

            for(int k=0;k<observeSize;k++){
                a[i][k] = (Math.random());
            }
        }
    }
    /*
    //return  N * T
    public List<ArrayWritable> forward(List<Double> sequence){

        return null;

    }*/

    //return  N * T
    public Double[][] forward(int[] sequence){

        int T = sequence.length;

        Double[][] alpha = new Double[hiddenSize][T];

        //初始化alpha
        for (int i=0; i<hiddenSize; i++){
            alpha[i][0] = pi[i] * b[i][sequence[0]];
        }

        for (int t=0; t<T-1; t++){
            for (int j=0; j<hiddenSize; j++){

                alpha[j][t+1] = 0.0;
                for (int i=0; i<hiddenSize; i++){
                    alpha[j][t+1] += (alpha[i][t] * a[i][j]);
                }
                alpha[j][t+1] *= b[j][sequence[t+1]];
            }
        }

        return alpha;

    }

    /*
    //return N * T
    public List<ArrayWritable> backward(List<Double> sequence){

        return null;

    }*/

    //return  N * T
    public Double[][] backward(int[] sequence){

        int T = sequence.length;

        Double[][] beta = new Double[hiddenSize][T];

        //初始化beta
        for (int i=0; i<hiddenSize; i++){
            beta[i][T-1] = 1.0;
        }

        for (int t=T-2; t>=0; t--){
            for (int i=0; i<hiddenSize; i++){

                beta[i][t] = 0.0;
                for (int j=0; j<hiddenSize; j++){
                    beta[i][t] += a[i][j] * b[j][sequence[t+1]] * beta[j][t+1];
                }

            }
        }

        return beta;

    }

    /*
    //return T * 1
    public List<Double> gamma(List<Double> sequence,int i,List<ArrayWritable> alpha,List<ArrayWritable> beta){

        return null;
    }*/

    //return T
    public Double[] gamma(int[] sequence,int i,Double[][] alpha,Double[][] beta){

        int T = sequence.length;

        Double[] gamma = new Double[T];

        for (int t=0; t<T; t++){

            Double num = alpha[i][t] * beta[i][t]; //分子

            Double denom = 0.0; //分母
            for (int j=0; j<hiddenSize; j++){
                denom += alpha[j][t] * beta[j][t];
            }

            gamma[t] = num/denom;

        }

        return gamma;
    }


    /*
    //return T * 1
    public List<Double> sigma(List<Double> sequence,int i,int j,List<ArrayWritable> alpha,List<ArrayWritable> beta){

        return null;
    }*/

    //return T
    public Double[] sigma(int[] sequence,int i,int j,Double[][] alpha,Double[][] beta){

        int T = sequence.length;
        Double[] sigma = new Double[T];

        for (int t=0; t<T; t++){

            Double num; //分子

            if (t == T-1) {
                num = alpha[i][t] * a[i][j];
            } else {
                num = alpha[i][t] * a[i][j] * b[j][sequence[t+1]] * beta[j][t+1];
            }

            Double denom = 0.0; //分母

            for (int k=0; k<hiddenSize; k++){
                denom += (alpha[k][t] * beta[k][t]);
            }

            sigma[t] = num/denom;
        }


        return sigma;
    }


    //维特比算法
    //return path[T]
    public int[] viterbi(int[] sequence){

        int T = sequence.length;
        Double[][] delta = new Double[hiddenSize][T];
        int[][] phi = new int[hiddenSize][T];
        int[] path = new int[T];

        //初始化delta以及phi
        for (int i=0; i<hiddenSize; i++){
            delta[i][0] = pi[i] * b[i][sequence[0]];
            phi[i][0] = 0;
        }

        //递归求解delta[i][t]和phi[i][t]
        for (int t=1; t<T; t++){
            for (int i=0; i<hiddenSize; i++){

                double prob = -1;
                int state = 0;

                for (int j=0; j<hiddenSize; j++){

                    double nprob = delta[j][t-1] * a[j][i] * b[i][sequence[t]];

                    if (nprob>prob) {

                        prob = nprob;
                        state = j;

                    }
                }

                delta[i][t] = prob;
                phi[i][t] = state;
            }

        }

        //终止
        double prob = -1;
        for (int i=0; i<hiddenSize; i++){
            if (delta[i][T-1]>prob) {
                prob =delta[i][T-1];
                path[T-1] = i;
            }
        }

        //最优路径回溯
        for (int t=T-2; t>=0; t--) {
            path[t] = phi[path[t+1]][t+1];
        }

        //最优路径序列
        return path;

    }

}
