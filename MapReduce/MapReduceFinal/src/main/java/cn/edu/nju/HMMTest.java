package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.yarn.util.SystemClock;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.*;

/**
 * Created by hadoop on 17-2-24.
 */
public class HMMTest {

    static long startTime;
    static long endTime;


    public static boolean HMMTestMain(String[] args) throws IOException {

        PropertyConfigurator.configure("log4j.properties");

        startTime = System.currentTimeMillis();

        Configuration conf = new Configuration();

        try {
            Job job = Job.getInstance(conf, "HMM - testing");
            job.setJarByClass(HMMTest.class);
            job.setInputFormatClass(TextInputFormat.class);

            job.setMapperClass(HMMTest.TestMapper.class);
            job.setCombinerClass(HMMTest.TestCombiner.class);
            job.setReducerClass(HMMTest.TestReducer.class);

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            job.setNumReduceTasks(1);

            MultipleOutputs.addNamedOutput(job, "Result", TextOutputFormat.class, Text.class, Text.class);
            MultipleOutputs.addNamedOutput(job, "Accuracy", TextOutputFormat.class, Text.class, Text.class);

            FileInputFormat.addInputPath(job, new Path(args[0]));

            //Path outputPath = new Path(args[3]);
            Path outputPath = new Path(args[1]);
            outputPath.getFileSystem(conf).delete(outputPath, true);
            //FileOutputFormat.setOutputPath(job, new Path(args[3]);
            FileOutputFormat.setOutputPath(job, new Path(args[1]));

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

    public static boolean readparameter(String path0, String path1) throws IOException {


        return true;
    }

    public static double log_func(double value){
        return Math.log(value);
    }

    public static class TestMapper extends Mapper<Object, Text, Text, Text> {

        static Map<String, Integer> word2Index;
        static Map<String, Integer> tag2Index;
        static Map<Integer, String> index2Tag;

        static int numOfTags;
        static int numOfWords;

        static Vector<Vector<Double>> transitionA;
        static Vector<Vector<Double>> emissionB;
        static Vector<Double> startPi;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            word2Index = new HashMap<String, Integer>();
            tag2Index = new HashMap<String, Integer>();
            index2Tag = new HashMap<Integer, String>();

            Configuration conf2 = new Configuration();
            FileSystem fileSystem = FileSystem.get(conf2);
            // read word - index file
            Path pathWord = new Path("output1/WordVoc-r-00000");
            if(!fileSystem.exists(pathWord)) {
                System.err.println("File output1/WordVoc-r-00000 does not exist!");
                System.exit(-2);
            }
            FSDataInputStream in = fileSystem.open(pathWord);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            int n = 0;
            while(true) {
                line = br.readLine();
                if(line == null)
                    break;
                line = line.trim();
                word2Index.put(line, n);
                n++;
            }
            word2Index.put("unk", n);
            br.close();
            in.close();
            numOfWords = word2Index.size();

            // read tag - index file
            Path pathWord2 = new Path("output1/POSTag-r-00000");
            if(!fileSystem.exists(pathWord2)) {
                System.err.println("File output1/POSTag-r-00000 does not exists!");
                System.exit(-2);
            }

            FSDataInputStream in2 = fileSystem.open(pathWord2);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
            n = 0;
            while(true) {
                line = br2.readLine();
                if(line == null)
                    break;
                line = line.trim();
                tag2Index.put(line, n);
                index2Tag.put(n, line);
                n++;
            }
            br2.close();
            in2.close();
            numOfTags = tag2Index.size();

            // read start file
            startPi = new Vector<Double>();
            Path pathStart = new Path("output2/Start-r-00000");
            if(!fileSystem.exists(pathStart)) {
                System.err.println("File output2/Start-r-00000 does not exists!");
                System.exit(-2);
            }
            FSDataInputStream in3 = fileSystem.open(pathStart);
            BufferedReader br3 = new BufferedReader(new InputStreamReader(in3));
            n = 0;
            while(true) {
                line = br3.readLine();
                if(line == null)
                    break;
                line = line.trim();
                startPi.add(n, Double.valueOf(line));
                n++;
            }
            br3.close();
            in3.close();

            // read emission: tag x word matrix
            emissionB = new Vector<Vector<Double>>();

            for(int i = 0; i<numOfTags; ++i) {
                Vector<Double> vtemp = new Vector<Double>();
                for(int j = 0; j<numOfWords; ++j) {
                    vtemp.add(j, 1e-8);
                }
                emissionB.add(i, vtemp);
            }
            Path pathEmission = new Path("output2/Emission-r-00000");
            if(!fileSystem.exists(pathEmission)) {
                System.err.println("File output2/Emission-r-00000 does not exists!");
                System.exit(-2);
            }
            FSDataInputStream in4 = fileSystem.open(pathEmission);
            BufferedReader br4 = new BufferedReader(new InputStreamReader(in4));

            n = 0;
            while (true) {
                line = br4.readLine();
                if(line == null)
                    break;
                line = line.trim();
                if(line.length() > 2) {
                    String[] s = line.split(" ");
                    String[] ind = s[0].split(",");
                    int x = Integer.valueOf(ind[0]);
                    int y = Integer.valueOf(ind[1]);
                    double z = Double.valueOf(s[1]);
                    emissionB.get(x).set(y, z);
                }
                n++;
            }
            br4.close();
            in4.close();

            // read transition tag x tag matrix
            transitionA = new Vector<Vector<Double>>();
            Path pathTransition = new Path("output2/Transition-r-00000");
            if(!fileSystem.exists(pathTransition)) {
                System.err.println("File output2/Transition-r-00000 does not exists!");
                System.exit(-2);
            }
            FSDataInputStream in5 = fileSystem.open(pathTransition);
            BufferedReader br5 = new BufferedReader(new InputStreamReader(in5));

            n = 0;
            while (true) {
                line = br5.readLine();
                if(line == null)
                    break;
                line = line.trim();
                Vector l = new Vector<Double>();
                String[] s = line.split(" ");
                for(int i = 0; i<s.length; i++) {
                    l.add(i, Double.valueOf(s[i]));
                }
                transitionA.add(n, l);
                n++;
            }
            br5.close();
            in5.close();

        }

        //Text t = new Text();
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = Dictionary.clean(value.toString());
            String[] tokens = line.split("\\s");
            int sentenceLen = tokens.length;
            double[][] T1 = new double[numOfTags][sentenceLen];
            int[][] T2 = new int[numOfTags][sentenceLen];
            Stack<String> s = new Stack<String>();

            // first word
            String w1 = tokens[0].split("/")[0];
            int w1Index = numOfWords-1;
            if(word2Index.get(w1) != null) {
                w1Index = (int)word2Index.get(w1);
            }
            for(int i = 0; i<numOfTags; ++i) {
                T1[i][0] = log_func(startPi.get(i))+log_func(emissionB.get(i).get(w1Index));
                T2[i][0] = -1;
            }

            for(int i = 1; i<sentenceLen; ++i) {
                int wiIndex = numOfWords-1;
                String wi = tokens[i].split("/")[0];
                if(word2Index.get(wi) != null) {
                    wiIndex = (int)word2Index.get(wi);
                }
                for(int j = 0; j<numOfTags; ++j) {
                    int maxIndex = -1;
                    double maxValue = -1.0e99;
                    for(int k = 0; k<numOfTags; ++k) {
                        double temp = T1[k][i-1]+log_func(transitionA.get(k).get(j)) + log_func(emissionB.get(j).get(wiIndex));
                        if(temp > maxValue) {
                            maxValue = temp;
                            maxIndex = k;
                        }
                    }
                    T1[j][i] = maxValue;
                    T2[j][i] = maxIndex;
                }
            }

            // backward, the last tag
            double maxValue = -1e99;
            int maxIndex = -1;
            for(int k = 0; k<numOfTags; ++k) {
                if(T1[k][sentenceLen-1] > maxValue) {
                    maxValue = T1[k][sentenceLen-1];
                    maxIndex = k;
                }
            }
            s.push(index2Tag.get(maxIndex));

            // backward -> first
            for(int i = sentenceLen-1; i>0; i--) {
                maxIndex = T2[maxIndex][i];
                s.push(index2Tag.get(maxIndex));
            }

            StringBuilder out = new StringBuilder();
            for(int i = 0; i<sentenceLen; ++i) {
                String[] pairs = tokens[i].split("/");
                String tagi;
                if(pairs.length == 2) {
                    tagi = pairs[1];
                    String predi = s.pop();
                    if(tagi.equals(predi)) {
                        context.write(new Text("_correct"), new Text("1"));
                    }
                    else {
                        context.write(new Text("_error"), new Text("1"));
                    }
                    out.append(pairs[0]+"/"+predi+" ");
                }
            }
            context.write(new Text("_sentence"), new Text(out.toString().trim()));

        }
    }

