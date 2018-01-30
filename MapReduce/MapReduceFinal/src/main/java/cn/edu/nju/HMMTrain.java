package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.util.EnumCounters;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Godfray on 2017/2/23.
 */
public class HMMTrain {

    public static boolean HMMTrainMain(String[] args) throws IOException {
        /*
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("output1/WordVoc-r-00000")));
        String line;
        int n = 0;
        while(true) {
            line = br.readLine();
            if(line == null)
                break;
            line = line.trim();
            word2Index.put(line, n);
            index2Word.put(n, line);
            n++;
        }
        word2Index.put("unk", n);
        index2Word.put(n, "unk");
        numOfWords = word2Index.size();
        br.close();

        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("output1/POSTag-r-00000")));
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
        numOfTags = tag2Index.size();
        */

        try {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "HMM - training");
            job.setJarByClass(HMMTrain.class);
            job.setInputFormatClass(TextInputFormat.class);

            // job.addCacheFile(new Path("output1/POSTag-r-00000").toUri());
            // job.addCacheFile(new Path("output1/WordVoc-r-00000").toUri());

            job.setMapperClass(TrainMapper.class);
            job.setCombinerClass(TrainCombiner.class);
            job.setReducerClass(TrainReducer.class);

            job.setNumReduceTasks(1);

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(LongWritable.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(NullWritable.class);

            MultipleOutputs.addNamedOutput(job, "Transition", TextOutputFormat.class, Text.class, NullWritable.class);
            MultipleOutputs.addNamedOutput(job, "Emission", TextOutputFormat.class, Text.class, NullWritable.class);
            MultipleOutputs.addNamedOutput(job, "Start", TextOutputFormat.class, Text.class, NullWritable.class);

            FileInputFormat.addInputPath(job, new Path(args[0]));

            Path outputPath = new Path("output2");
            outputPath.getFileSystem(conf).delete(outputPath, true);
            FileOutputFormat.setOutputPath(job, new Path("output2"));

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


    public static class TrainMapper extends Mapper<Object, Text, Text, LongWritable> {

        Text t = new Text();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {


        }


        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = Dictionary.clean(value.toString());
            if(line.length() > 2) {
                String[] parts = line.split("\\s+");
                String startWordTag = parts[0];
                if(startWordTag.length() > 2) {
                    String[] wt = startWordTag.split("/");
                    if(wt.length == 2) {
                        t.set("start_"+wt[1]);
                        context.write(t, new LongWritable(1));
                        t.set("emit_"+wt[1]+"_"+wt[0]);
                        context.write(t, new LongWritable(1));
                    }

                }
                for(int i = 1; i<parts.length; ++i) {
                    // the corpus format: sentenceID,[ ](space)\t\t,sentence(word/tag)
                    String x = parts[i-1];
                    String y = parts[i];
                    String[] wtx = x.split("/");
                    String[] wty = y.split("/");
                    if(wtx.length == 2 && wty.length == 2) {
                        String xtag = wtx[1];
                        String ytag = wty[1];
                        String yword = wty[0];
                        t.set("trans_"+xtag+"_"+ytag);
                        context.write(t, new LongWritable(1));
                        t.set("emit_"+ytag+"_"+yword);
                        context.write(t, new LongWritable(1));
                    }
                }
            }
        }

    }

    public static class TrainCombiner extends Reducer<Text, LongWritable, Text, LongWritable> {

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
        }

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long s = 0;
            for(LongWritable value: values) {
                s = s + value.get();
            }
            context.write(key, new LongWritable(s));
        }
    }


    public static class TrainReducer extends Reducer<Text, LongWritable, Text, NullWritable> {

        private MultipleOutputs mos;

        static Map<String, Integer> word2Index;
        // static Map<Integer, String> index2Word;

        static Map<String, Integer> tag2Index;
        // static Map<Integer, String> index2Tag;

        static Vector<Vector<Double>> transitionA;
        static Vector<Vector<Double>> emissionB;
        static Vector<Double> startPi;

        static int numOfTags;
        static int numOfWords;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {

            word2Index = new HashMap<String, Integer>();
            // index2Word = new HashMap<Integer, String>();
            tag2Index = new HashMap<String, Integer>();
            // index2Tag = new HashMap<Integer, String>();

            Configuration conf2 = new Configuration();

            FileSystem fileSystem = FileSystem.get(conf2);

            Path pathWord = new Path("output1/WordVoc-r-00000");
            if(!fileSystem.exists(pathWord)) {
                System.err.println("File output1/WordVoc-r-00000 does not exists!");
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
                // index2Word.put(n, line);
                n++;
            }
            word2Index.put("unk", n);
            // index2Word.put(n, "unk");
            numOfWords = word2Index.size();


            br.close();

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
                // index2Tag.put(n, line);
                n++;
            }
            br2.close();
            numOfTags = tag2Index.size();

            mos = new MultipleOutputs<Text, NullWritable>(context);

            transitionA = new Vector<Vector<Double>>();
            emissionB = new Vector<Vector<Double>>();
            startPi = new Vector<Double>();

            for(int i = 0; i<numOfTags; ++i) {
                Vector<Double> tempA = new Vector<Double>();
                for(int j = 0; j<numOfTags; ++j)
                    tempA.add(0.0);
                transitionA.add(tempA);
                Vector<Double> tempB = new Vector<Double>();
                for(int j = 0; j<numOfWords; ++j)
                    tempB.add(0.0);
                emissionB.add(tempB);
                startPi.add(0.0);
            }
        }

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long s = 0;
            for(LongWritable value: values) {
                s = s + value.get();
            }
            String k = key.toString();
            if(k.startsWith("start")) {
                String[] st = k.split("_");
                if(st.length == 2) {
                    String state = st[1];
                    int stateIndex = tag2Index.get(state);
                    double old = startPi.get(stateIndex);
                    startPi.set(stateIndex, old+(s+0.0));
                }
            }
            else if(k.startsWith("trans")) {
                String[] tt = k.split("_");
                if(tt.length == 3) {
                    String state1 = tt[1];
                    String state2 = tt[2];
                    if(tag2Index.get(state1) != null && tag2Index.get(state2) != null) {
                        int stateIndex1 = tag2Index.get(state1);
                        int stateIndex2 = tag2Index.get(state2);
                        double old = transitionA.get(stateIndex1).get(stateIndex2);
                        transitionA.get(stateIndex1).set(stateIndex2, old + (s + 0.0));
                    }
                }
            }
            else if(k.startsWith("emit")) {
                String[] et = k.split("_");
                if(et.length == 3) {
                    String state = et[1];
                    String ob = et[2];
                    if(tag2Index.get(state) != null && word2Index.get(ob) != null) {
                        int stateIndex = tag2Index.get(state);
                        int obIndex = word2Index.get(ob);
                        double old = emissionB.get(stateIndex).get(obIndex);
                        emissionB.get(stateIndex).set(obIndex, old + (s+0.0));
                    }
                }
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {

            Vector<Double> pi = normVector(startPi);

            for(int i = 0; i<transitionA.size(); ++i) {
                Vector<Double> u = normVector(transitionA.get(i));
                transitionA.set(i, u);
                Vector<Double> v = normVector(emissionB.get(i));
                emissionB.set(i, v);
                startPi.set(i, pi.get(i));
            }

            // unknown word
            int unkIndex = word2Index.get("unk");
            System.err.println(unkIndex);
            for(int i = 0; i<emissionB.size(); ++i) {
                Vector<Double> u = emissionB.get(i);
                u.set(unkIndex, 1.0/emissionB.size());
            }

            /*

            System.out.println("startPi = "+ startPi);
            context.write(new Text(startPi.toString()), NullWritable.get());

            System.out.println("trans vector 2: "+transitionA.get(2));
            context.write(new Text(transitionA.get(2).toString()), NullWritable.get());
            double sum = 0.0;
            for(Double d: transitionA.get(2)) {
                sum += d.doubleValue();
            }
            System.out.println("sum of vector 2: "+sum);
            context.write(new Text(String.valueOf(sum)), NullWritable.get());

            */

            /**
             * write to file
             */

            for(Double d: startPi) {
                mos.write("Start", new Text(String.valueOf(d)), NullWritable.get());
            }

            for(Vector<Double> v: transitionA) {
                StringBuilder outTrans = new StringBuilder();
                for(Double d: v) {
                    double x = d.doubleValue();
                    outTrans.append(String.valueOf(x)+" ");
                }
                mos.write("Transition", new Text(outTrans.toString().trim()), NullWritable.get());
            }

            int i = 0;
            for(Vector<Double> v: emissionB) {
                // StringBuilder outEmit = new StringBuilder();
                int j = 0;
                for(Double d: v) {
                    double x = d.doubleValue();
                    // outEmit.append(String.valueOf(x)+" ");
                    if(x > 1e-8) {
                        String t = i+","+j+" "+x;
                        mos.write("Emission", new Text(t), NullWritable.get());
                    }
                    j++;
                }
                i++;
            }

            mos.close();

            //clean memory
            /*
            transitionA.clear();
            emissionB.clear();
            startPi.clear();

            tag2Index.clear();
            index2Tag.clear();
            index2Word.clear();
            word2Index.clear();
            */
        }
    }


    private static Vector<Double> normVector(Vector<Double> v) {

        Vector<Double> temp = new Vector<Double>();
        double x = 0.0;
        for(Double d: v) {
            if(d != null)
                x += d.doubleValue();
        }

        for(Double d: v) {
            temp.add(d.doubleValue() / x);
        }

        return temp;
    }





}
