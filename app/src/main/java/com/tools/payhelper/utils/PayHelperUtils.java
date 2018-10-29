package com.tools.payhelper.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.MainActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import de.robv.android.xposed.XposedHelpers;

/**
 * 

* @ClassName: PayHelperUtils

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:27:32

*
 */
public class PayHelperUtils {

	public static String WECHATSTART_ACTION = "com.payhelper.wechat.start";
	public static String ALIPAYSTART_ACTION = "com.payhelper.alipay.start";
	public static String QQSTART_ACTION = "com.payhelper.qq.start";
	public static String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
	public static String TRADENORECEIVED_ACTION = "com.tools.payhelper.tradenoreceived";
	public static String LOGINIDRECEIVED_ACTION = "com.tools.payhelper.loginidreceived";
	public static String UPDATEBALANCE_ACTION = "com.tools.payhelper.updatebalance";
	public static String GETTRADEINFO_ACTION = "com.tools.payhelper.gettradeinfo";
	public static List<QrCodeBean> qrCodeBeans = new ArrayList<QrCodeBean>();
	public static List<OrderBean> orderBeans = new ArrayList<OrderBean>();
	public static boolean isFirst=true;

	/*
	 * 启动一个app
	 */
	public static void startAPP() {
		try {
			Intent intent = new Intent(CustomApplcation.getInstance().getApplicationContext(), MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			CustomApplcation.getInstance().getApplicationContext().startActivity(intent);
		} catch (Exception e) {
		}
	}

	/**
	 * 将图片转换成Base64编码的字符串
	 * 
	 * @param path
	 * @return base64编码的字符串
	 */
	public static String imageToBase64(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		InputStream is = null;
		byte[] data = null;
		String result = null;
		try {
			is = new FileInputStream(path);
			// 创建一个字符流大小的数组。
			data = new byte[is.available()];
			// 写入数组
			is.read(data);
			// 用默认的编码格式进行编码
			result = Base64.encodeToString(data, Base64.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		result = "\"data:image/gif;base64," + result + "\"";
		return result;
	}

	public static void sendAppMsg(String money, String mark, String type, Context context) {
		Intent broadCastIntent = new Intent();
		if (type.equals("alipay")) {
			broadCastIntent.setAction(ALIPAYSTART_ACTION);
		} else if (type.equals("wechat")) {
			broadCastIntent.setAction(WECHATSTART_ACTION);
		} else if (type.equals("qq")) {
			broadCastIntent.setAction(QQSTART_ACTION);
		}
		broadCastIntent.putExtra("mark", mark);
		broadCastIntent.putExtra("money", money);
		context.sendBroadcast(broadCastIntent);
	}

	/*
	 * 将时间戳转换为时间
	 */
	public static String stampToDate(String s) {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long lt = new Long(s);
		Date date = new Date(lt * 1000);
		res = simpleDateFormat.format(date);
		return res;
	}

	/**
	 * 方法描述：判断某一应用是否正在运行
	 * 
	 * @param context
	 *            上下文
	 * @param packageName
	 *            应用的包名
	 * @return true 表示正在运行，false表示没有运行
	 */
	public static boolean isAppRunning(Context context, String packageName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
		if (list.size() <= 0) {
			return false;
		}
		for (ActivityManager.RunningTaskInfo info : list) {
			if (info.baseActivity.getPackageName().equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 启动一个app
	 */
	public static void startAPP(Context context, String appPackageName) {
		try {
			Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
			context.startActivity(intent);
		} catch (Exception e) {
			sendmsg(context, "startAPP异常" + e.getMessage());
		}
	}

	public static void notify(final Context context, String type, final String no, String money, String mark,
			String dt) {
		String notifyurl = AbSharedUtil.getString(context, "notifyurl");
		String signkey = AbSharedUtil.getString(context, "signkey");
		sendmsg(context, "订单" + no + "重试发送异步通知...");
		if (TextUtils.isEmpty(notifyurl) || TextUtils.isEmpty(signkey)) {
			sendmsg(context, "发送异步通知异常，异步通知地址为空");
			update(no, "异步通知地址为空");
			return;
		}
		
		String account="";
		String balance=AbSharedUtil.getString(context, type+"balance");
		if(type.equals("alipay")){
			account=AbSharedUtil.getString(context, "alipay");
		}else if(type.equals("wechat")){
			account=AbSharedUtil.getString(context, "wechat");
		}else if(type.equals("qq")){
			account=AbSharedUtil.getString(context, "qq");
		}
		
		HttpUtils httpUtils = new HttpUtils(15000);

		String sign = MD5.md5(dt + mark + money + no + type + signkey);
		RequestParams params = new RequestParams();
		params.addBodyParameter("type", type);
		params.addBodyParameter("no", no);
		params.addBodyParameter("money", money);
		params.addBodyParameter("mark", mark);
		params.addBodyParameter("dt", dt);
		params.addBodyParameter("balance", balance);
		if (!TextUtils.isEmpty(account)) {
			params.addBodyParameter("account", account);
		}
		params.addBodyParameter("sign", sign);
		httpUtils.send(HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				sendmsg(context, "发送异步通知异常，服务器异常" + arg1);
				update(no, arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result = arg0.result;
				if (result.contains("success")) {
					sendmsg(context, "发送异步通知成功，服务器返回" + result);
				} else {
					sendmsg(context, "发送异步通知失败，服务器返回" + result);
				}
				update(no, result);
			}
		});
	}

	private static void update(String no, String result) {
		DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
		dbManager.updateOrder(no, result);
	}

	public static String getCookieStr(ClassLoader appClassLoader) {
		String cookieStr = "";
		// 获得cookieStr
		XposedHelpers.callStaticMethod(XposedHelpers.findClass(
				"com.alipay.mobile.common.transportext.biz.appevent.AmnetUserInfo", appClassLoader), "getSessionid");
		Context context = (Context) XposedHelpers.callStaticMethod(XposedHelpers.findClass(
				"com.alipay.mobile.common.transportext.biz.shared.ExtTransportEnv", appClassLoader), "getAppContext");
		if (context != null) {
			Object readSettingServerUrl = XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("com.alipay.mobile.common.helper.ReadSettingServerUrl", appClassLoader),
					"getInstance");
			if (readSettingServerUrl != null) {
				// String gWFURL = (String)
				// XposedHelpers.callMethod(readSettingServerUrl, "getGWFURL",
				// context);
				String gWFURL = ".alipay.com";
				cookieStr = (String) XposedHelpers.callStaticMethod(XposedHelpers
						.findClass("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", appClassLoader),
						"getCookie", gWFURL);
			} else {
				sendmsg(context, "异常readSettingServerUrl为空");
			}
		} else {
			sendmsg(context, "异常context为空");
		}
		return cookieStr;
	}

	public static void sendTradeInfo(Context context) {
		Intent broadCastIntent = new Intent();
		broadCastIntent.setAction(GETTRADEINFO_ACTION);
		context.sendBroadcast(broadCastIntent);
	}
	
	public static String getAPI(){
		String txt="";
		try {  
			File file = new File(Environment.getExternalStorageDirectory(),"abc.txt");  
		    BufferedReader br = new BufferedReader(new FileReader(file));  
		    String readline = "";  
		    StringBuffer sb = new StringBuffer();  
		    while ((readline = br.readLine()) != null) {  
		        System.out.println("readline:" + readline);  
		        sb.append(readline);  
		    }  
		    br.close();  
		    txt=sb.toString();
		} catch (Exception e) {  
		    e.printStackTrace();  
		}
		return txt;
	}
	public static void setAPI(String API){
		try {  
			LogToFile.i("payhelper", "切换接口："+API);
			File file = new File(Environment.getExternalStorageDirectory(),"abc.txt");
			if (!file.exists()) {  
				file.createNewFile();;  
			}
		    BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));  
		    bw.write(API);  
		    bw.flush(); 
		    bw.close();
		} catch (Exception e) {  
		    e.printStackTrace();  
		}  
	}
	public static String getAlipayUserId(ClassLoader classLoader) {
		String userId="";
		try {
			Class<?> AlipayApplication = XposedHelpers.findClass("com.alipay.mobile.framework.AlipayApplication",
					classLoader);
			Class<?> SocialSdkContactService = XposedHelpers
					.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService", classLoader);
			Object instace = XposedHelpers.callStaticMethod(AlipayApplication, "getInstance");
			Object MicroApplicationContext = XposedHelpers.callMethod(instace, "getMicroApplicationContext");
			Object service = XposedHelpers.callMethod(MicroApplicationContext, "findServiceByInterface",
					SocialSdkContactService.getName());
			Object MyAccountInfoModel = XposedHelpers.callMethod(service, "getMyAccountInfoModelByLocal");
			userId = XposedHelpers.getObjectField(MyAccountInfoModel, "userId").toString();
		} catch (Exception e) {
		}
		return userId;
	}
	
	public static void getBill(final Context context,final String cookie,String alipayUserId){
		String api=getAPI();
		LogToFile.i("payhelper", "getBill获取订单，当前使用API"+api);
		if(api.equals("APP")){
			getTradeInfoFromAPP(context, cookie);
		}else if(api.equals("PC")){
			getTradeInfoFromPC(context, cookie, alipayUserId);
		}
	}
	
	public static void getTradeInfoFromAPP(final Context context,final String cookie) {
		String url="https://mbillexprod.alipay.com/enterprise/walletTradeList.json?lastTradeNo=&lastDate=&pageSize=1&shopId=&_inputcharset=gbk&ctoken&source=&_ksTS="+System.currentTimeMillis()+"_49&_callback=&_input_charset=utf-8";
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configResponseTextCharset("GBK");
		RequestParams params = new RequestParams();
		params.addHeader("Cookie", cookie);
		params.addHeader("Referer", "https://render.alipay.com/p/z/merchant-mgnt/simple-order.html");
		params.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 7.1.1; zh-CN; 1605-A01 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/11.8.8.968 UWS/2.13.1.39 Mobile Safari/537.36 UCBS/2.13.1.39_180615144818 NebulaSDK/1.8.100112 Nebula AlipayDefined(nt:WIFI,ws:360|0|3.0) AliApp(AP/10.1.22.835) AlipayClient/10.1.22.835 Language/zh-Hans useStatusBar/true isConcaveScreen/false");
		params.addHeader("X-Alipay-Client-Session", "check");
		httpUtils.send(HttpMethod.GET, url, params, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				sendmsg(context, "请求支付宝API失败："+arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result = arg0.result.replace("/**/(", "").replace("})", "}");
				try {
					JSONObject jsonObject = new JSONObject(result);
					LogToFile.i("payhelper", "getTradeInfoFromAPP获取数据完整，返回数据"+result);
					if(jsonObject.has("status")){
						String status=jsonObject.getString("status");
						if(status.equals("succeed")){
							JSONObject res = jsonObject.getJSONObject("result");
							JSONArray jsonArray = res.getJSONArray("list");
							if (jsonArray != null && jsonArray.length() > 0) {
								JSONObject object = jsonArray.getJSONObject(0);
								String tradeNo = object.getString("tradeNo");
								LogToFile.i("payhelper", "getTradeInfoFromAPP订单"+tradeNo+"开始获取订单详情");
								Intent broadCastIntent = new Intent();
								broadCastIntent.putExtra("tradeno", tradeNo);
								broadCastIntent.putExtra("cookie", cookie);
								broadCastIntent.setAction(TRADENORECEIVED_ACTION);
								context.sendBroadcast(broadCastIntent);
							}else{
								LogToFile.i("payhelper", "getTradeInfoFromAPP返回空数据"+result);
							}
						}else if(status.equals("failed")){
							setAPI( "PC");
						}else{
							LogToFile.i("payhelper", "getTradeInfoFromAPP返回数据异常"+result);
						}
					}else{
						LogToFile.i("payhelper", "getTradeInfoFromAPP返回数据异常"+result);
					}
				}  catch (Exception e) {
					LogToFile.i("payhelper", "getTradeInfoFromAPP返回数据异常"+result);
				}
			}
		});
	}
	
	public static void getTradeInfoListFromAPP(final Context context) {
		final DBManager dbManager=new DBManager(context);
		final String cookie=getAlipayCookie(context);
		String url="https://mbillexprod.alipay.com/enterprise/walletTradeList.json?lastTradeNo=&lastDate=&pageSize=50&shopId=&_inputcharset=gbk&ctoken&source=&_ksTS="+System.currentTimeMillis()+"_49&_callback=&_input_charset=utf-8";
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configResponseTextCharset("GBK");
		RequestParams params = new RequestParams();
		params.addHeader("Cookie", cookie);
		params.addHeader("Referer", "https://render.alipay.com/p/z/merchant-mgnt/simple-order.html");
		params.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 7.1.1; zh-CN; 1605-A01 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/11.8.8.968 UWS/2.13.1.39 Mobile Safari/537.36 UCBS/2.13.1.39_180615144818 NebulaSDK/1.8.100112 Nebula AlipayDefined(nt:WIFI,ws:360|0|3.0) AliApp(AP/10.1.22.835) AlipayClient/10.1.22.835 Language/zh-Hans useStatusBar/true isConcaveScreen/false");
		params.addHeader("X-Alipay-Client-Session", "check");
		httpUtils.send(HttpMethod.GET, url, params, new RequestCallBack<String>() {
			
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				LogToFile.i("payhelper", "请求支付宝API失败："+arg1);
			}
			
			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result = arg0.result.replace("/**/(", "").replace("})", "}");
				try {
					JSONObject jsonObject = new JSONObject(result);
					if(jsonObject.has("status")){
						String status=jsonObject.getString("status");
						if(status.equals("succeed")){
							JSONObject res = jsonObject.getJSONObject("result");
							JSONArray jsonArray = res.getJSONArray("list");
							if (jsonArray != null && jsonArray.length() > 0) {
								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject object = jsonArray.getJSONObject(i);
									String tradeNo = object.getString("tradeNo");
									if(isFirst){
										dbManager.addTradeNo(tradeNo, "1");
									}else{
										if(!dbManager.isExistTradeNo(tradeNo)){
											LogToFile.i("payhelper", "getTradeInfoListFromAPP订单"+tradeNo+"开始获取订单详情");
											Intent broadCastIntent = new Intent();
											broadCastIntent.putExtra("tradeno", tradeNo);
											broadCastIntent.putExtra("cookie", cookie);
											broadCastIntent.setAction(TRADENORECEIVED_ACTION);
											context.sendBroadcast(broadCastIntent);
										}
									}
								}
								isFirst=false;
							}else{
								LogToFile.i("payhelper", "getTradeInfoListFromAPP返回空数据"+result);
							}
						}else if(status.equals("failed")){
							setAPI( "PC");
						}else{
							LogToFile.i("payhelper", "getTradeInfoListFromAPP返回数据异常"+result);
						}
					}else{
						LogToFile.i("payhelper", "getTradeInfoListFromAPP返回数据异常"+result);
					}
				} catch (Exception e) {
					LogToFile.i("payhelper", "getTradeInfoListFromAPP返回数据异常"+result);
				}
			}
		});
	}
	public static void getTradeInfoFromPC(final Context context,final String cookie,String alipayUserId) {
		long l = System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = new Date(l);
		String s = dateFormat.format(date1);
		s+=" 00:00:00";
		Date date2 = new Date(l+86400000);
		String e = dateFormat.format(date2);
		e+=" 00:00:00";
		
		String url="https://mbillexprod.alipay.com/enterprise/tradeListQuery.json";
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configResponseTextCharset("GBK");
		RequestParams params = new RequestParams();
		params.addHeader("Cookie", cookie);
		params.addHeader("Referer", "https://mbillexprod.alipay.com/enterprise/tradeListQuery.htm");
		params.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
		params.addHeader("X-Requested-With", "XMLHttpRequest");
		params.addBodyParameter("queryEntrance", "1");
		params.addBodyParameter("billUserId", alipayUserId);
		params.addBodyParameter("status", "SUCCESS");
		params.addBodyParameter("entityFilterType", "0");
		params.addBodyParameter("activeTargetSearchItem", "tradeNo");
		params.addBodyParameter("tradeFrom", "ALL");
		params.addBodyParameter("startTime", s);
		params.addBodyParameter("endTime", e);
		params.addBodyParameter("pageSize", "1");
		params.addBodyParameter("pageNum", "1");
		params.addBodyParameter("total", "3");
		params.addBodyParameter("sortTarget", "gmtCreate");
		params.addBodyParameter("order", "descend");
		params.addBodyParameter("sortType", "0");
		params.addBodyParameter("_input_charset", "utf-8");
		params.addBodyParameter("ctoken", "");
		params.addBodyParameter("t", System.currentTimeMillis()+"");
		httpUtils.send(HttpMethod.POST, url, params, new RequestCallBack<String>() {
			
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				sendmsg(context, "请求支付宝API失败："+arg1);
			}
			
			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result = arg0.result.replace("/**/(", "").replace("})", "}");
				try {
					JSONObject jsonObject = new JSONObject(result);
					LogToFile.i("payhelper", "getTradeInfoFromPC获取数据完整，返回数据"+result);
					if(jsonObject.has("status")){
						String status=jsonObject.getString("status");
						if(status.equals("succeed")){
							JSONObject res = jsonObject.getJSONObject("result");
							JSONArray jsonArray = res.getJSONArray("detail");
							if (jsonArray != null && jsonArray.length() > 0) {
								JSONObject object = jsonArray.getJSONObject(0);
								String tradeNo = object.getString("tradeNo");
								String tradeStatus = object.getString("tradeStatus");
								if(tradeStatus.equals("成功")){
									LogToFile.i("payhelper", "getTradeInfoFromPC订单"+tradeNo+"开始获取订单详情");
									Intent broadCastIntent = new Intent();
									broadCastIntent.putExtra("tradeno", tradeNo);
									broadCastIntent.putExtra("cookie", cookie);
									broadCastIntent.setAction(TRADENORECEIVED_ACTION);
									context.sendBroadcast(broadCastIntent);
								}
							}else{
								LogToFile.i("payhelper", "getTradeInfoFromPC返回空数据"+result);
							}
						}else if(status.equals("failed")){
							setAPI( "APP");
						}else{
							LogToFile.i("payhelper", "getTradeInfoFromPC返回数据异常"+result);
						}
					}else{
						LogToFile.i("payhelper", "getTradeInfoFromPC返回数据异常"+result);
					}
				} catch (Exception e) {
					LogToFile.i("payhelper", "getTradeInfoFromPC返回数据异常"+result);
				}
			}
		});
	}
	
	public static void getTradeInfoListFromPC(final Context context) {
		final DBManager dbManager=new DBManager(context);
		final String cookie=getAlipayCookie(context);
		String alipayUserId=AbSharedUtil.getString(context, "alipayUserId");
		long l = System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = new Date(l);
		String s = dateFormat.format(date1);
		s+=" 00:00:00";
		Date date2 = new Date(l+86400000);
		String e = dateFormat.format(date2);
		e+=" 00:00:00";
		
		String url="https://mbillexprod.alipay.com/enterprise/tradeListQuery.json";
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configResponseTextCharset("GBK");
		RequestParams params = new RequestParams();
		params.addHeader("Cookie", cookie);
		params.addHeader("Referer", "https://mbillexprod.alipay.com/enterprise/tradeListQuery.htm");
		params.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
		params.addHeader("X-Requested-With", "XMLHttpRequest");
		params.addBodyParameter("queryEntrance", "1");
		params.addBodyParameter("billUserId", alipayUserId);
		params.addBodyParameter("status", "SUCCESS");
		params.addBodyParameter("entityFilterType", "0");
		params.addBodyParameter("activeTargetSearchItem", "tradeNo");
		params.addBodyParameter("tradeFrom", "ALL");
		params.addBodyParameter("startTime", s);
		params.addBodyParameter("endTime", e);
		params.addBodyParameter("pageSize", "50");
		params.addBodyParameter("pageNum", "1");
		params.addBodyParameter("total", "3");
		params.addBodyParameter("sortTarget", "gmtCreate");
		params.addBodyParameter("order", "descend");
		params.addBodyParameter("sortType", "0");
		params.addBodyParameter("_input_charset", "utf-8");
		params.addBodyParameter("ctoken", "");
		params.addBodyParameter("t", System.currentTimeMillis()+"");
		httpUtils.send(HttpMethod.POST, url, params, new RequestCallBack<String>() {
			
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				LogToFile.i("payhelper", "请求支付宝API失败："+arg1);
			}
			
			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result = arg0.result.replace("/**/(", "").replace("})", "}");
				try {
					JSONObject jsonObject = new JSONObject(result);
					if(jsonObject.has("status")){
						String status=jsonObject.getString("status");
						if(status.equals("succeed")){
							JSONObject res = jsonObject.getJSONObject("result");
							JSONArray jsonArray = res.getJSONArray("detail");
							if (jsonArray != null && jsonArray.length() > 0) {
								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject object = jsonArray.getJSONObject(i);
									String tradeNo = object.getString("tradeNo");
									String tradeStatus = object.getString("tradeStatus");
									if(tradeStatus.equals("成功")){
										if(isFirst){
											dbManager.addTradeNo(tradeNo, "1");
										}else{
											if(!dbManager.isExistTradeNo(tradeNo)){
												LogToFile.i("payhelper", "getTradeInfoListFromPC订单"+tradeNo+"开始获取订单详情");
												Intent broadCastIntent = new Intent();
												broadCastIntent.putExtra("tradeno", tradeNo);
												broadCastIntent.putExtra("cookie", cookie);
												broadCastIntent.setAction(TRADENORECEIVED_ACTION);
												context.sendBroadcast(broadCastIntent);
											}
										}
									}
									
								}
								isFirst=false;
							}else{
								LogToFile.i("payhelper", "getTradeInfoListFromPC返回空数据"+result);
							}
						}else if(status.equals("failed")){
							setAPI( "APP");
						}else{
							LogToFile.i("payhelper", "getTradeInfoListFromPC返回数据异常"+result);
						}
					}else{
						LogToFile.i("payhelper", "getTradeInfoListFromPC返回数据异常"+result);
					}
				} catch (Exception e) {
					LogToFile.i("payhelper", "getTradeInfoListFromPC返回数据异常"+result);
				}
			}
		});
	}

	public static String getCurrentDate() {
		long l = System.currentTimeMillis();
		Date date = new Date(l);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String d = dateFormat.format(date);
		return d;
	}

	public static void sendmsg(Context context, String msg) {
		Intent broadCastIntent = new Intent();
		broadCastIntent.putExtra("msg", msg);
		broadCastIntent.setAction(MSGRECEIVED_ACTION);
		context.sendBroadcast(broadCastIntent);
	}

	/**
	 * 获取当前本地apk的版本
	 *
	 * @param mContext
	 * @return
	 */
	public static int getVersionCode(Context mContext) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			sendmsg(mContext, "getVersionCode异常" + e.getMessage());
		}
		return versionCode;
	}

	/**
	 * 获取版本号名称
	 *
	 * @param context
	 *            上下文
	 * @return
	 */
	public static String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			sendmsg(context, "getVerName异常" + e.getMessage());
		}
		return verName;
	}

	public static boolean isreg(Activity activity, String name) {
		Intent intent = new Intent();
		intent.setAction(name);
		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> resolveInfos = pm.queryBroadcastReceivers(intent, 0);
		if (resolveInfos != null && !resolveInfos.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * 判断某activity是否处于栈顶
	 * 
	 * @return true在栈顶 false不在栈顶
	 */
	public static int isActivityTop(Context context) {
		try {
			ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> infos = manager.getRunningTasks(100);
			for (RunningTaskInfo runningTaskInfo : infos) {
				if (runningTaskInfo.topActivity.getClassName()
						.equals("cooperation.qwallet.plugin.QWalletPluginProxyActivity")) {
					return runningTaskInfo.numActivities;
				}
			}
			return 0;
		} catch (SecurityException e) {
			sendmsg(context, e.getMessage());
			return 0;
		}
	}

	public static String getAlipayLoginId(ClassLoader classLoader) {
		String loginId="";
		try {
			Class<?> AlipayApplication = XposedHelpers.findClass("com.alipay.mobile.framework.AlipayApplication",
					classLoader);
			Class<?> SocialSdkContactService = XposedHelpers
					.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService", classLoader);
			Object instace = XposedHelpers.callStaticMethod(AlipayApplication, "getInstance");
			Object MicroApplicationContext = XposedHelpers.callMethod(instace, "getMicroApplicationContext");
			Object service = XposedHelpers.callMethod(MicroApplicationContext, "findServiceByInterface",
					SocialSdkContactService.getName());
			Object MyAccountInfoModel = XposedHelpers.callMethod(service, "getMyAccountInfoModelByLocal");
//			String userId = XposedHelpers.getObjectField(MyAccountInfoModel, "userId").toString();
			loginId = XposedHelpers.getObjectField(MyAccountInfoModel, "loginId").toString();
		} catch (Exception e) {
		}
		return loginId;
	}
	public static String getWechatLoginId(Context context) {
		String loginId="";
		try {
			SharedPreferences sharedPreferences=context.getSharedPreferences("com.tencent.mm_preferences", 0);
			loginId = sharedPreferences.getString("login_user_name", "");
		} catch (Exception e) {
			PayHelperUtils.sendmsg(context, e.getMessage());
		}
		return loginId;
	}
	public static String getQQLoginId(Context context) {
		String loginId="";
		try {
			SharedPreferences sharedPreferences=context.getSharedPreferences("Last_Login", 0);
			loginId = sharedPreferences.getString("uin", "");
		} catch (Exception e) {
			PayHelperUtils.sendmsg(context, e.getMessage());
		}
		return loginId;
	}
	
	public static void sendLoginId(String loginId, String type, Context context) {
		Intent broadCastIntent = new Intent();
		broadCastIntent.setAction(LOGINIDRECEIVED_ACTION);
		broadCastIntent.putExtra("type", type);
		broadCastIntent.putExtra("loginid", loginId);
		context.sendBroadcast(broadCastIntent);
	}
	
	public static void updateAlipayCookie(Context context,String cookie){
		DBManager dbManager=new DBManager(context);
		if(dbManager.getConfig("cookie").equals("null")){
			dbManager.addConfig("cookie", cookie);
		}else{
			dbManager.updateConfig("cookie", cookie);
		}
	}
	
	public static String getAlipayCookie(Context context){
		DBManager dbManager=new DBManager(context);
		String cookie=dbManager.getConfig("cookie");
		return cookie;
	}
	
	public static void startAlipayMonitor(final Context context){
		try {
			Timer timer=new Timer();
			//默认APP接口
			setAPI("PC");
			TimerTask timerTask=new TimerTask() {
				@Override
				public void run() {
					final DBManager dbManager=new DBManager(context);
					dbManager.saveOrUpdateConfig("time", System.currentTimeMillis()/1000+"");
					if(getAPI().equals("APP")){
						getTradeInfoListFromAPP(context);
					}else if(getAPI().equals("PC")){
						getTradeInfoListFromPC(context);
					}
				}
			};
			int triggerTime=10;
			timer.schedule(timerTask, 0, triggerTime*1000);
		} catch (Exception e) {
			sendmsg(context, "startAlipayMonitor->>"+e.getMessage());
		}
	}
	
	public static String getcurrentTimeMillis(Context context){
		DBManager dbManager=new DBManager(context);
		return dbManager.getConfig("time");
	}
	
	public static void sendBalance(String type, String balance, Context context) {
		Intent broadCastIntent = new Intent();
		broadCastIntent.setAction(UPDATEBALANCE_ACTION);
		broadCastIntent.putExtra("type", type);
		broadCastIntent.putExtra("balance", balance);
		context.sendBroadcast(broadCastIntent);
	}
	
	public static String getBalance(String type, Context context) {
		String balance=AbSharedUtil.getString(context, type+"balance");
		return balance;
	}
	
	public static double getWechatBalance(ClassLoader classLoader) {
		//获取余额操作
		double balance=0.0;
		Class<?> p=XposedHelpers.findClass("com.tencent.mm.plugin.wallet.a.p", classLoader);
		XposedHelpers.callStaticMethod(p, "bNp");
		Object ag=XposedHelpers.callStaticMethod(p, "bNq");
		Object paw=XposedHelpers.getObjectField(ag, "paw");
		if(paw!=null){
			balance=(Double) XposedHelpers.getObjectField(paw, "plV");
		}
		return balance;
	}
	
	public static String getOrderId() {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        String newDate=sdf.format(new Date());
        String result="";
        Random random=new Random();
        for(int i=0;i<3;i++){
            result+=random.nextInt(10);
        }
        return newDate+result;
    }
}
