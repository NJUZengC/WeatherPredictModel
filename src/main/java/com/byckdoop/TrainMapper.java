package com.byckdoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;


/**
 * @author zengc
 * @date 2018/2/3 13:29
 */
public class TrainMapper extends Mapper<Object, Text, Text, IntWritable> {

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

    }

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        String temp = new String();
        String Interface = new String();
        String TimeInterval = new String();
        String line = value.toString();
        StringTokenizer itr = new StringTokenizer(line);
        int index = 0;
        String first = "";
        for(; itr.hasMoreTokens();) {
            temp = itr.nextToken();
            if(index == 0){
                first = temp;
            }
            else {
                context.write(new Text(first), new IntWritable(Integer.parseInt(temp)));
            }
            index++;
        }
    }
}
