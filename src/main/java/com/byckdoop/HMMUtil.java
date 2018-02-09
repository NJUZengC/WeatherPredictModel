package com.byckdoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

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

        //递推
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
                    beta[i][t] += (a[i][j] * b[j][sequence[t+1]] * beta[j][t+1]) ;
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

            double num = alpha[i][t] * beta[i][t]; //分子

            double denom = 0.0; //分母
            for (int j=0; j<hiddenSize; j++){
                denom += alpha[j][t] * beta[j][t];
            }

            gamma[t] = num/denom;

        }

        double sum = 0.0;
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

        //定义sigma为一个长度为T的数组，前T-1项为t从0到T-1时刻的sigma值，其中最后一项表示前T-1项之和；
        double[] sigma = new double[T];

        for (int t=0; t<T-1; t++){

            double num = alpha[i][t] * a[i][j] * b[j][sequence[t+1]] * beta[j][t+1]; //分子
            /*
            if (t == T-1) {
                //在T时刻（从1开始计算），分子的值为：
                num = alpha[i][t] * a[i][j];
                //num = alpha[i][t] * beta[i][t];
            } else {

                num = alpha[i][t] * a[i][j] * b[j][sequence[t+1]] * beta[j][t+1];
            //}
            */

            double denom = 0.0; //分母

            for (int k=0; k<hiddenSize; k++){
                denom += (alpha[k][t] * beta[k][t]);
            }

            sigma[t] = num/denom;
        }

        double sum = 0.0;
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

                double prob = 0.0;
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
        double prob = 0.0;
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

    public static int predict(HMMModel hmmModel,int[] sequence){
        //todo: sequence为观测序列，长度为T，前T-1个正确观测序列，第T个为占位符。返回最大可能的第T个观测序列

        int T = sequence.length;
        int observeSize = hmmModel.getObserveSize();
        int hiddenSize = hmmModel.getHiddenSize();
        //double[] pi = hmmModel.getPi();
        //double[][] a = hmmModel.getA();
        //double[][] b = hmmModel.getB();
        double prob = 0;  //记录P(O|m);
        int observe = 0;  //记录最大可能的第T个观测值；

        for (int o=0; o<observeSize; o++) {
            sequence[T-1] = o;
            double nprob = 0.0;
            double [][]alpha = forward(hmmModel, sequence);
            //System.out.println("alpha: "+Arrays.toString(alpha[0]));
            for (int i=0; i<hiddenSize; i++) {
                nprob += alpha[i][T-1];
                //记录：给定模型，当前观测序列出现的概率；
            }
            //System.out.println(nprob);
            if (nprob > prob) {
                prob = nprob;
                observe = o;
                //记录最大的概率及其对应的观测值；
            }

        }
        
        return observe;
    }

    public static HMMModel loadModel(HMMModel hmmModel,Path hmmModelPath)throws Exception{
        double[] pi = new double[hmmModel.getHiddenSize()];
        double [][]a = new double[hmmModel.getHiddenSize()][hmmModel.getHiddenSize()];
        double [][] b = new double[hmmModel.getHiddenSize()][hmmModel.getObserveSize()];
        String []tokens;
        String line;
        BufferedReader dataReader = new BufferedReader(new FileReader(hmmModelPath.toString()));
        try{
            int piLength = 0;
            int aLength = 0;
            int bLength = 0;
            while((line = dataReader.readLine())!=null){
                tokens = line.split(" ");
                if(tokens[0].trim().startsWith("I")){
                    piLength += 1;
                    assert(tokens.length == hmmModel.getHiddenSize()+1);
                    for(int i=1;i<tokens.length;i++){
                        pi[i-1] = Double.parseDouble(tokens[i]);
                    }
                }else if(tokens[0].trim().startsWith("E")){
                    bLength += 1;
                    assert(tokens.length == hmmModel.getObserveSize()+1);
                    int index = Integer.parseInt(tokens[0].trim().substring(1));
                    for (int i=1;i<tokens.length;i++){
                        b[index][i-1] = Double.parseDouble(tokens[i]);
                    }

                }else if(tokens[0].trim().startsWith("T")){
                    aLength += 1;
                    assert(tokens.length == hmmModel.getHiddenSize()+1);
                    int index = Integer.parseInt(tokens[0].trim().substring(1));
                    for (int i=1;i<tokens.length;i++){
                        a[index][i-1] = Double.parseDouble(tokens[i]);
                    }
                }
                hmmModel.setPi(pi);
                hmmModel.setA(a);
                hmmModel.setB(b);
            }
            assert(piLength==1 && aLength==hmmModel.getHiddenSize() && aLength==bLength);
        }finally {
            dataReader.close();
        }
        return hmmModel;
    }

    public static double evaluate(HMMModel hmmModel,Path hmmModelPath,Path testPath)throws Exception{


        hmmModel = loadModel(hmmModel,hmmModelPath);
        //System.out.println(hmmModel);
        ArrayList<int[]> testSample = new ArrayList<>();
        String []tokens;
        String line;
        BufferedReader dataReader = new BufferedReader(new FileReader(testPath.toString()));
        try{

            while((line = dataReader.readLine())!=null){
                tokens = line.split(" ");
                int[] res = new int[tokens.length];
                for(int i=0;i<res.length;i++)
                    res[i] = Integer.parseInt(tokens[i].trim());
                if(res.length>0)
                    testSample.add(res);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            dataReader.close();
        }

        int correctNum = 0;
        for(int i=0;i<testSample.size();i++){
            int[] sequence = testSample.get(i);
            //System.out.println("before: "+ Arrays.toString(sequence));
            int realIndex = sequence[sequence.length-1];
            int index = predict(hmmModel,sequence);
            //System.out.println("after: "+sequence[sequence.length-1]);
            //System.out.println(index);
            if(index == realIndex){
                System.out.println("correct");
                correctNum += 1;
            }
        }
        return correctNum*1.0/testSample.size();

    }

    public static int readHDFS(Path path)throws Exception{
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(path.toUri(), conf);
        FSDataInputStream hdfsInStream = fs.open(path);

        byte[] ioBuffer =new byte[1024];
        int res = hdfsInStream.read(ioBuffer);
        return res;
    }



}
