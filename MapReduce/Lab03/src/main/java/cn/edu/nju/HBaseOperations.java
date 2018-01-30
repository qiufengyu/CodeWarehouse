package cn.edu.nju;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.codehaus.jackson.map.DeserializerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by hadoop on 16-11-3.
 */
public class HBaseOperations {
    private static final String TABLE_NAME = "Wuxia";
    private static final byte[] CF_DEFAULT = "ColumnFamily".getBytes();
    private static final byte[] QUALIFIER_NAME = "count".getBytes();

    public void createTable(Configuration config) throws IOException {
        Connection connection = ConnectionFactory.createConnection(config);
        Admin admin = connection.getAdmin();
        HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
        tableDescriptor.addFamily(new HColumnDescriptor(CF_DEFAULT));
        createOrOverwrite(admin, tableDescriptor);
        connection.close();
    }

    public void createOrOverwrite(Admin admin, HTableDescriptor tableDescriptor) throws IOException {
        if(admin.tableExists(tableDescriptor.getTableName())) {
            admin.disableTable(tableDescriptor.getTableName());
            admin.deleteTable(tableDescriptor.getTableName());
        }
        admin.createTable(tableDescriptor);
    }

    public void addTable(Configuration config, String word, double count) throws IOException {
        Connection connection = ConnectionFactory.createConnection(config);
        Table table = connection.getTable(TableName.valueOf(TABLE_NAME));
        Put put = new Put(Bytes.toBytes(word));
        put.addColumn(CF_DEFAULT, QUALIFIER_NAME, Bytes.toBytes(count));
        table.put(put);

        table.close();
        connection.close();
    }

    public void addTableList(Configuration config, List<Put> putList) throws IOException {
        Connection connection = ConnectionFactory.createConnection(config);
        Table table = connection.getTable(TableName.valueOf(TABLE_NAME));

        table.put(putList);

        table.close();
        connection.close();
    }
}
