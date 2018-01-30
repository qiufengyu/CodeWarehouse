package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created by hadoop on 16-11-25.
 */
public class Undirected {
    public Undirected() {
    }

    public static boolean main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Triangle - to undirected graph");
        job.setJarByClass(Undirected.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(Undirected.UndirectedMapper.class);
        job.setReducerClass(Undirected.UndirectedReducer.class);
        job.setNumReduceTasks(50);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path outputPath = new Path("output2");
        outputPath.getFileSystem(conf).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, new Path("output2"));
        return job.waitForCompletion(true);
    }

    public static boolean mainAnd(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        Job job1 = Job.getInstance(conf, "Triangle - to undirected graph (and) - 1");
        job1.setJarByClass(Undirected.class);
        job1.setInputFormatClass(TextInputFormat.class);
        job1.setMapperClass(UndirectedAndMapper.class);
        job1.setReducerClass(UndirectedAndReducer.class);
        job1.setMapOutputValueClass(IntWritable.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        job1.setNumReduceTasks(100);
        FileInputFormat.addInputPath(job1, new Path(args[0]));
        Path outputPath = new Path("output1");
        outputPath.getFileSystem(conf).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job1, new Path("output1"));
        if(job1.waitForCompletion(true)) {
            Job job2 = Job.getInstance(conf, "Triangle - to undirected graph (and) - 2");
            job2.setJarByClass(Undirected.class);
            job2.setInputFormatClass(TextInputFormat.class);
            job2.setMapperClass(Undirected.UndirectedMapper.class);
            job2.setReducerClass(Undirected.UndirectedReducer.class);
            job2.setOutputKeyClass(Text.class);
            job2.setOutputValueClass(Text.class);
            job2.setNumReduceTasks(50);
            FileInputFormat.addInputPath(job2, new Path("output1"));
            Path outputPath2 = new Path("output2");
            outputPath2.getFileSystem(conf).delete(outputPath2, true);
            FileOutputFormat.setOutputPath(job2, new Path("output2"));
            return job2.waitForCompletion(true);

        }
        return false;
    }

    public static class UndirectedAndMapper extends Mapper<Object, Text, Text, IntWritable> {
        Text k = new Text();

        protected void map(Object key, Text value, Mapper<Object, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split("\\s");
            String usera = parts[0];
            String userb = parts[1];
            String k1 = usera+","+userb;
            k.set(k1);
            context.write(k, new IntWritable(1));
            String k2 = userb+","+usera;
            k.set(k2);
            context.write(k, new IntWritable(0));
        }
    }

    public static class UndirectedAndReducer extends Reducer<Text, IntWritable, Text, Text> {
        Text k = new Text();
        Text v = new Text();

        static Integer one = new Integer(1);
        static Integer zero = new Integer(0);

        protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, Text>.Context context) throws IOException, InterruptedException {
            HashSet<Integer> set = new HashSet<Integer>();

            for(IntWritable i: values) {
                set.add(i.get());
            }

            if(set.contains(one) && set.contains(zero)) {
                String[] pair = key.toString().split(",");
                String usera = pair[0];
                String userb = pair[1];
                if(usera.compareTo(userb) < 0) {
                    k.set(usera);
                    v.set(userb);
                    context.write(k, v);
                } else if(usera.compareTo(userb) > 0) {
                    k.set(userb);
                    v.set(usera);
                    context.write(k, v);
                }
            }

        }
    }

    public static class UndirectedReducer extends Reducer<Text, Text, Text, Text> {
        Text v = new Text();

        public UndirectedReducer() {
        }

        protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
            TreeSet<String> set = new TreeSet<String>();

            for(Text t: values) {
                set.add(t.toString());
            }

            String s = new String();
            for(String x: set) {
                s += (x+",");
            }

            v.set(s.replaceAll(",$", ""));
            context.write(key, v);
        }
    }

    public static class UndirectedMapper extends Mapper<Object, Text, Text, Text> {
        Text k = new Text();
        Text v = new Text();

        public UndirectedMapper() {
        }

        protected void map(Object key, Text value, Mapper<Object, Text, Text, Text>.Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split("\\s");
            String usera = parts[0];
            String userb = parts[1];
            if(usera.compareTo(userb) < 0) {
                k.set(usera);
                v.set(userb);
                context.write(k, v);
            } else if(usera.compareTo(userb) > 0) {
                k.set(userb);
                v.set(usera);
                context.write(k, v);
            }

        }
    }
}
