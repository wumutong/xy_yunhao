package com.xy.run;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xy.model.LoginfoData;
import com.xy.request.HttpRest;
import com.xy.service.*;
import com.xy.utils.Configure;
import com.xy.utils.HDFSOperate;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.security.UserGroupInformation;

public class RunGetPost {


	public static void main(String[] args) {
		Configure props = new Configure("config.properties");
		String hdfsPath = props.getString("hdfs.path");

		Map<String, String> map = new ReadConfTable().getMap();
		String requestUrl = map.get("url");
		String user = map.get("user");
		String password = map.get("password");
		List<String> pubList = new ReadPubTable().getList();
		Map<String, String> logMap = new ReadSetLogTable().getMap();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		long timeMillis = System.currentTimeMillis();
		String dates = sdf.format(new Date(timeMillis));
		//存储
		List<LoginfoData> listSet = new ArrayList<>();
		List<LoginfoData> listGet = new ArrayList<>();
		HDFSOperate hdfsOperate = new HDFSOperate();

		String sql = "insert into keywords_api_log (customer,customer_id,type,status,dates,taken,task_id,state) values(?,?,?,?,?,?,?,?)";
		String sqlType = "UPDATE customer_info SET remarks=? WHERE customer=? and customer_id=?";
		//日志输出mysql
		PreparedStatement statement = GetPstmt.getPstmt(sql);
		PreparedStatement statementUpdate = GetPstmt.getPstmt(sqlType);
		String params1 = "a=" + user + "&fun=login&key=" + password;
		try {
			String token = null;
			for (int i = 0; i < 5; i++) {
				String result = HttpRest.sendPostHttpRequest(requestUrl, params1);
				JSONObject jsonObject = JSONObject.parseObject(result);
				String data = jsonObject.get("data").toString();
				token = JSON.parseObject(data).get("token").toString();
				String state = jsonObject.get("state").toString();
				String msg = jsonObject.get("msg").toString();
				//失败策略需要写入mysql，来进行最终结果的判断，获取成功在数据库中更新状态值和时间戳，失败进行重试
				if (state.equals("1")) {
					long l = System.currentTimeMillis();
					listSet.add(new LoginfoData("", "", "login", "成功", String.valueOf(l), token, "", state));
					break;
				}
				if (i == 4) {
					//写出请求失败到mysql
					long l = System.currentTimeMillis();
					listSet.add(new LoginfoData("", "", "login", "失败", String.valueOf(l), "", "", state));
				}
			}

			if (token != null) {
				int countSet = 1;
				for (int i = 0; i < pubList.size(); i++) {
					String[] splitPub = pubList.get(i).split("\\|", -1);
					String pub = splitPub[0];
					String type = splitPub[1];
					String[] s = pub.split("_");
					String pubname = s[0];
					String pubid = s[1];
					//包含则为成功set的数据
					if (logMap.containsKey(pubid)) {
						continue;
					}

					System.out.println("当前set的企业为：：：" + pub);
					if (i % 5 == 0) {
						System.out.println("写myql中。。。。。。。。。");
						WriteDataLog.intoMsql(listSet, statement);
						listSet.clear();
					}
					String params2 = "fun=SetCrawTask&keywords=" + pub + "&token=" + token;
					String result2 = HttpRest.sendPostHttpRequest(requestUrl, params2);
					JSONObject jsonObject2 = JSONObject.parseObject(result2);
					if (jsonObject2.get("data") == null) {
						//写出请求失败到mysql
						long l = System.currentTimeMillis();
						listSet.add(new LoginfoData(pubname, pubid, "set", "失败", String.valueOf(l), token, "", ""));
						countSet = 1;
						continue;
					}
					String data2 = jsonObject2.get("data").toString();

					String taskID = JSON.parseObject(data2).get("taskID").toString();
					String eta = JSON.parseObject(data2).get("eta").toString();
					String state2 = jsonObject2.get("state").toString();
					String msg2 = jsonObject2.get("msg").toString();

					//set企业后判断等待时间，等待时间到达后去get数据，失败次数设置为5次，都失败写入数据库
					if (countSet == 2) {
						//写出请求失败到mysql
						long l = System.currentTimeMillis();
						listSet.add(new LoginfoData(pubname, pubid, "set", "失败", String.valueOf(l), token, "", state2));
						countSet = 1;
						continue;
					}
					//if (!state2.equals("1")) {
					if (msg2.equals("请求成功")) {
						//写出请求成功到mysql
						long l = System.currentTimeMillis();
						listSet.add(new LoginfoData(pubname, pubid, "set", "成功", String.valueOf(l), token, taskID, state2));
						countSet = 1;
						System.out.println("获取result2数据为:" + result2);
					} else {
						Thread.sleep(Integer.parseInt(eta) * 1000);
						countSet++;
						i--;
						System.out.println("set失败，等待后再次请求:" + result2);
						continue;
					}
					///////////
				}
			}

			if (token != null) {

				int x = 0;
				for (int z = 0; z < pubList.size() - x; z++) {

					int countSet = 1;
					int countGet = 1;
					String taskID = null;
					for (int i = 0; i < pubList.size(); i++) {
						String[] splitPub = pubList.get(i).split("\\|", -1);
						String pub = splitPub[0];
						String type = splitPub[1];
						String[] s = pub.split("_");
						String pubname = s[0];
						String pubid = s[1];
						//1为成功，0为失败
						if (type.equals("1")) {
							continue;
						}

						System.out.println("当前请求的企业为：：：" + pub);
						if (i % 5 == 0) {
							System.out.println("写myql中。。。。。。。。。");
							WriteDataLog.intoMsql(listGet, statement);
							listGet.clear();
						}
						if (countGet == 1) {
							String params2 = "fun=SetCrawTask&keywords=" + pub + "&token=" + token;
							String result2 = HttpRest.sendPostHttpRequest(requestUrl, params2);
							JSONObject jsonObject2 = JSONObject.parseObject(result2);
							if (jsonObject2.get("data") == null) {
								//写出请求失败到mysql
								long l = System.currentTimeMillis();
//								listGet.add(new LoginfoData(pubname, pubid, "set", "失败", String.valueOf(l), token, "", ""));
								countSet = 1;
								continue;
							}
							String data2 = jsonObject2.get("data").toString();
							taskID = JSON.parseObject(data2).get("taskID").toString();
							String eta = JSON.parseObject(data2).get("eta").toString();
							String state2 = jsonObject2.get("state").toString();
							String msg2 = jsonObject2.get("msg").toString();
							System.out.println("开始获取获取数据为:" + result2 + "、、、、taskID：" + taskID);

							//set企业后判断等待时间，等待时间到达后去get数据，失败次数设置为5次，都失败写入数据库
							if (countSet == 2) {
								//写出请求失败到mysql
								long l = System.currentTimeMillis();
//								listGet.add(new LoginfoData(pubname, pubid, "set", "失败", String.valueOf(l), token, "", state2));
								countSet = 1;
								continue;
							}
							//if (!state2.equals("1")) {
							if (msg2.equals("请求成功")) {
								//写出请求成功到mysql
								long l = System.currentTimeMillis();
//								listGet.add(new LoginfoData(pubname, pubid, "set", "成功", String.valueOf(l), token, taskID, state2));
								countSet = 1;
								System.out.println("获取result2数据为:" + result2);
							} else {
								Thread.sleep(Integer.parseInt(eta) * 1000);
								countSet++;
								i--;
								System.out.println("请求失败，等待后再次请求:" + result2);
								continue;
							}
							///////////
						}
						String params3 = "fun=GetCrawTaskResult&taskID=" + taskID + "&token=" + token;
						String result3 = HttpRest.sendPostHttpRequest(requestUrl, params3);
						JSONObject jsonObject3 = JSONObject.parseObject(result3);
						String data3 = jsonObject3.getString("data");
						String state3 = jsonObject3.getString("state");
						String msg3 = jsonObject3.getString("msg");

						System.out.println("请求的taskID为：：：：" + taskID);
						if (countGet == 2) {
							//写出请求失败到mysql
							long l = System.currentTimeMillis();

							listGet.add(new LoginfoData(pubname, pubid, "get", "失败", String.valueOf(l), token, "", state3));
							countGet = 1;
							System.out.println("请求失败：" + pubname);
							continue;
						}
//					if (!state3.equals("1")) {
						if (msg3.equals("任务已完成")) {
							//写出请求成功到mysql
							long l = System.currentTimeMillis();
							listGet.add(new LoginfoData(pubname, pubid, "get", "成功", String.valueOf(l), token, taskID, state3));
							countGet = 1;
							System.out.println("写出数据：" + pubname);
							//写出状态值到企业数据表中，更新状态值为1
							WriteDataLog.updateMsql("1", pubname, pubid, statementUpdate);
							x++;
						} else {
							System.out.println("没有请求到数据，歇一歇，次数：" + countGet);
							Thread.sleep(5 * 1000);
							countGet++;
							i--;
							continue;
						}


						JSONObject jsonObject4 = JSONObject.parseObject(data3);
						JSONArray results = jsonObject4.getJSONArray("result");

						if (results == null) {
							throw new RuntimeException("获取result3数据为:" + result3);
						}

						System.out.println("输出。。。。。。。。。。。。。。");
						toHdfs2(pubname, pubid, hdfsPath, results, hdfsOperate, dates, String.valueOf(timeMillis));
					}


					//更新mysql数据
					Thread.sleep(300 * 1000);
					System.out.println("更新mysql数据customer_info");
					pubList = new ReadPubTable().getList();
				}

			}

			WriteDataLog.intoMsql(listSet, statement);
			WriteDataLog.intoMsql(listGet, statement);
			listSet.clear();
			listGet.clear();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param merged
	 * @param wordss
	 * @param gsID
	 * @param hdfsOperate
	 * @param dates
	 * @param filename
	 */
	private static void toHdfs(String hdfsPath, JSONArray merged, String type, String wordss, String gsID, HDFSOperate hdfsOperate, String dates, String filename) {
		List<String> list = new ArrayList<>();
		for (int j = 0; j < merged.size(); j++) {
			JSONObject mergedjob = merged.getJSONObject(j);
			String count = mergedjob.getString("count");
			String index = mergedjob.getString("index");
			String rank = mergedjob.getString("rank");
			String word = mergedjob.getString("word");
			String values = dates + "," + type + "," + wordss + "," + gsID + "," + count + "," + index + "," + rank + "," + word;
//			System.out.println(wordss + "_" + gsID + "=====merged::count=" + count + ",,index=" + index + ",,rank=" + rank + ",,word=" + word);
			list.add(values);
		}
		try {
			hdfsOperate.appendHDFS(hdfsPath, dates, filename, list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param hdfsPath
	 * @param results
	 * @param hdfsOperate
	 * @param dates
	 * @param filename
	 */
	private static void toHdfs2(String pubname, String pubid, String hdfsPath, JSONArray results, HDFSOperate hdfsOperate, String dates, String filename) {
		List<String> list = new ArrayList<>();
		//这边需要写循环去数组

		for (int x = 0; x < results.size(); x++) {
			JSONObject resultsjob = results.getJSONObject(x);
			String wordss = resultsjob.getString("word");
			String gsID = resultsjob.getString("gsID");
			String wordRank = resultsjob.getString("wordRank");
			JSONObject wordjob = JSONObject.parseObject(wordRank);
			JSONArray baike = wordjob.getJSONArray("baike");
			JSONArray gongshang = wordjob.getJSONArray("gongshang");
			JSONArray merged = wordjob.getJSONArray("merged");

			for (int j = 0; j < merged.size(); j++) {
				JSONObject mergedjob = merged.getJSONObject(j);
				String count = mergedjob.getString("count");
				String index = mergedjob.getString("index");
				String rank = mergedjob.getString("rank");
				String word = mergedjob.getString("word");
				String values = dates + ",merged," + wordss + "," + gsID + "," + count + "," + index + "," + rank + "," + word;
				list.add(values);
			}
			for (int j = 0; j < gongshang.size(); j++) {
				JSONObject gongshangjob = gongshang.getJSONObject(j);
				String count = gongshangjob.getString("count");
				String index = gongshangjob.getString("index");
				String rank = gongshangjob.getString("rank");
				String word = gongshangjob.getString("word");
				String values = dates + ",gongshang," + wordss + "," + gsID + "," + count + "," + index + "," + rank + "," + word;
				list.add(values);
			}
			for (int j = 0; j < baike.size(); j++) {
				JSONObject baikejob = baike.getJSONObject(j);
				String count = baikejob.getString("count");
				String index = baikejob.getString("index");
				String rank = baikejob.getString("rank");
				String word = baikejob.getString("word");
				String values = dates + ",baike," + wordss + "," + gsID + "," + count + "," + index + "," + rank + "," + word;
				list.add(values);
			}
		}

		try {
			if (list.size() == 0) {
				String values = dates + ",," + pubname + "," + pubid + ",,,,";
				list.add(values);
			}
			hdfsOperate.appendHDFS(hdfsPath, dates, filename, list);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
