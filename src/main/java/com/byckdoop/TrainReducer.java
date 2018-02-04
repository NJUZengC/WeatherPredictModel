package com.byckdoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @author zengc
 * @date 2018/2/4 13:05
 */
public class TrainReducer extends Reducer<Text, DoubleWritable, Text, Text> {

    public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
        double sum = 0;
        String np = "";
        for (DoubleWritable val : values) {
            np += val.get() +" ";
            sum += val.get();
        }

        context.write(key,new Text(sum+" /j "+np));
    }
}
