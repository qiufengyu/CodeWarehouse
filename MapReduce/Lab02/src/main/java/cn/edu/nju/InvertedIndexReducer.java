package cn.edu.nju;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by godfray on 2016/10/31.
 */
public class InvertedIndexReducer extends Reducer<Text, IntWritable, Text, Text> {
    private Text word1 = new Text();
    private Text word2 = new Text();
    String temp = new String();
    static Text currentItem = new Text(" ");
    static List<String> postingList = new ArrayList<String>();

    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        word1.set(key.toString().split("#")[0]);
        temp = key.toString().split("#")[1];
        for(IntWritable val : values) {
            sum += val.get();
        }

        word2.set(temp+":"+sum);
        if(!currentItem.equals(word1) && !currentItem.equals(" ")) {
            // currentItem 为上一个word
            StringBuilder out = new StringBuilder();
            long count = 0;
            for(String p: postingList) {
                out.append(p.replaceAll("\\.[tT][xX][tT]\\.segmented", ""));
                out.append(";");
                count += Long.parseLong(p.substring(p.indexOf(":")+1));
            }
            // out.append("total:"+count);
            double average = (double) count /(double) postingList.size();
            StringBuilder out2 = new StringBuilder();
            out2.append(average+",");
            out2.append(out.toString().replaceAll(";$", ""));
            if(count > 0) {
                // context.write(currentItem, new Text(out.toString()));
                context.write(currentItem, new Text(out2.toString()));
            }
            postingList = new ArrayList<String>();
        }
        currentItem.set(word1);
        postingList.add(word2.toString());

    }

    public void cleanup(Context context) throws IOException, InterruptedException {
        StringBuilder out = new StringBuilder();
        long count = 0;
        for(String p: postingList) {
            out.append(p.replaceAll("\\.[tT][xX][tT]\\.segmented", ""));
            out.append(";");
            count += Long.parseLong(p.substring(p.indexOf(":")+1));
        }

        double average = (double) count /(double) postingList.size();
        StringBuilder out2 = new StringBuilder();
        out2.append(average+",");
        out2.append(out.toString().replaceAll(";$", ""));
        if(count > 0) {
            //context.write(currentItem, new Text(out.toString()));
            context.write(currentItem, new Text(out2.toString()));
        }
    }
}