    public static class TestCombiner extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String k = key.toString();
            if(k.startsWith("_sentence")) {
                for(Text value: values) {
                    context.write(new Text("_sentence"), value);
                }
            }
            else if(k.startsWith("_correct")) {
                long s = 0;
                for(Text value: values) {
                    s = s + Long.valueOf(value.toString());
                }
                context.write(new Text("_correct"), new Text(String.valueOf(s)));
            }
            else if(k.startsWith("_error")) {
                long s = 0;
                for(Text value: values) {
                    s = s + Long.valueOf(value.toString());
                }
                context.write(new Text("_error"), new Text(String.valueOf(s)));
            }
        }
    }


    public static class TestReducer extends Reducer<Text, Text, Text, Text> {

        private MultipleOutputs mos;

        static long correct = 0;
        static long error = 0;

        protected void setup(Context context) throws IOException, InterruptedException {
            mos = new MultipleOutputs<Text, Text>(context);
        }

        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            String k = key.toString();

            if(k.startsWith("_sentence")) {
                for(Text value: values) {
                    mos.write("Result", value, new Text(""));
                }
            }
            else {

                long s = 0;

                for(Text value: values) {
                    s = s + Long.valueOf(value.toString());
                }

                if(k.startsWith("_correct"))
                    correct = s;
                else if(k.startsWith("_error"))
                    error = s;
            }
        }

        protected void cleanup(Context context) throws IOException, InterruptedException {

            endTime = System.currentTimeMillis();

            // System.out.println("Time: "+(endTime-startTime)/1000.0+"s");
            // System.out.printf("Precision: %.2f%%\n",(double)correct*(double)100/(double)(correct+error));

            String time = "Time: "+(endTime-startTime)/1000.0+"s";
            String c = "Correct: "+correct;
            String t = "Total: "+(correct+error);
            String acc = String.format("Precision: %.2f%%",(double)correct*(double)100/(double)(correct+error));
            // context.write(new IntWritable(1), new Text("Correct = "+correct));
            // context.write(new IntWritable(2), new Text("Total = "+(correct+error)));
            // context.write(new IntWritable(3), new Text(acc));
            mos.write("Accuracy", new Text(time), new Text());
            mos.write("Accuracy", new Text(c), new Text());
            mos.write("Accuracy", new Text(t), new Text());
            mos.write("Accuracy", new Text(acc), new Text());

            mos.close();

        }
    }

    public static void tmain( String[] args ) throws IOException {

        //args[0] : path of test file
        //args[1] : path of result


        if (args.length != 2) {
            System.err.println("Usage: cn.edu.nju.HMMTest args[0] args[1]");
            System.exit(-1);
        }



        //if (Dictionary.dict(args)) {
        //    HMMTrain.HMMTrainMain(args);
        // }
    }
}
