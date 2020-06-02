package com.xy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取参数配置表数据表
 */
public class ReadConfTable {
	private static Logger logger = LoggerFactory.getLogger(ReadConfTable.class);

	private Map<String, String> map;

	public Map<String, String> getMap() {
		if (map != null) {
			map.clear();
		} else {
			map = new HashMap<>();
		}
		String sql = "SELECT name,val FROM parameter";

		PreparedStatement pstmt = GetPstmt.getPstmt(sql);
		ResultSet query = null;
		try {
			query = pstmt.executeQuery();
			while (query.next()) {
				String name = query.getString("name");
				String val = query.getString("val");
				map.put(name, val);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (query != null) {
					query.close();
				}
				if (pstmt != null) {
					GetPstmt.closeConn();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		logger.info("共加载配置文件{}条记录", map.size());
		if (map == null) {
			throw new IllegalStateException("信息尚未加载");
		}
		return map;
	}
}
