package com.byckdoop;

/**
 * @author zengc
 * @date 2018/2/4 18:53
 */
public class HMMUtil {

    public static double[][] forward(HMMModel hmmModel,int[] sequence){

        int T = sequence.length;
        int hiddenSize = hmmModel.getHiddenSize();
        double[] pi = hmmModel.getPi();
        double[][] a = hmmModel.getA();
        double[][] b = hmmModel.getB();

        double[][] alpha = new double[hiddenSize][T];

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

    public static double[][] backward(HMMModel hmmModel,int[] sequence){

        int T = sequence.length;
        int hiddenSize = hmmModel.getHiddenSize();
        double[][] a = hmmModel.getA();
        double[][] b = hmmModel.getB();

        double[][] beta = new double[hiddenSize][T];

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

    public static double[] gamma(HMMModel hmmModel,int[] sequence,int i,double[][] alpha,double[][] beta){

        int T = sequence.length;
        int hiddenSize = hmmModel.getHiddenSize();

        //gamma数组长度为T+1，其中最后一项记录前面T项之和
        double[] gamma = new double[T+1];

        for (int t=0; t<T; t++){

            Double num = alpha[i][t] * beta[i][t]; //分子

            Double denom = 0.0; //分母
            for (int j=0; j<hiddenSize; j++){
                denom += alpha[j][t] * beta[j][t];
            }

            gamma[t] = num/denom;

        }

        double sum = 0;
        for (int k=0; k<T; k++) {
            sum += gamma[k];
        }
        gamma[T] = sum;

        return gamma;

    }

    public static double[] sigma(HMMModel hmmModel,int[] sequence,int i,int j,double[][] alpha,double[][] beta){

        int T = sequence.length;
        int hiddenSize = hmmModel.getHiddenSize();
        double[][] a = hmmModel.getA();
        double[][] b = hmmModel.getB();

        //定义sigma为一个长度为T+1的数组，其中最后一项表示前T项之和；
        double[] sigma = new double[T];

        for (int t=0; t<T-1; t++){

            Double num = alpha[i][t] * a[i][j] * b[j][sequence[t+1]] * beta[j][t+1]; //分子
            /*
            if (t == T-1) {
                //在T时刻（从1开始计算），分子的值为：
                num = alpha[i][t] * a[i][j];
                //num = alpha[i][t] * beta[i][t];
            } else {

                num = alpha[i][t] * a[i][j] * b[j][sequence[t+1]] * beta[j][t+1];
            //}
            */

            Double denom = 0.0; //分母

            for (int k=0; k<hiddenSize; k++){
                denom += (alpha[k][t] * beta[k][t]);
            }

            sigma[t] = num/denom;
        }

        double sum = 0;
        for (int k=0; k<T-1; k++) {
            sum += sigma[k];
        }
        sigma[T-1] = sum;

        return sigma;

    }

    //维特比算法
    //return path[T]
    public static int[] viterbi(HMMModel hmmModel,int[] sequence){

        int T = sequence.length;
        int hiddenSize = hmmModel.getHiddenSize();
        double[] pi = hmmModel.getPi();
        double[][] a = hmmModel.getA();
        double[][] b = hmmModel.getB();

        double[][] delta = new double[hiddenSize][T];
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
