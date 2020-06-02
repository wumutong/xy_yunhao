package com.xy.service;


import com.xy.utils.Configure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class GetPstmt {
	private static PreparedStatement pstmt;
	private static Connection conn;
	public static PreparedStatement getPstmt(String sql) {
		Configure props = new Configure("config.properties");
		String url = props.getString("jdbc.monitor.url");
		String username = props.getString("jdbc.monitor.username");
		String password = props.getString("jdbc.monitor.password");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, username, password);
			pstmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return pstmt;
	}

	public static void closeConn(){
		try {
			if (pstmt != null) {
				pstmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
