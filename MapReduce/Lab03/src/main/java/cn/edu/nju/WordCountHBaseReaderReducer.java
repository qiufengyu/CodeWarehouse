package cn.edu.nju;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created by shenr on 2016/11/2.
 */
public class WordCountHBaseReaderReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

   public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {
        for(DoubleWritable val: values){
            context.write(key, val);
        }
   }
}