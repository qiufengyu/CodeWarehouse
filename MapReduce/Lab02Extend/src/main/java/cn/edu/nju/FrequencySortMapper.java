package cn.edu.nju;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Created by godfray on 2016/11/1.
 */
public class FrequencySortMapper extends Mapper<Object, Text, DoubleWritable, Text> {
    @Override
    protected void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {
        Text word = new Text();
        String line = value.toString();
        word.set(line.split("\t")[0]);
        String l = line.split("\t")[1];
        double frequency = Double.parseDouble(l.split(",")[0]);
        context.write(new DoubleWritable(frequency), word);

    }
}
