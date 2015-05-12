/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package examples

import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.{ HBaseConfiguration, HTableDescriptor, TableName }
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.KeyValue.Type
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.CellUtil
import org.apache.spark._
import scala.collection.JavaConverters._
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.Base64
import org.apache.hadoop.hbase.client.HTableInterface
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Get

object HBaseInput {
  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("HBaseTest")
    val sc = new SparkContext(sparkConf)
    val conf = HBaseConfiguration.create()
    // Other options for hbase configuration are available, please check 
    // http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/HConstants.html
    conf.set(HConstants.ZOOKEEPER_QUORUM, args(0))
    // Other options for configuring scan behavior are available. More information available at
    // http://hbase.apache.org/apidocs/org/apache/hadoop/hbase/mapreduce/TableInputFormat.html
    conf.set(TableInputFormat.INPUT_TABLE, args(1))

    // Initialize hBase table if necessary
    val admin = new HBaseAdmin(conf)
    if (!admin.isTableAvailable(args(1))) {
      val tableDesc = new HTableDescriptor(TableName.valueOf(args(1)))
      admin.createTable(tableDesc)
    }

    val scanner = new Scan
    scanner.setReversed(true)
    val start = args(2) + "_" + args(3)
    val stop = args(2) + "_" + args(4)
    scanner.setStartRow(Bytes.toBytes(start))
    scanner.setStopRow(Bytes.toBytes(stop))

      def convertScanToString(scan: Scan): String = {
        val proto: ClientProtos.Scan = ProtobufUtil.toScan(scan);
        return Base64.encodeBytes(proto.toByteArray());
      }

    conf.set(TableInputFormat.SCAN, convertScanToString(scanner))

    val hBaseRDD = sc.newAPIHadoopRDD(conf, classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])

    val keyValue = hBaseRDD.map(x => x._2).map(x => x.getColumn(Bytes.toBytes("identity"), Bytes.toBytes("id")))

    val outPut = keyValue.flatMap { x =>
      x.asScala.map { cell =>
        if (Bytes.toString(CellUtil.cloneFamily(cell)) == "identity") {
          if (Bytes.toString(CellUtil.cloneQualifier(cell)) == "id") {
            CellUtil.cloneValue(cell)
          }
        }
      }
    }

    outPut.foreach(println)

    sc.stop()
  }
}
