package com.byckdoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.util.GenericOptionsParser;

/**
 * @author zengc
 * @date 2018/2/3 13:28
 */
public class WeatherPredictModelMain  {

    public static void main(String[] args) throws Exception{

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 4) {
            System.err.println("Usage: InvertedIndex <in> <out>");
            System.exit(4);
        }
        conf.set(WeatherModelConfig.observeSize,otherArgs[0]);
        conf.set(WeatherModelConfig.hiddenSize,otherArgs[1]);
        @SuppressWarnings("deprecation")
        Job job = new Job(conf,"InterfaceFrequent");
        job.setJarByClass(WeatherPredictModelMain.class);
        job.setMapperClass(TrainMapper.class);
        //job.setCombinerClass(InterfaceFrequentCombiner.class);
        job.setReducerClass(TrainReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(HMMArrayWritable.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(HMMArrayWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[2]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[3]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

}
