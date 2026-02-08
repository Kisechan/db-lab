# HBase Test

A simple Maven project to test HBase connection and basic operations.

## Prerequisites

- Java 11+
- Maven 3.6+
- HBase running at `hadoop-hbase:2181`

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
