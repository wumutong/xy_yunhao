package com.xy.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.GroupFactory;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetReader.Builder;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Types;

public class ReadWriteParquet {
	static Logger logger = Logger.getLogger(ReadWriteParquet.class);

	static String ClusterName = "mfcluster";
	private static final String HADOOP_URL = "hdfs://" + ClusterName;
	public static Configuration conf;
	static String nn1 = "10.0.5.21:8020";
	static String nn2 = "10.0.5.22:8020";

	static {
		conf = new Configuration();
		conf.set("fs.defaultFS", HADOOP_URL);
		conf.set("dfs.nameservices", ClusterName);
		conf.set("dfs.ha.namenodes." + ClusterName, "nn1,nn2");
		conf.set("dfs.namenode.rpc-address." + ClusterName + ".nn1", nn1);
		conf.set("dfs.namenode.rpc-address." + ClusterName + ".nn2", nn2);
		conf.set("dfs.client.failover.proxy.provider." + ClusterName, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
		conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
	}


	public static void main(String[] args) throws Exception {
//		parquetReaderV2("test\\parquet-out2");
//		MessageType messageTypeFromCode = getMessageTypeFromCode();


		for (int i = 0; i < 5; i++) {
			System.out.println(i);
			if (i == 3) {
				i--;
				Thread.sleep(5000);
				continue;
			}

		}


	}

	/**
	 * 读取parquet文件
	 *
	 * @param inPath
	 * @throws Exception
	 */
	public static void parquetReaderV2(String inPath) throws Exception {
		GroupReadSupport readSupport = new GroupReadSupport();
		Builder<Group> reader = ParquetReader.builder(readSupport, new Path(inPath));
		ParquetReader<Group> build = reader.build();
		Group line = null;
		while ((line = build.read()) != null) {
			Group time = line.getGroup("time", 0);
			//通过下标和字段名称都可以获取
			System.out.println(line.getString("city", 0) + "\t" +
					line.getString("ip", 0) + "\t" +
					time.getInteger("ttl", 0) + "\t" +
					time.getString("ttl2", 0) + "\t");
		}
		logger.info("读取结束");
	}


	public static MessageType getMessageTypeFromCode() {
		MessageType schema = Types.buildMessage()
				.required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("city")
				.required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("ip")
				.repeatedGroup().required(PrimitiveType.PrimitiveTypeName.INT32).named("ttl")
				.required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("ttl2")
				.named("time")
				.named("Pair");
		System.out.println(schema.toString());
		return schema;
	}


	/**
	 * @param outPath 　　输出Parquet格式
	 * @param inPath  输入普通文本文件
	 * @throws IOException
	 */
	public static void parquetWriter(String outPath, String inPath) throws IOException {
		MessageType schema = Types.buildMessage()
				.required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("city")
				.required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("ip")
				.repeatedGroup().required(PrimitiveType.PrimitiveTypeName.INT32).named("ttl")
				.required(PrimitiveType.PrimitiveTypeName.BINARY).as(OriginalType.UTF8).named("ttl2")
				.named("time")
				.named("Pair");
		GroupFactory factory = new SimpleGroupFactory(schema);
		Path path = new Path(outPath);
		GroupWriteSupport writeSupport = new GroupWriteSupport();
		writeSupport.setSchema(schema, conf);
		ParquetWriter<Group> writer = new ParquetWriter<>(path, conf, writeSupport);
		//把本地文件读取进去，用来生成parquet格式文件
		BufferedReader br = new BufferedReader(new FileReader(new File(inPath)));
		String line = "";
		Random r = new Random();
		while ((line = br.readLine()) != null) {
			String[] strs = line.split("\\s+");
			if (strs.length == 2) {
				Group group = factory.newGroup()
						.append("city", strs[0])
						.append("ip", strs[1]);
				Group tmpG = group.addGroup("time");
				tmpG.append("ttl", r.nextInt(9) + 1);
				tmpG.append("ttl2", r.nextInt(9) + "_a");
				writer.write(group);
			}
		}
		logger.info("write end");
		writer.close();
	}


}