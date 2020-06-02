package com.xy.service;


import com.xy.model.LoginfoData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WriteDataLog {


	/**
	 * 日志数据入库mysql
	 *
	 * @param arrayList
	 * @param pstmt
	 */
	public static void intoMsql(List<LoginfoData> arrayList, PreparedStatement pstmt) {
		//数据入库mysql
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		long l = System.currentTimeMillis();
		try {
			int count = 0;
			for (int i = 0, len = arrayList.size(); i < len; i++) {
				LoginfoData loginfoData = arrayList.get(i);

				pstmt.setString(1, loginfoData.getPubName());
				pstmt.setString(2, loginfoData.getPubId());
				pstmt.setString(3, loginfoData.getType());
				pstmt.setString(4, loginfoData.getStatus());
				pstmt.setString(5, sdf.format(new Date(Long.parseLong(loginfoData.getDates()))));
				pstmt.setString(6, loginfoData.getTaken());
				pstmt.setString(7, loginfoData.getTaskID());
				pstmt.setString(8, loginfoData.getState());
				pstmt.addBatch();
				count++;
				if (count == 1000) {
					pstmt.executeBatch();
					count = 0;
					pstmt.clearBatch();
				}
			}
			if (count > 0) {
				pstmt.executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新数据入库mysql
	 *
	 * @param type
	 * @param pstmt
	 */
	public static void updateMsql(String type,String pubName ,String pubId,PreparedStatement pstmt) {
		//数据入库mysql
		try {
			pstmt.setString(1, type);
			pstmt.setString(2, pubName);
			pstmt.setString(3, pubId);
			pstmt.addBatch();
			pstmt.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
