package com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class HBaseTest {

    // 表名和列族
    private static final String TABLE_NAME = "test_table";
    private static final String COLUMN_FAMILY = "cf";
    private static final TableName TABLE = TableName.valueOf(TABLE_NAME);

    private static Connection connection;
    private static Admin admin;

    public static void main(String[] args) {
        System.out.println("HBase Connection Test Started!");

        try {
            // 初始化连接
            initConnection();

            // 删除旧表
            deleteTableIfExists(TABLE);

            // 创建表
            createTable(TABLE, COLUMN_FAMILY);

            // 插入数据
            insertData();

            // 查询数据
            queryData();

            // 更新数据
            updateData();

            // 查询数据
            System.out.println("\n--- After Update Query ---");
            queryData();

            // 删除数据
            deleteData();

            // 验证删除结果
            System.out.println("\n--- After Delete Query ---");
            queryData();

            // 删除表
            deleteTable(TABLE);

            System.out.println("\n");
            System.out.println("[Congratulations]    ✓ All Tests Completed Successfully");

        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            closeResources();
        }
    }

    /**
     * 初始化 HBase 连接
     */
    private static void initConnection() throws IOException {
        System.out.println("[1/10] Initializing HBase Connection...");
        
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "hadoop-hbase");
        config.set("hbase.zookeeper.property.clientPort", "2181");

        connection = ConnectionFactory.createConnection(config);
        admin = connection.getAdmin();

        System.out.println("✓ Connection established successfully!\n");
    }

    /**
     * 如果表存在则删除
     */
    private static void deleteTableIfExists(TableName tableName) throws IOException {
        if (admin.tableExists(tableName)) {
            System.out.println("[1.5/10] Cleaning up old table...");
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("✓ Old table deleted\n");
        }
    }

    /**
     * 创建表
     */
    private static void createTable(TableName tableName, String columnFamily) throws IOException {
        System.out.println("[2/10] Creating Table '" + TABLE_NAME + "'...");

        HTableDescriptor tableDesc = new HTableDescriptor(tableName);
        HColumnDescriptor colDesc = new HColumnDescriptor(columnFamily);
        tableDesc.addFamily(colDesc);

        admin.createTable(tableDesc);
        System.out.println("✓ Table created successfully with column family: " + columnFamily + "\n");
    }

    /**
     * 插入数据
     */
    private static void insertData() throws IOException {
        System.out.println("[3/10] Inserting Test Data...");

        Table table = connection.getTable(TABLE);

        try {
            // 插入第一行
            Put put1 = new Put(Bytes.toBytes("row1"));
            put1.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("name"), Bytes.toBytes("Alice"));
            put1.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age"), Bytes.toBytes("25"));
            put1.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("city"), Bytes.toBytes("Shanghai"));
            table.put(put1);
            System.out.println("  → Inserted row1: name=Alice, age=25, city=Shanghai");

            // 插入第二行
            Put put2 = new Put(Bytes.toBytes("row2"));
            put2.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("name"), Bytes.toBytes("Bob"));
            put2.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age"), Bytes.toBytes("30"));
            put2.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("city"), Bytes.toBytes("Beijing"));
            table.put(put2);
            System.out.println("  → Inserted row2: name=Bob, age=30, city=Beijing");

            // 插入第三行
            Put put3 = new Put(Bytes.toBytes("row3"));
            put3.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("name"), Bytes.toBytes("Charlie"));
            put3.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age"), Bytes.toBytes("28"));
            put3.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("city"), Bytes.toBytes("Shenzhen"));
            table.put(put3);
            System.out.println("  → Inserted row3: name=Charlie, age=28, city=Shenzhen");

            System.out.println("✓ Data inserted successfully\n");
        } finally {
            table.close();
        }
    }

    /**
     * 查询数据
     */
    private static void queryData() throws IOException {
        System.out.println("[4/10] Querying Data From Table...");

        Table table = connection.getTable(TABLE);

        try {
            // 扫描表中所有数据
            Scan scan = new Scan();
            ResultScanner scanner = table.getScanner(scan);

            boolean hasData = false;
            for (Result result : scanner) {
                hasData = true;
                String rowKey = Bytes.toString(result.getRow());
                String name = Bytes.toString(result.getValue(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("name")));
                String age = Bytes.toString(result.getValue(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age")));
                String city = Bytes.toString(result.getValue(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("city")));

                System.out.println("  → " + rowKey + ": name=" + name + ", age=" + age + ", city=" + city);
            }

            if (!hasData) {
                System.out.println("  → No data found in table");
            }

            scanner.close();
            System.out.println("✓ Query completed\n");
        } finally {
            table.close();
        }
    }

    /**
     * 更新数据
     */
    private static void updateData() throws IOException {
        System.out.println("[5/10] Updating Data...");

        Table table = connection.getTable(TABLE);

        try {
            // 更新 row1 の age
            Put put = new Put(Bytes.toBytes("row1"));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age"), Bytes.toBytes("26"));
            table.put(put);
            System.out.println("  → Updated row1: age changed to 26");

            System.out.println("✓ Update completed\n");
        } finally {
            table.close();
        }
    }

    /**
     * 删除数据
     */
    private static void deleteData() throws IOException {
        System.out.println("[6/10] Deleting Test Data...");

        Table table = connection.getTable(TABLE);

        try {
            // 删除第一行
            Delete delete1 = new Delete(Bytes.toBytes("row1"));
            table.delete(delete1);
            System.out.println("  → Deleted row1");

            // 删除第二行
            Delete delete2 = new Delete(Bytes.toBytes("row2"));
            table.delete(delete2);
            System.out.println("  → Deleted row2");

            // 删除第三行
            Delete delete3 = new Delete(Bytes.toBytes("row3"));
            table.delete(delete3);
            System.out.println("  → Deleted row3");

            System.out.println("✓ Data deletion completed\n");
        } finally {
            table.close();
        }
    }

    /**
     * 删除表
     */
    private static void deleteTable(TableName tableName) throws IOException {
        System.out.println("[7/10] Deleting Table '" + TABLE_NAME + "'...");

        if (admin.tableExists(tableName)) {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("✓ Table deleted successfully\n");
        }
    }

    /**
     * 关闭所有资源
     */
    private static void closeResources() {
        System.out.println("[8/10] Closing Resources...");
        try {
            if (admin != null) {
                admin.close();
            }
            if (connection != null) {
                connection.close();
            }
            System.out.println("✓ Resources closed\n");
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}
