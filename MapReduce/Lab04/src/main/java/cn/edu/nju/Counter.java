package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by hadoop on 16-11-26.
 */
public class Counter {

    public static boolean main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Triangle - counter");
        job.setJarByClass(Counter.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(CounterMapper.class);
        job.setReducerClass(CounterReducer.class);
        job.setNumReduceTasks(100);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path("output2"));
        Path outputPath = new Path("output3");
        outputPath.getFileSystem(conf).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, new Path("output3"));
        return job.waitForCompletion(true);
    }


    public static class CounterMapper extends Mapper<Object, Text, Text, Text> {

        Text k = new Text();
        Text v = new Text();

        protected void map(Object key, Text value, Mapper<Object, Text, Text, Text>.Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split("\\s");
            String usera = parts[0];
            k.set(usera);
            String userlist = parts[1];
            // for reducer to check based upon
            String toCheck = "?"+userlist;
            context.write(k, new Text(toCheck));

            String[] lists = userlist.split(",");
            for(int i = 0; i<lists.length-1; ++i) {
                k.set(lists[i]);
                for(int j = i+1; j<lists.length; ++j) {
                    v.set(lists[j]);
                    context.write(k, v);
                }
            }
        }
    }

    public static class CounterReducer extends Reducer<Text, Text, Text, Text> {
        Text v = new Text();
        long count = 0;

        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            HashSet<String> set = new HashSet<String>();
            ArrayList<String> list = new ArrayList<String>();

            for(Text t: values) {
                String s = t.toString();
                if(s.startsWith("?")) { // all adjacent vetex
                    s = s.substring(1);
                    String[] ss = s.split(",");
                    for(String x: ss) {
                        set.add(x);
                    }
                }
                else { // to be check if vertex s in adjacent to the vertex key
                    list.add(s);
                }
            }

            for(String y: list) {
                if(set.contains(y)) {
                    count++;
                }
            }

        }

        protected void cleanup(Context context) throws IOException, InterruptedException {
            String c = String.valueOf(count);
            context.write(new Text("Number of triangles(parts):"), new Text(c));
        }
    }

}
