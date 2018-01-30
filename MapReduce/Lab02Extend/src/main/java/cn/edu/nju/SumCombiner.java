package cn.edu.nju;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created by godfray on 2016/10/31.
 */
public class SumCombiner extends Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        long sum = 0;
        for(Text val: values) {
            sum += Long.parseLong(val.toString());
        }
        String[] keyPair = key.toString().split("#");
        String result = keyPair[1]+":"+String.valueOf(sum);

        context.write(new Text(keyPair[0]), new Text(result));
    }

}
