#!/bin/bash

docker-compose up -d
sleep 10

docker exec mongodb-standalone mongosh -u root -p 123456 --authenticationDatabase admin << 'EOF'

use kisechansdb
db.createCollection("students", { capped: false })
show collections
db.students.insertMany([
  { name: "张三", age: 20, department: "计算机系", courses: ["数据库", "数据结构"] },
  { name: "李四", age: 22, department: "软件工程", courses: ["Java", "Python"] },
  { name: "王五", age: 21, department: "网络工程", courses: ["网络原理", "网络安全"] }
])
db.students.find().forEach(printjson)
db.students.find({ age: { $gt: 20 } }).forEach(printjson)
db.students.updateOne(
  { name: "张三" },
  { $set: { age: 19, grade: "优秀" } }
)
db.students.deleteOne({ name: "王五" })
db.students.createIndex({ name: 1 })
db.students.createIndex({ age: 1, department: 1 })
db.students.getIndexes()
db.students.aggregate([
  { $group: { _id: "$department", count: { $sum: 1 }, avgAge: { $avg: "$age" } } }
]).forEach(printjson)

EOF

echo "查看 MongoDB 日志："
docker logs mongodb-standalone --tail 10

echo "实验完成，停止服务"
docker-compose down