package com.byckdoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import java.io.IOException;

/**
 * @author zengc
 * @date 2018/2/4 13:05
 */
public class TrainReducer extends Reducer<Text, HMMArrayWritable, Text, ArrayWritable> {

    private MultipleOutputs<Text,ArrayWritable> output;
    private HMMModel hmmModel = new HMMModel();
    double [] pi;
    double [][] a;
    double [][] b;


    public void setup(Context context)throws IOException, InterruptedException{
        output = new MultipleOutputs<Text,ArrayWritable>(context);
        Configuration configuration = context.getConfiguration();
        int observeSize = configuration.getInt(WeatherModelConfig.observeSize,-1);
        int hiddenSize = configuration.getInt(WeatherModelConfig.hiddenSize,-1);
        int iterationNum = configuration.getInt(WeatherModelConfig.iterationNum,-1);

        if(observeSize <=0 || hiddenSize <= 0){
            System.out.println("Exception <= 0");
            System.exit(-1);
        }

        hmmModel.init(observeSize, hiddenSize);

        pi = new double[hiddenSize];
        a = new double[hiddenSize][hiddenSize];
        b = new double[hiddenSize][observeSize];
    }

    public void reduce(Text key, Iterable<HMMArrayWritable> values, Context context) throws IOException, InterruptedException {
        if(key.toString().equals(WeatherModelConfig.debugInfo)){
            for (HMMArrayWritable val : values) {
                output.write("Debug",key,val);
            }

        }else {
            double sum = 0;
            double []res;
            if(key.toString().startsWith("E")){
                res = new double[hmmModel.getObserveSize()];
            }else {
                res = new double[hmmModel.getHiddenSize()];
            }
            for(int i=0;i<res.length;i++)
                res[i] = 0;


            for (HMMArrayWritable val : values) {
                context.write(key, val);
                Writable[] lists = val.get();
                assert (res.length == lists.length);
                for(int i=0;i<res.length;i++) {
                    double temp = Double.parseDouble(lists[i].toString());
                    res[i] = res[i] + temp;
                    sum += temp;
                }
            }



            HMMArrayWritable hmmArrayWritable = new HMMArrayWritable();
            DoubleWritable[] doubleWritables = new DoubleWritable[res.length];
            for(int i=0;i<res.length;i++)
                doubleWritables[i] = new DoubleWritable(res[i]/sum);
            hmmArrayWritable.set(doubleWritables);
            output.write("HMMMODEL", key,hmmArrayWritable);

        }

    }

    public void cleanup(Context context) throws IOException, InterruptedException {
        /*
        output.close();
        output = new MultipleOutputs<Text,ArrayWritable>(context);
        String outputFile = context.getConfiguration().get(WeatherModelConfig.outputFile);
        String testFile = context.getConfiguration().get(WeatherModelConfig.testFile);
        try {
            double accurateRate = HMMUtil.evaluate(hmmModel,new Path(outputFile),new Path(testFile));
            HMMArrayWritable arrayWritable = new HMMArrayWritable();
            DoubleWritable[] t = new DoubleWritable[]{new DoubleWritable(accurateRate)};
            arrayWritable.set(t);
            output.write("Debug",new Text(WeatherModelConfig.debugInfo),arrayWritable);

        }catch (Exception e){
            e.printStackTrace();
        }
        */
    }
}
