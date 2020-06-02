package com.xy.service;

import com.xy.model.LoginfoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取log数据表
 */
public class ReadSetLogTable {
	private static Logger logger = LoggerFactory.getLogger(ReadSetLogTable.class);

	private Map<String, String> map;

	public Map<String, String> getMap() {
		if (map != null) {
			map.clear();
		} else {
			map = new HashMap<>();
		}
		String sql = "SELECT customer_id,type,status FROM keywords_api_log";

		PreparedStatement pstmt = GetPstmt.getPstmt(sql);
		ResultSet query = null;
		try {
			query = pstmt.executeQuery();
			while (query.next()) {
				String customer_id = query.getString("customer_id");
				String type = query.getString("type");
				String status = query.getString("status");
				if (customer_id != null && !customer_id.equals("") && type.equals("set") && status.equals("成功")) {
					map.put(customer_id, status);
				}
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
		logger.info("共加载set成功的{}条记录", map.size());
		if (map == null) {
			throw new IllegalStateException("加载log日志信息尚未加载");
		}
		return map;
	}
}
