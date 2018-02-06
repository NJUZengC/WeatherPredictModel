package com.byckdoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.FileReader;
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
        int iterationNum = configuration.getInt(WeatherModelConfig.iterationNum,-1);

        if(observeSize <=0 || hiddenSize <= 0){
            System.out.println("Exception <= 0");
            System.exit(-1);
        }
        if(iterationNum == 0) {
            hmmModel.init(observeSize, hiddenSize);
        }else {
            hmmModel.init(observeSize, hiddenSize);
            Path[] cacheFiles = context.getLocalCacheFiles();
            if(cacheFiles.length ==1) {

                Path path = cacheFiles[0];
                String line;
                String[] tokens;
                BufferedReader dataReader = new BufferedReader(new FileReader(path.toString()));
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
                                hmmModel.getPi()[i-1] = Double.parseDouble(tokens[i]);
                            }
                        }else if(tokens[0].trim().startsWith("E")){
                            bLength += 1;
                            assert(tokens.length == hmmModel.getObserveSize()+1);
                            int index = Integer.parseInt(tokens[0].trim().substring(1));
                            for (int i=1;i<tokens.length;i++){
                                hmmModel.getB()[index][i-1] = Double.parseDouble(tokens[i]);
                            }

                        }else if(tokens[0].trim().startsWith("T")){
                            aLength += 1;
                            assert(tokens.length == hmmModel.getHiddenSize()+1);
                            int index = Integer.parseInt(tokens[0].trim().substring(1));
                            for (int i=1;i<tokens.length;i++){
                                hmmModel.getA()[index][i-1] = Double.parseDouble(tokens[i]);
                            }
                        }
                    }
                    assert(piLength==1 && aLength==hmmModel.getHiddenSize() && aLength==bLength);
                }finally {
                    dataReader.close();
                }
            }
            HMMArrayWritable debug = new HMMArrayWritable();
            DoubleWritable[] doubleWritables = hmmModel.getWritablePi();
            debug.set(doubleWritables);
            context.write(new Text(WeatherModelConfig.debugInfo), debug);
        }

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
        context.write(new Text("I"),pi);


        for(int i=0;i<hmmModel.getHiddenSize();i++){
            HMMArrayWritable a = new HMMArrayWritable();

            for(int j=0;j<hmmModel.getHiddenSize();j++){
                double []sigma = HMMUtil.sigma(hmmModel,o,i,j,alpha,beta);
                transitionMatrix[i][j] = new DoubleWritable(sigma[sigma.length - 1]/(gamma[i][gamma[i].length-1]-gamma[i][gamma[i].length-2]));
            }
            a.set(transitionMatrix[i]);
            context.write(new Text("T"+i),a);
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
            context.write(new Text("E"+i),b);
        }


    }
}
