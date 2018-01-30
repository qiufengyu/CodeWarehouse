package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.PropertyConfigurator;

import java.util.Random;


/**
 * Hello world!
 *
 */
public class FrequencySort
{
    public static void main( String[] args ) {
        PropertyConfigurator.configure("log4j.properties");

        if (args.length != 2) {
            System.err.println("Usage: FrequencySort args[0] args[1]");
            System.exit(-1);
        }
        System.out.println("Inverted Indexer Program Running...");
        try {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "frequency sort - inverted indexer");
            job.setJarByClass(InvertedIndexer.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setMapperClass(InvertedIndexMapper.class);
            job.setCombinerClass(SumCombiner.class);
            job.setReducerClass(InvertedIndexReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            FileInputFormat.addInputPath(job, new Path(args[0]));

            // a temp directory for the first map-reduce job output
            // output of the first job, input of the second job
            Path tempDir = new Path("frequency-sort-temp-" + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
            FileOutputFormat.setOutputPath(job, tempDir);

            if (job.waitForCompletion(true)) {
                Job sortJob = Job.getInstance(conf, "frequency sort - sort");
                sortJob.setJarByClass(FrequencySort.class);
                FileInputFormat.addInputPath(sortJob, tempDir);
                sortJob.setInputFormatClass(TextInputFormat.class);
                // word-frequency -> frequency-word
                sortJob.setMapperClass(FrequencySortMapper.class);
                sortJob.setMapOutputKeyClass(DoubleWritable.class);
                sortJob.setMapOutputValueClass(Text.class);

                // Default by increasing sorting, rewrite by decreasing number
                sortJob.setSortComparatorClass(DoubleWritableDecreasingComparator.class);

                // frequency-word -> word-frequency
                sortJob.setReducerClass(FrequencySortReducer.class);
                FileOutputFormat.setOutputPath(sortJob, new Path(args[1]));
                sortJob.setOutputKeyClass(Text.class);
                sortJob.setOutputValueClass(DoubleWritable.class);

                Path outputPath = new Path(args[1]);
                outputPath.getFileSystem(conf).delete(outputPath, true);

                if (sortJob.waitForCompletion(true)) {
                    tempDir.getFileSystem(conf).delete(tempDir, true);
                    System.exit(0);
                } else {
                    System.exit(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
