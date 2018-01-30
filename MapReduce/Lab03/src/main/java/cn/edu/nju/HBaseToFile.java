package cn.edu.nju;

/**
 * Created by shenr on 2016/11/2.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.PropertyConfigurator;

public class HBaseToFile {
    public static void main(String[] args) throws Exception {

        PropertyConfigurator.configure("log4j.properties");
        if(args.length != 3) {
            System.err.println("Usage: InvertedIndexer args[0](input files) args[1](output files) args[2](local file from hbase)");
            System.exit(-1);
        }

        if(InvertedIndexer.init(args[0], args[1]) == 0) {

            String tableName = "Wuxia";
            Configuration conf = HBaseConfiguration.create();

            Job job = Job.getInstance(conf, "HBaseToFile");

            job.setJarByClass(WordCountHBaseReaderMapper.class);

            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);

            TableMapReduceUtil.initTableMapperJob(tableName, scan, WordCountHBaseReaderMapper.class, Text.class, DoubleWritable.class, job);

            //设置任务数据的输出路径；
            Path outputPath = new Path(args[2]);
            outputPath.getFileSystem(conf).delete(outputPath, true);

            FileOutputFormat.setOutputPath(job, new Path(args[2]));

            job.setReducerClass(WordCountHBaseReaderReducer.class);

            //调用job.waitForCompletion(true) 执行任务，执行成功后退出；
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }
        else {
            System.exit(-1);
        }
    }
}
