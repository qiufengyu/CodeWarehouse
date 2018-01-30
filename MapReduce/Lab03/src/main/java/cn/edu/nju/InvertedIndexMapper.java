package cn.edu.nju;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by godfray on 2016/10/31.
 */
public class InvertedIndexMapper extends Mapper<Object, Text, Text, Text> {
    @Override
    protected void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        String fileName = fileSplit.getPath().getName();
        Text word = new Text();
        StringTokenizer itr = new StringTokenizer(value.toString().toLowerCase());
        while(itr.hasMoreTokens()) {
            String temp = itr.nextToken();
            word.set(temp + "#" + fileName);
            context.write(word, new Text("1"));
        }
    }
}