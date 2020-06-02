package com.xy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取企业信息数据表
 */
public class ReadPubTable {
	private static Logger logger = LoggerFactory.getLogger(ReadPubTable.class);

	private List<String> list ;

	public List<String> getList() {
		if (list != null) {
			list.clear();
		} else {
			list = new ArrayList<>();
		}
		String sql = "SELECT customer,customer_id,remarks FROM customer_info";

		PreparedStatement pstmt = GetPstmt.getPstmt(sql);
		ResultSet query = null;
		try {
			query = pstmt.executeQuery();
			while (query.next()) {
				String customer = query.getString("customer");
				String customer_id = query.getString("customer_id");
				String remarks = query.getString("remarks");
				if (remarks==null||remarks.equals("")){
					remarks="0";
				}
				list.add(customer+"_"+customer_id+"|"+remarks);
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
		logger.info("共加载企业客户{}条记录", list.size());
		if (list == null) {
			throw new IllegalStateException("信息尚未加载");
		}
		return list;
	}
}
