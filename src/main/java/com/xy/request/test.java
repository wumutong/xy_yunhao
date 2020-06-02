package com.xy.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xy.service.ReadPubTable;

import java.util.List;

public class test {

	public static void main(String[] args) {

		List<String> pubList = new ReadPubTable().getList();


		String requestUrl = "http://xy.api.114best.cn/api.ashx";
		String params1 = "a=2&fun=login&key=00BB363D8F4617C1CFFBABC471B94575";
		String result = HttpRest.sendPostHttpRequest(requestUrl, params1);
		JSONObject jsonObject = JSONObject.parseObject(result);
		String data = jsonObject.get("data").toString();
		String token = JSON.parseObject(data).get("token").toString();
		String state = jsonObject.get("state").toString();
		String msg = jsonObject.get("msg").toString();


		for (int x = 0; x < pubList.size(); x++) {
			String pub = pubList.get(x);

			String params2 = "fun=SetCrawTask&keywords="+pub+"&token=" + token;
			String result2 = HttpRest.sendPostHttpRequest(requestUrl, params2);
			JSONObject jsonObject2 = JSONObject.parseObject(result2);
			String data2 = jsonObject2.get("data").toString();
			String taskID = JSON.parseObject(data2).get("taskID").toString();
			String eta = JSON.parseObject(data2).get("eta").toString();
			String state2 = jsonObject2.get("state").toString();
			String msg2 = jsonObject2.get("msg").toString();
//			System.out.println(result2);

			String params3 = "fun=GetCrawTaskResult&taskID=" + taskID + "&token=" + token;
			String result3 = HttpRest.sendPostHttpRequest(requestUrl, params3);
			JSONObject jsonObject3 = JSONObject.parseObject(result3);
			String data3 = jsonObject3.get("data").toString();
			JSONObject jsonObject4 = JSONObject.parseObject(data3);
			JSONArray results = jsonObject4.getJSONArray("result");
//			System.out.println(result3);
			if (results==null){
				x-=1;
				try {
					Thread.sleep(Long.parseLong(eta)*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
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
					System.out.println(wordss + "_" + gsID + "=====baike::count=" + count + ",,index=" + index + ",,rank=" + rank + ",,word=" + word);
				}
				System.out.println("====================");
				for (int j = 0; j < gongshang.size(); j++) {
					JSONObject gongshangjob = gongshang.getJSONObject(j);
					String count = gongshangjob.getString("count");
					String index = gongshangjob.getString("index");
					String rank = gongshangjob.getString("rank");
					String word = gongshangjob.getString("word");
					System.out.println(wordss + "_" + gsID + "=====gongshang::count=" + count + ",,index=" + index + ",,rank=" + rank + ",,word=" + word);
				}
				System.out.println("====================");
				for (int j = 0; j < merged.size(); j++) {
					JSONObject mergedjob = merged.getJSONObject(j);
					String count = mergedjob.getString("count");
					String index = mergedjob.getString("index");
					String rank = mergedjob.getString("rank");
					String word = mergedjob.getString("word");
					System.out.println(wordss + "_" + gsID + "=====merged::count=" + count + ",,index=" + index + ",,rank=" + rank + ",,word=" + word);
				}
			}
			try {
				Thread.sleep(Long.parseLong(eta)*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
