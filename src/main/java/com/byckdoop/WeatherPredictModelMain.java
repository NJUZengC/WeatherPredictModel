package com.byckdoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * @author zengc
 * @date 2018/2/3 13:28
 */
public class WeatherPredictModelMain  {

    public static void main(String[] args) throws Exception{

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 5) {
            System.err.println("Usage: WeatherPredictModelMain <observeSize> <hiddenSize> <input> <output> <maxTurns>");
            System.exit(-1);
        }
        int index = 1;
        int maxTurns = Integer.parseInt(otherArgs[4]);
        while (index < maxTurns) {
            conf.set(WeatherModelConfig.observeSize, otherArgs[0]);
            conf.set(WeatherModelConfig.hiddenSize, otherArgs[1]);
            conf.setInt(WeatherModelConfig.iterationNum,index);

            @SuppressWarnings("deprecation")
            Job job = new Job(conf, "WeatherPredictModel");
            if(index > 1) {
                String hdfspath =  otherArgs[3]+(index-1)+"/HMMMODEL-r-00000";
                Path path = new Path(hdfspath);
                FileSystem fileSystem = path.getFileSystem(conf);
                //getFileSystem()函数功能  Return the FileSystem that owns this Path.
                if (!fileSystem.exists(new Path(hdfspath))) {
                    System.out.println("Something Wrong in HMMMODEL");
                }
                System.out.println("Something Wright in HMMMODEL");
                job.addCacheFile( path.toUri());
            }
            job.setJarByClass(WeatherPredictModelMain.class);
            job.setMapperClass(TrainMapper.class);
            //job.setCombinerClass(InterfaceFrequentCombiner.class);
            job.setReducerClass(TrainReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(HMMArrayWritable.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(HMMArrayWritable.class);

            MultipleOutputs.addNamedOutput(job, "HMMMODEL", TextOutputFormat.class, Text.class, Text.class);
            MultipleOutputs.addNamedOutput(job, "Debug", TextOutputFormat.class, Text.class, Text.class);
            FileInputFormat.addInputPath(job, new Path(otherArgs[2]));
            FileOutputFormat.setOutputPath(job, new Path(otherArgs[3]+index));
            job.waitForCompletion(true);

            int pass_index = index-1;
            Path path = new Path(otherArgs[3]+pass_index);
            FileSystem fileSystem = path.getFileSystem(conf);
            //getFileSystem()函数功能  Return the FileSystem that owns this Path.
            if (fileSystem.exists(path)) {
                //System.out.println(path.toString());
                fileSystem.delete(path,true);
            }
            index++;
        }

    }

}
