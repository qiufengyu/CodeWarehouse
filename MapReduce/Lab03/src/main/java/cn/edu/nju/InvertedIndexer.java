package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.PropertyConfigurator;


public class InvertedIndexer {


    /**
     * init do inverted indexer first
     * write into hbase
     * @param args0 input path
     * @param args1 output path
     * @return 0, ok 1/-1 error
     */
    public static int init( String args0, String args1 ) {

        PropertyConfigurator.configure("log4j.properties");

        System.out.println("Job Inverted Indexer Running...");
        try {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "Inverted Indexer");
            job.setJarByClass(InvertedIndexer.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setMapperClass(InvertedIndexMapper.class);
            job.setCombinerClass(SumCombiner.class);
            // job.setPartitionerClass(NewPartitioner.class);
            job.setReducerClass(InvertedIndexReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            FileInputFormat.addInputPath(job, new Path(args0));

            // if the output directory exists, delete it
            // or it will throw an org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory output already exists
            // visit http://powerxing.com/isntall-hadoop
            Path outputPath = new Path(args1);
            outputPath.getFileSystem(conf).delete(outputPath, true);

            FileOutputFormat.setOutputPath(job, new Path(args1));
            return job.waitForCompletion(true)? 0:1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}