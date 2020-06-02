package com.xy.utils;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 获取内部配置文件
 */

public class Configure {

	private static Properties config = null;

	public Configure() {
		config = new Properties();
	}

	public Configure(String filePath) {
		config = new Properties();
		try {
			ClassLoader CL = this.getClass().getClassLoader();
			InputStream in;
			if (CL != null) {
				in = CL.getResourceAsStream(filePath);
			} else {
				in = ClassLoader.getSystemResourceAsStream(filePath);
			}
			config.load(in);
			//    in.close();
		} catch (FileNotFoundException e) {
			//    log.error("服务器配置文件没有找到");
			System.out.println("服务器配置文件没有找到");
		} catch (Exception e) {
			//    log.error("服务器配置信息读取错误");
			System.out.println("服务器配置信息读取错误");
		}
	}

	public String getString(String key) {
		if (config.containsKey(key)) {
			String value = config.getProperty(key);
			return value;
		} else {
			return "";
		}
	}

	public int getInt(String key) {
		String value = getString(key);
		int valueInt = 0;
		try {
			valueInt = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return valueInt;
		}
		return valueInt;
	}

}