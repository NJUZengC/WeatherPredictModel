package com.byckdoop;

import org.apache.hadoop.io.ArrayWritable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import java.io.IOException;

/**
 * @author zengc
 * @date 2018/2/4 13:05
 */
public class TrainReducer extends Reducer<Text, HMMArrayWritable, Text, ArrayWritable> {

    private MultipleOutputs<Text,ArrayWritable> output;


    public void setup(Context context)throws IOException, InterruptedException{
        output = new MultipleOutputs<Text,ArrayWritable>(context);
    }

    public void reduce(Text key, Iterable<HMMArrayWritable> values, Context context) throws IOException, InterruptedException {
        if(key.toString().equals(WeatherModelConfig.debugInfo)){
            for (HMMArrayWritable val : values) {
                output.write("Debug",key,val);

            }

        }else {
            double sum = 0;
            String np = "";
            for (HMMArrayWritable val : values) {
                context.write(key, val);
                output.write("HMMMODEL", key, val);

            }
        }




    }
}
