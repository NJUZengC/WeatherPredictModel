package com.byckdoop;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Writable;

/**
 * @author zengc
 * @date 2018/2/5 0:23
 */
public class HMMArrayWritable extends ArrayWritable {

    public HMMArrayWritable(){
        super(DoubleWritable.class);
    }

    @Override
    public String toString() {
        Writable[] value = get();
        String res = " ";
        for (int i=0;i<value.length;i++)
            res += value[i].toString() + " ";
        return res;
    }
}
