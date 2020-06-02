package com.xy.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 获取http连接及请求数据
 */
public class HttpRest {

	/**
	 * 获取http连接及请求数据
	 *
	 * @param url
	 * @param param
	 * @return
	 */
	public static String sendPostHttpRequest(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "x-www-form-urlencoded");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			//1.获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 3发送请求参数
			out.print(param);
			// 4.flush输出流的缓冲
			out.flush();
			// 5.定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

			int BUFFER_SIZE = 20000;
			char[] buffer = new char[BUFFER_SIZE];
			int charsRead;
			while ((charsRead = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
				sb.append(buffer, 0, charsRead);
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
//			e.printStackTrace();
		} finally {
			//使用finally块来关闭输出流、输入流
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String requestUrl = "http://xy.api.114best.cn/api.ashx";
		String user = "2";
		String password = "00BB363D8F4617C1CFFBABC471B94575";

		String params1 = "a=" + user + "&fun=login&key=" + password;
		try {
			String result = HttpRest.sendPostHttpRequest(requestUrl, params1);
			JSONObject jsonObject = JSONObject.parseObject(result);
			String data = jsonObject.get("data").toString();
			String token = JSON.parseObject(data).get("token").toString();
			String state = jsonObject.get("state").toString();
			String msg = jsonObject.get("msg").toString();
			//todo 失败策略需要写入mysql，来进行最终结果的判断，获取成功在数据库中更新状态值和时间戳，失败进行重试

//				String params2 = "fun=SetCrawTask&keywords=" + pub + "&token=" + token;
//				String result2 = HttpRest.sendPostHttpRequest(requestUrl, params2);
//				JSONObject jsonObject2 = JSONObject.parseObject(result2);
//				String data2 = jsonObject2.get("data").toString();
//				String taskID = JSON.parseObject(data2).get("taskID").toString();
//				String eta = JSON.parseObject(data2).get("eta").toString();
//				String state2 = jsonObject2.get("state").toString();
//				String msg2 = jsonObject2.get("msg").toString();
				//todo set企业后判断等待时间，等待时间到达后去get数据，失败次数设置为5次，都失败写入数据库

				String params3 = "fun=GetCrawTaskResult&taskID=14&token=" + token;
				String result3 = HttpRest.sendPostHttpRequest(requestUrl, params3);
				JSONObject jsonObject3 = JSONObject.parseObject(result3);
				String data3 = jsonObject3.get("data").toString();
				JSONObject jsonObject4 = JSONObject.parseObject(data3);
				JSONArray results = jsonObject4.getJSONArray("result");
				//这边需要写循环去数组
				for (int i = 0; i < results.size(); i++) {
					System.out.println("====================");
					JSONObject resultsjob = results.getJSONObject(i);
					String wordss = resultsjob.getString("word");
					String gsID = resultsjob.getString("gsID");
					String wordRank = resultsjob.getString("wordRank");
					JSONObject wordjob = JSONObject.parseObject(wordRank);
					JSONArray baike = wordjob.getJSONArray("baike");
					JSONArray gongshang = wordjob.getJSONArray("gongshang");
					JSONArray merged = wordjob.getJSONArray("merged");
					for (int j = 0; j < baike.size(); j++) {
						JSONObject bikejob = baike.getJSONObject(j);
						String count = bikejob.getString("count");
						String index = bikejob.getString("index");
						String rank = bikejob.getString("rank");
						String word = bikejob.getString("word");
						System.out.println(wordss+"_"+gsID+"=====baike::count=" + count + ",,index=" + index + ",,rank=" + rank + ",,word=" + word);
					}
					System.out.println("====================");
					for (int j = 0; j < gongshang.size(); j++) {
						JSONObject gongshangjob = gongshang.getJSONObject(j);
						String count = gongshangjob.getString("count");
						String index = gongshangjob.getString("index");
						String rank = gongshangjob.getString("rank");
						String word = gongshangjob.getString("word");
						System.out.println(wordss+"_"+gsID+"=====gongshang::count=" + count + ",,index=" + index + ",,rank=" + rank + ",,word=" + word);
					}
					System.out.println("====================");
					for (int j = 0; j < merged.size(); j++) {
						JSONObject mergedjob = merged.getJSONObject(j);
						String count = mergedjob.getString("count");
						String index = mergedjob.getString("index");
						String rank = mergedjob.getString("rank");
						String word = mergedjob.getString("word");
						System.out.println(wordss+"_"+gsID+"=====merged::count=" + count + ",,index=" + index + ",,rank=" + rank + ",,word=" + word);
					}
				}


				String state3 = jsonObject3.get("state").toString();
				String msg3 = jsonObject3.get("msg").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
