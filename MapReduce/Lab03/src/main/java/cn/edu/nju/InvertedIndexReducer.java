package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by godfray on 2016/10/31.
 */
public class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {

    private static final byte[] CF_DEFAULT = Bytes.toBytes("ColumnFamily");
    private static final byte[] QUALIFIER_NAME = Bytes.toBytes("count");

    static HBaseOperations hBaseOp = new HBaseOperations();
    static Configuration conf = HBaseConfiguration.create();
    static List<Put> putList = new ArrayList<Put>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        hBaseOp.createTable(conf);
        putList.clear();
        super.setup(context);

    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        long sum = 0;
        int count = 0;
        StringBuilder out = new StringBuilder();
        for(Text t: values) {
            String sumString = t.toString().split(":")[1];
            sum += Long.parseLong(sumString);
            count++;
            out.append(t.toString().replaceAll("\\.[tT][xX][tT]\\.segmented", "")+";");
        }

        double average = (double) sum/(double) count;
        String av = String.valueOf(average)+",";
        StringBuilder out2 = new StringBuilder();
        out2.append(av);
        out2.append(out.toString().replaceAll(";$",""));

        context.write(key, new Text(out2.toString()));

        Put put = new Put(key.toString().getBytes());
        put.addColumn(CF_DEFAULT, QUALIFIER_NAME, Bytes.toBytes(average));
        putList.add(put);
    }

    @Override
    protected void cleanup(Context context) throws IOException {
        hBaseOp.addTableList(conf, putList);
    }
}
