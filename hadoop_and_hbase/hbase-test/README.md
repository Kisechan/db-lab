# HBase Test

Build a simple Maven project to test HBase connection and basic operations.

## Prerequisites

- Java 11+
- Maven 3.6+
- HBase running at `hadoop-hbase:2181`

## Code

Write the following code in the `src/main/java/com/example/HBaseTest.java` file:

```java
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

    private static final String TABLE_NAME = "test_table";
    private static final String COLUMN_FAMILY = "cf";
    private static final TableName TABLE = TableName.valueOf(TABLE_NAME);

    private static Connection connection;
    private static Admin admin;

    public static void main(String[] args) {
        System.out.println("HBase Connection Test Started!");

        try {
            initConnection();
            deleteTableIfExists(TABLE);
            createTable(TABLE, COLUMN_FAMILY);
            insertData();
            queryData();
            updateData();
            System.out.println("\n--- After Update Query ---");
            queryData();
            deleteData();
            System.out.println("\n--- After Delete Query ---");
            queryData();
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

    private static void initConnection() throws IOException {
        System.out.println("[1/10] Initializing HBase Connection...");
        
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "hadoop-hbase");
        config.set("hbase.zookeeper.property.clientPort", "2181");

        connection = ConnectionFactory.createConnection(config);
        admin = connection.getAdmin();

        System.out.println("✓ Connection established successfully!\n");
    }

    private static void deleteTableIfExists(TableName tableName) throws IOException {
        if (admin.tableExists(tableName)) {
            System.out.println("[1.5/10] Cleaning up old table...");
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("✓ Old table deleted\n");
        }
    }

    private static void createTable(TableName tableName, String columnFamily) throws IOException {
        System.out.println("[2/10] Creating Table '" + TABLE_NAME + "'...");

        HTableDescriptor tableDesc = new HTableDescriptor(tableName);
        HColumnDescriptor colDesc = new HColumnDescriptor(columnFamily);
        tableDesc.addFamily(colDesc);

        admin.createTable(tableDesc);
        System.out.println("✓ Table created successfully with column family: " + columnFamily + "\n");
    }

    private static void insertData() throws IOException {
        System.out.println("[3/10] Inserting Test Data...");

        Table table = connection.getTable(TABLE);

        try {
            Put put1 = new Put(Bytes.toBytes("row1"));
            put1.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("name"), Bytes.toBytes("Alice"));
            put1.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age"), Bytes.toBytes("25"));
            put1.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("city"), Bytes.toBytes("Shanghai"));
            table.put(put1);
            System.out.println("  → Inserted row1: name=Alice, age=25, city=Shanghai");

            Put put2 = new Put(Bytes.toBytes("row2"));
            put2.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("name"), Bytes.toBytes("Bob"));
            put2.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age"), Bytes.toBytes("30"));
            put2.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("city"), Bytes.toBytes("Beijing"));
            table.put(put2);
            System.out.println("  → Inserted row2: name=Bob, age=30, city=Beijing");

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

    private static void queryData() throws IOException {
        System.out.println("[4/10] Querying Data From Table...");

        Table table = connection.getTable(TABLE);

        try {
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

    private static void updateData() throws IOException {
        System.out.println("[5/10] Updating Data...");

        Table table = connection.getTable(TABLE);

        try {
            Put put = new Put(Bytes.toBytes("row1"));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("age"), Bytes.toBytes("26"));
            table.put(put);
            System.out.println("  → Updated row1: age changed to 26");

            System.out.println("✓ Update completed\n");
        } finally {
            table.close();
        }
    }

    private static void deleteData() throws IOException {
        System.out.println("[6/10] Deleting Test Data...");

        Table table = connection.getTable(TABLE);

        try {
            Delete delete1 = new Delete(Bytes.toBytes("row1"));
            table.delete(delete1);
            System.out.println("  → Deleted row1");

            Delete delete2 = new Delete(Bytes.toBytes("row2"));
            table.delete(delete2);
            System.out.println("  → Deleted row2");

            Delete delete3 = new Delete(Bytes.toBytes("row3"));
            table.delete(delete3);
            System.out.println("  → Deleted row3");

            System.out.println("✓ Data deletion completed\n");
        } finally {
            table.close();
        }
    }

    private static void deleteTable(TableName tableName) throws IOException {
        System.out.println("[7/10] Deleting Table '" + TABLE_NAME + "'...");

        if (admin.tableExists(tableName)) {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("✓ Table deleted successfully\n");
        }
    }

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
```

Write the follwing code in `./pom.xml` file:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>hbase-test</artifactId>
    <version>1.0-SNAPSHOT</version>

    <name>HBase Test</name>
    <description>Simple HBase connection test</description>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <hbase.version>2.4.17</hbase.version>
        <hadoop.version>3.3.6</hadoop.version>
    </properties>

    <dependencies>
        <!-- HBase Client -->
        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-client</artifactId>
            <version>${hbase.version}</version>
        </dependency>

        <!-- SLF4J for logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.36</version>
        </dependency>

        <!-- Woodstox for XML processing -->
        <dependency>
            <groupId>org.codehaus.woodstox</groupId>
            <artifactId>woodstox-core-asl</artifactId>
            <version>4.4.1</version>
        </dependency>

        <!-- JUnit for testing -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.9.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>

            <!-- Maven Shade Plugin for creating fat jar -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.4.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.example.HBaseTest</mainClass>
                                </transformer>
                            </transformers>
                            <finalName>hbase-test</finalName>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

## Build

```bash
mvn clean package -DskipTests
```

## Run

### Using `mvn`

```bash
mvn exec:java -Dexec.mainClass="com.example.HBaseTest"
```

### Using `JAR`

After building with `mvn clean package`, run:

```bash
java -jar target/hbase-test.jar
```

## Test Steps

1. Initialize HBase connection
2. Clean up old test table
3. Create a test table with column family 'cf'
4. Insert 3 rows (row1, row2, row3) with columns: name, age, city
5. Query and display all data
6. Update row1's age value
7. Query data again
8. Delete all rows
9. Verify deletion (query again)
10. Delete test table
11. Close all resources

## Expected Output

```
HBase Connection Test Started!

[1/10] Initializing HBase Connection...
✓ Connection established successfully!

[2/10] Creating Table 'test_table'...
✓ Table created successfully...

...
✓ All Tests Completed Successfully
```
