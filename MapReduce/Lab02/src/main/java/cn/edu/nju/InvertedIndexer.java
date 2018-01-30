package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.PropertyConfigurator;


public class InvertedIndexer
{
    public static void main( String[] args ) {

        PropertyConfigurator.configure("log4j.properties");

        if(args.length != 2) {
            System.err.println("Usage: InvertedIndexer args[0] args[1]");
            System.exit(-1);
        }

        System.out.println("Program Running...");
        try {
            Configuration conf = new Configuration();
            Job job = new Job(conf, "invert index");
            job.setJarByClass(InvertedIndexer.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setMapperClass(InvertedIndexMapper.class);
            job.setMapOutputValueClass(IntWritable.class);
            job.setCombinerClass(SumCombiner.class);
            job.setPartitionerClass(NewPartitioner.class);
            job.setReducerClass(InvertedIndexReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            FileInputFormat.addInputPath(job, new Path(args[0]));

            // if the output directory exists, delete it
            // or it will throw an org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory output already exists
            // powerxing.com/isntall-hadoop
            Path outputPath = new Path(args[1]);
            outputPath.getFileSystem(conf).delete(outputPath, true);

            FileOutputFormat.setOutputPath(job, new Path(args[1]));
            System.exit(job.waitForCompletion(true)? 0:1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}