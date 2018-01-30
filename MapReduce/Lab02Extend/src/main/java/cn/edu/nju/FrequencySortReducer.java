package cn.edu.nju;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created by godfray on 2016/11/1.
 */
public class FrequencySortReducer extends Reducer<DoubleWritable, Text, Text, DoubleWritable> {

    public void reduce(DoubleWritable key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        Text word = new Text();
        for(Text t: values) {
            word.set(t.toString());
            context.write(word, key);
        }
    }

}
