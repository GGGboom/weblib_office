package com.dcampus.weblib.util.userutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;


/**
 * HttpClient工具类，实现基本Get、Post和参数解析
 * 
 * @author zdfeng
 */
public class HttpClientUtil {
	
	// um地址
	//private static String url = "http://localhost:8080/";
    //private static String methods = "project_model/admin/json/group/list";

	// 建立访问
	private static CloseableHttpClient client = HttpClients.createDefault();

	public static String post(String url, String methods,
			Map<String, String[]> param) throws RuntimeException {
		HttpPost post = new HttpPost(url + methods);
		//System.out.println("Http Post : " + post.getURI().toString());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (param != null && param.size() > 0) {
			Iterator<Map.Entry<String, String[]>> it = param.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<String, String[]> entry = it.next();
				for (String s : entry.getValue()) {
					//System.out.println("Http Post params: " + entry.getKey() + " = " + s);
					params.add(new BasicNameValuePair(entry.getKey(), s));
				}
				
			}
		}
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(params, "UTF-8");
			post.setEntity(uefEntity);
			CloseableHttpResponse response = client.execute(post);
			try {
				HttpEntity responseEntiry = response.getEntity();
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (responseEntiry != null) {
						BufferedReader rd = new BufferedReader(
								new InputStreamReader(
										responseEntiry.getContent(), "UTF-8"));
						String line = null;
						while ((line = rd.readLine()) != null) {
							if (!line.isEmpty()) {
								//System.out.println("line : " + line);
								JSONObject json = JSONObject.fromObject(line);
								String data = json.getString("data");
								return data;
							}
						}
					}
				} else {
					throw new RuntimeException("HttpPost request fail!");
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 返回整个json，包括错误信息
	 * @param url
	 * @param methods
	 * @param param
	 * @return
	 * @throws RuntimeException
	 */
	public static String post_v2(String url, String methods,
			Map<String, String[]> param) throws RuntimeException {
		HttpPost post = new HttpPost(url + methods);
		//System.out.println("Http Post : " + post.getURI().toString());
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (param != null && param.size() > 0) {
			Iterator<Map.Entry<String, String[]>> it = param.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<String, String[]> entry = it.next();
				for (String s : entry.getValue()) {
					//System.out.println("Http Post params: " + entry.getKey() + " = " + s);
					params.add(new BasicNameValuePair(entry.getKey(), s));
				}
				
			}
		}
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(params, "UTF-8");
			post.setEntity(uefEntity);
			CloseableHttpResponse response = client.execute(post);
			try {
				HttpEntity responseEntiry = response.getEntity();
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (responseEntiry != null) {
						BufferedReader rd = new BufferedReader(
								new InputStreamReader(
										responseEntiry.getContent(), "UTF-8"));
						String line = null;
						while ((line = rd.readLine()) != null) {
							if (!line.isEmpty()) {
								//System.out.println("line : " + line);
								JSONObject json = JSONObject.fromObject(line);
								//String data = json.getString("data");
								return json.toString();//整个json直接返回
							}
						}
					}
				} else {
					throw new RuntimeException("HttpPost request fail!");
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String get(String url, String methods,
			Map<String, String[]> param) throws RuntimeException {
		StringBuffer getURL = new StringBuffer();
		getURL.append(url).append(methods);
		if (param != null && param.size() > 0) {
			getURL.append("?");
			Iterator<Map.Entry<String, String[]>> it = param.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<String, String[]> entry = it.next();
				// url最后会多一个&，但不影响get方法
				for (String s : entry.getValue()) {
					getURL.append(entry.getKey()).append("=").append(s)
							.append("&");
				}
			}
		}
		//System.out.println("Http Get : " + getURL.toString());
		HttpGet get = new HttpGet(getURL.toString());
		try {
			CloseableHttpResponse response = client.execute(get);
			try {
				HttpEntity responseEntiry = response.getEntity();
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (responseEntiry != null) {
						BufferedReader rd = new BufferedReader(
								new InputStreamReader(
										responseEntiry.getContent(), "UTF-8"));
						String line = null;
						while ((line = rd.readLine()) != null) {
							if (!line.isEmpty()) {
								//System.out.println("line : " + line);
								JSONObject json = JSONObject.fromObject(line);
								String data = json.getString("data");
								return data;
							}
						}
					}
				} else {
					throw new RuntimeException("HttpGet request fail!");
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 返回整个json
	 * @param url
	 * @param methods
	 * @param param
	 * @return
	 * @throws RuntimeException
	 */
	public static String get_v2(String url, String methods,
			Map<String, String[]> param) throws RuntimeException {
		StringBuffer getURL = new StringBuffer();
		getURL.append(url).append(methods);
		if (param != null && param.size() > 0) {
			getURL.append("?");
			Iterator<Map.Entry<String, String[]>> it = param.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<String, String[]> entry = it.next();
				// url最后会多一个&，但不影响get方法
				for (String s : entry.getValue()) {
					getURL.append(entry.getKey()).append("=").append(s)
							.append("&");
				}
			}
		}
		//System.out.println("Http Get : " + getURL.toString());
		HttpGet get = new HttpGet(getURL.toString());
		try {
			CloseableHttpResponse response = client.execute(get);
			try {
				HttpEntity responseEntiry = response.getEntity();
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					if (responseEntiry != null) {
						BufferedReader rd = new BufferedReader(
								new InputStreamReader(
										responseEntiry.getContent(), "UTF-8"));
						String line = null;
						while ((line = rd.readLine()) != null) {
							if (!line.isEmpty()) {
								//System.out.println("line : " + line);
								JSONObject json = JSONObject.fromObject(line);
								//String data = json.getString("data");
								return json.toString();//返回整个json
							}
						}
					}
				} else {
					throw new RuntimeException("HttpGet request fail!");
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public static void main(String[] args) {
		Map<String, String[]> params = new HashMap<String, String[]>();
		String url = "http://localhost:8080/";
		String methods = "project_model/admin/json/group/list";
		params.put("parentId",new String[] {"0d06faa20c0e4e87999acf335c358bee"});
		//System.out.println("========= post ========");
		//System.out.println(post(url, methods, params));
		//System.out.println("========== get ========");
		//System.out.println(get(url, methods, params));
	}
}
