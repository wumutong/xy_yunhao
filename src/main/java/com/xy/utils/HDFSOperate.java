package com.xy.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * 针对HDFS文件的操作类
 */
public class HDFSOperate {

	static Logger logger = Logger.getLogger(HDFSOperate.class);
	static Configure props = new Configure("config.properties");
	static String ClusterName = props.getString("cluster.name");
	private static final String HADOOP_URL = "hdfs://" + ClusterName;
	private static  Configuration conf = new Configuration();;
	static String nn1 = props.getString("namenode1");
	static String nn2 = props.getString("namenode2");
//    static String filePath = System.getProperty("user.dir");

	private static final String USER_KEY = "dolphinscheduler@HADOOP.COM";
	private static final String KEY_TAB_PATH = "./dolphinscheduler.keytab";
	static {
		System.setProperty("java.security.krb5.conf", "./krb5.conf");
		conf.set("hadoop.security.authentication", "kerberos");
		conf.set("dfs.namenode.kerberos.principal.pattern", "*/*@HADOOP.COM");
		conf.setBoolean("hadoop.security.authorization", true);
		conf.set("hadoop.security.auth_to_local", "RULE:[1:$1@$0](.*@HADOOP.COM)s/@.*// DEFAULT");

		conf.set("fs.defaultFS", HADOOP_URL);
		conf.set("dfs.nameservices", ClusterName);
		conf.set("dfs.ha.namenodes." + ClusterName, "nn1,nn2");
		conf.set("dfs.namenode.rpc-address." + ClusterName + ".nn1", nn1);
		conf.set("dfs.namenode.rpc-address." + ClusterName + ".nn2", nn2);
		conf.set("dfs.client.failover.proxy.provider." + ClusterName,"org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
		conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());


		try {
			UserGroupInformation.setConfiguration(conf);
			UserGroupInformation.loginUserFromKeytab(USER_KEY, KEY_TAB_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * 新增（创建）HDFS文件
	 *
	 * @param hdfs
	 */
	public static void createHDFS(String hdfs) {
		try {
			FileSystem fs = FileSystem.get(URI.create(hdfs), conf);
			Path path = new Path(hdfs);
			//判断HDFS文件是否存在
			if (fs.exists(path)) {
				//System.out.println(hdfs + "已经存在！！！");
			} else {
				FSDataOutputStream hdfsOutStream = fs.create(new Path(hdfs));
				hdfsOutStream.close();
			}
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 新增（创建）HDFS文件
	 *
	 * @param hdfs
	 */
	public static void createHDFSPath(String hdfs) {
		try {
			FileSystem fs = FileSystem.get(conf);
			Path path = new Path(hdfs);
			//判断HDFS文件是否存在
			if (fs.exists(path)) {
//				System.out.println(hdfs + "已经存在！！！");
				fs.setPermission(path, new FsPermission("777"));
			} else {
				fs.mkdirs(path);
				fs.setPermission(path, new FsPermission("777"));
			}
			fs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 在HDFS文件后面追加内容
	 */
	public void appendHDFS(String hdfspath, String day, String taskID, List<String> list) throws IOException {
		try {
			String hdfsPath = HADOOP_URL + "/" + hdfspath + "/dt=" + day;
			createHDFSPath(hdfsPath);
			String hdfs = HADOOP_URL + "/" + hdfspath + "/dt=" + day +"/"+taskID;
			Path path = new Path(hdfs);
			FileSystem fs = FileSystem.get(URI.create(hdfs), conf);
			//判断HDFS文件是否存在
			if (fs.exists(path)) {
//				System.out.println(hdfs + "已经存在！！！");
				fs.setPermission(path, new FsPermission("777"));
			} else {
				FSDataOutputStream hdfsOutStream = fs.create(path);
				hdfsOutStream.close();
			}
			fs.setPermission(path, new FsPermission("777"));
			FSDataOutputStream hdfsOutStream = fs.append(path);
			for (String value : list) {
				byte[] str = (value + "\n").getBytes("UTF-8");//防止中文乱码
				hdfsOutStream.write(str);
			}
			System.out.println("写出成功，写出数据大小为："+list.size());
			hdfsOutStream.flush();
			hdfsOutStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}