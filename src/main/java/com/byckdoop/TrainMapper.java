package com.byckdoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;


/**
 * @author zengc
 * @date 2018/2/3 13:29
 */
public class TrainMapper extends Mapper<Object, Text, Text, HMMArrayWritable> {

    HMMModel hmmModel = new HMMModel();

    public void setup(Context context)throws IOException, InterruptedException{
        Configuration configuration = context.getConfiguration();
        int observeSize = configuration.getInt(WeatherModelConfig.observeSize,-1);
        int hiddenSize = configuration.getInt(WeatherModelConfig.hiddenSize,-1);

        if(observeSize <=0 || hiddenSize <= 0){
            System.out.println("Exception <= 0");
            System.exit(-1);
        }

        hmmModel.init(observeSize,hiddenSize);
        System.out.println("hahahha");

    }

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        String sq = value.toString();
        String []strSequence = sq.split(" ");
        int []o = new int[strSequence.length];

        for(int i=0;i<strSequence.length;i++)
            o[i] = Integer.parseInt(strSequence[i]);

        HMMArrayWritable pi = new HMMArrayWritable();

        DoubleWritable []initialMatrix = new DoubleWritable[hmmModel.getHiddenSize()];
        DoubleWritable [][]transitionMatrix = new DoubleWritable[hmmModel.getHiddenSize()][hmmModel.getHiddenSize()];
        DoubleWritable [][]emitMatrix = new DoubleWritable[hmmModel.getHiddenSize()][hmmModel.getObserveSize()];

        double [][] alpha;
        double [][] beta;

        alpha = HMMUtil.forward(hmmModel,o);
        beta = HMMUtil.backward(hmmModel,o);


        double [][]gamma = new double[hmmModel.getHiddenSize()][o.length+1];

        for(int i=0;i<hmmModel.getHiddenSize();i++) {
            gamma[i] = HMMUtil.gamma(hmmModel, o, i, alpha, beta);
            initialMatrix[i] = new DoubleWritable(gamma[i][0]);
        }
        pi.set(initialMatrix);
        context.write(new Text("initial from "),pi);


        for(int i=0;i<hmmModel.getHiddenSize();i++){
            HMMArrayWritable a = new HMMArrayWritable();

            for(int j=0;j<hmmModel.getHiddenSize();j++){
                double []sigma = HMMUtil.sigma(hmmModel,o,i,j,alpha,beta);
                transitionMatrix[i][j] = new DoubleWritable(sigma[sigma.length - 1]/gamma[i][gamma[i].length-1]);
            }
            a.set(transitionMatrix[i]);
            context.write(new Text("transit from "+i),a);
        }

        for(int i=0;i<hmmModel.getHiddenSize();i++){

            HMMArrayWritable b = new HMMArrayWritable();
            for(int j=0;j<hmmModel.getObserveSize();j++){
                double sum = 0;
                for(int k = 0;k<o.length;k++){
                    if(o[k]==j)
                        sum += gamma[i][k];
                }
                emitMatrix[i][j] = new DoubleWritable(sum/gamma[i][gamma[i].length-1]);
            }
            b.set(emitMatrix[i]);
            context.write(new Text("emit from "+i),b);
        }


    }
}
