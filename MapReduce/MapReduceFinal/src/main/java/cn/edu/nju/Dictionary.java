package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


import java.io.IOException;
import java.util.HashSet;

/**
 * Created by Godfray on 2017/2/22.
 */
public class Dictionary {

    private static HashSet<String> wordSet = new HashSet<String>();
    private static HashSet<String> posSet = new HashSet<String>();

    public static boolean dict(String[] args) {
        Configuration conf = new Configuration();
        try {
            Job job = Job.getInstance(conf, "HMM - generate dictionary");
            job.setJarByClass(Dictionary.class);
            job.setInputFormatClass(TextInputFormat.class);
            job.setOutputFormatClass(TextOutputFormat.class);
            job.setMapperClass(DictionaryMapper.class);
            job.setReducerClass(DictionaryReducer.class);

            MultipleOutputs.addNamedOutput(job, "WordVoc", TextOutputFormat.class, Text.class, NullWritable.class);
            MultipleOutputs.addNamedOutput(job, "POSTag", TextOutputFormat.class, Text.class, NullWritable.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(NullWritable.class);
            FileInputFormat.addInputPath(job, new Path(args[0]));
            Path outputPath = new Path("output1");
            outputPath.getFileSystem(conf).delete(outputPath, true);
            FileOutputFormat.setOutputPath(job, new Path("output1"));

            return job.waitForCompletion(true);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }



    public static class DictionaryMapper extends Mapper<Object, Text, Text, NullWritable> {
        Text k = new Text();

        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = clean(value.toString());
            if(line.length() > 2) {
                String[] parts = line.split("\\s");
                for(int i = 3; i<parts.length; ++i) {
                    String x = parts[i];
                    if(x.length() > 2) {
                        String[] wpos = x.split("/");
                        if(wpos.length == 2 && wpos[0].length() > 0) {
                            String word = wpos[0];
                            String tag = wpos[1];
                            if(!wordSet.contains(word)) {
                                k.set(word);
                                wordSet.add(word);
                                context.write(k, NullWritable.get());
                            }
                            if(!posSet.contains(tag)) {
                                /*
                                if(tag.startsWith("01")) {
                                    FileSplit fs = (FileSplit)context.getInputSplit();
                                    String fileName = fs.getPath().getName();
                                    System.err.println(fileName);
                                    System.exit(-3);
                                }
                                */
                                k.set("_#t" + tag);
                                posSet.add(tag);
                                context.write(k, NullWritable.get());
                            }
                        }
                    }
                }
            }
        }
    }

    public static class DictionaryReducer extends Reducer<Text, NullWritable, Text, NullWritable> {

        private MultipleOutputs mos;

        protected void setup(Context context) {
            mos = new MultipleOutputs<Text, NullWritable>(context);
        }

        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            if (key.toString().startsWith("_#t")) {
                // part of speech tags
                String x = key.toString().substring(3);
                mos.write("POSTag", new Text(x), NullWritable.get());
            } else {
                mos.write("WordVoc", key, NullWritable.get());
            }
        }

        protected void cleanup(Context context) throws IOException, InterruptedException {
            mos.close();
        }
    }


    public static String clean(String s) {
        String t = s.trim();
        t = t.replaceAll(" \\[", "");
        t = t.replaceAll("\\] ", "");
        return t;
    }


}
