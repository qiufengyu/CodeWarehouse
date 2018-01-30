package cn.edu.nju;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by shenr on 2016/11/2.
 */

public class WordCountHBaseReaderMapper extends TableMapper<Text, DoubleWritable> {

    private static final byte[] CF_DEFAULT = "ColumnFamily".getBytes();
    private static final byte[] QUALIFIER_NAME = "count".getBytes();

    protected void map(ImmutableBytesWritable key, Result value, Context context)
            throws IOException, InterruptedException {

        double average = 0.0;
        byte[] temp = value.getValue(CF_DEFAULT, QUALIFIER_NAME);

        average = ByteBuffer.wrap(temp).getDouble();

        context.write(new Text(key.get()), new DoubleWritable(average));

    }
}
