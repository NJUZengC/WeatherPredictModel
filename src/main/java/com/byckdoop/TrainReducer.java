package com.byckdoop;

import org.apache.hadoop.io.ArrayWritable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @author zengc
 * @date 2018/2/4 13:05
 */
public class TrainReducer extends Reducer<Text, HMMArrayWritable, Text, ArrayWritable> {

    public void reduce(Text key, Iterable<HMMArrayWritable> values, Context context) throws IOException, InterruptedException {
        double sum = 0;
        String np = "";
        for (HMMArrayWritable val : values) {
            context.write(key,val);
        }


    }
}
