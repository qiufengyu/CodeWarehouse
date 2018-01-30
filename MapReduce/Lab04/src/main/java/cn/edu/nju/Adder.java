package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by hadoop on 16-11-27.
 */
public class Adder {
    public static boolean main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Triangle - adder");
        job.setJarByClass(Counter.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(AdderMapper.class);
        job.setReducerClass(AdderReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path("output3"));
        Path outputPath = new Path(args[1]);
        outputPath.getFileSystem(conf).delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        return job.waitForCompletion(true);
    }

    public static class AdderMapper extends Mapper<Object, Text, Text, Text> {

        Text k = new Text("add");

        protected void map(Object key, Text value, Mapper<Object, Text, Text, Text>.Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] parts = line.split("\t");
            if(parts.length == 2) {
                context.write(k, new Text(parts[1]));
            }
        }
    }

    public static class AdderReducer extends Reducer<Text, Text, Text, Text> {
        static long adder = 0;
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text t : values) {
                adder += Long.valueOf(t.toString());
            }
            context.write(new Text("Number of triangles:"), new Text(String.valueOf(adder)));
        }

    }

}
