package com.xy.request;

public class demo {
	public static void main(String[] args) {
		System.out.println(demo.class.getClassLoader().getResource("krb5.conf").getPath());
		String filePath = System.getProperty("user.dir");
		System.out.println(filePath);
	}
}
