package com.tools.payhelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.tools.payhelper.utils.AbSharedUtil;
import com.tools.payhelper.utils.BitmapUtil;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.LogUtils;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.QrCodeBean;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import fi.iki.elonen.NanoHTTPD;

/**
 *

* @ClassName: WebServer

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:26:56

*
 */
public class WebServer extends NanoHTTPD {

	public static final String TAG = WebServer.class.getSimpleName();
	private static final String REQUEST_ROOT = "/";
	private static final String REQUEST_WECHAT = "/wechat";
	private static final String REQUEST_GETPAY = "/getpay";
	private static final String REQUEST_QUERY = "/query";
	private static final String REQUEST_GETRESULT = "/getresult";
	public static String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
	private Context context;

	public WebServer(Context context,int serverport) {
		super(serverport);
		this.context = context;
	}

	@Override
	public Response serve(IHTTPSession session) {
		LogUtils.d("OnRequest: " + session.getUri());
		try {
			if (REQUEST_ROOT.equals(session.getUri())) {
				return responseRootPage(session);
			} else if (REQUEST_WECHAT.equals(session.getUri())) {
				@SuppressWarnings("deprecation")
				Map<String, String> params = session.getParms();
				String money = params.get("money");
				String mark = params.get("mark");
				String type=params.get("type");
				if(type==null || type.equals("")){
					type="wechat";
				}
				double m=Double.parseDouble(money);
				if(type.equals("alipay")){
					if(m>50000){
						return responseText(session, "支付宝最大支持单笔50000元支付！");
					}
				}else if(type.equals("wechat")){
					if(m>15000){
						return responseText(session, "微信最大支持单笔15000元支付！");
					}
				}else if(type.equals("qq")){
					if(m>30000){
						return responseText(session, "QQ最大支持单笔30000元支付！");
					}
					if(mark.length()>12){
						return responseText(session, "QQ备注长度不能超过12位！");
					}
				}

				if(type.equals("alipay") && !PayHelperUtils.isAppRunning(context, "com.eg.android.AlipayGphone")){
					PayHelperUtils.startAPP(context, "com.eg.android.AlipayGphone");
				}else if(type.equals("wechat") && !PayHelperUtils.isAppRunning(context, "com.tencent.mm")){
					PayHelperUtils.startAPP(context, "com.tencent.mm");
				}else if(type.equals("qq") && !PayHelperUtils.isAppRunning(context, "com.tencent.mobileqq")){
					PayHelperUtils.startAPP(context, "com.tencent.mobileqq");
				}

				List<QrCodeBean> qrCodeBeans=new ArrayList<QrCodeBean>();
				DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
				PayHelperUtils.sendAppMsg(money, mark, type, context);
				int times=0;
				while (times<30 && qrCodeBeans.size()==0) {
					qrCodeBeans=dbManager.FindQrCodes(money, mark, type);
					times++;
					Thread.sleep(500);
				}
				if(qrCodeBeans.size()==0){
					PayHelperUtils.startAPP();
					return responseText(session,"获取超时....");
				}else{
					String payurl=qrCodeBeans.get(0).getPayurl();
                    PayHelperUtils.startAPP();
					return responseQRCode(session, payurl,mark);
				}
			} else if (REQUEST_GETPAY.equals(session.getUri())) {
				@SuppressWarnings("deprecation")
				JSONObject jsonObject=new JSONObject();
				Map<String, String> params = session.getParms();
				String money = params.get("money");
				String mark = params.get("mark");
				String type=params.get("type");
				if(type==null || type.equals("")){
					type="wechat";
				}
				String account="";
				double m=Double.parseDouble(money);
				if(type.equals("alipay")){
					account=AbSharedUtil.getString(context, "alipay");
					if(m>50000){
						jsonObject.put("msg", "支付宝最大支持单笔50000元支付！");
						return responseJson(session,jsonObject.toString());
					}
				}else if(type.equals("wechat")){
					account=AbSharedUtil.getString(context, "wechat");
					if(m>15000){
						jsonObject.put("msg", "微信最大支持单笔15000元支付！");
						return responseJson(session,jsonObject.toString());
					}
				}else if(type.equals("qq")){
					account=AbSharedUtil.getString(context, "qq");
					if(m>30000){
						return responseText(session, "QQ最大支持单笔30000元支付！");
					}
					if(mark.length()>12){
						return responseText(session, "QQ备注长度不能超过12位！");
					}
				}

				if(type.equals("alipay") && !PayHelperUtils.isAppRunning(context, "com.eg.android.AlipayGphone")){
					PayHelperUtils.startAPP(context, "com.eg.android.AlipayGphone");
				}else if(type.equals("wechat") && !PayHelperUtils.isAppRunning(context, "com.tencent.mm")){
					PayHelperUtils.startAPP(context, "com.tencent.mm");
				}else if(type.equals("qq") && !PayHelperUtils.isAppRunning(context, "com.tencent.mobileqq")){
					PayHelperUtils.startAPP(context, "com.tencent.mobileqq");
				}

				List<QrCodeBean> qrCodeBeans=new ArrayList<QrCodeBean>();
				DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
				PayHelperUtils.sendAppMsg(money, mark, type, context);
				int times=0;
				while (times<30 && qrCodeBeans.size()==0) {
					qrCodeBeans=dbManager.FindQrCodes(money, mark, type);
					times++;
					Thread.sleep(500);
				}

				LogUtils.d("size = " + qrCodeBeans.size());

				if(qrCodeBeans.size()==0){
					PayHelperUtils.startAPP();
					jsonObject.put("msg", "获取超时");
					return responseJson(session,jsonObject.toString());
				}else{
					String payurl=qrCodeBeans.get(0).getPayurl();
                    PayHelperUtils.startAPP();
                    jsonObject.put("msg", "获取成功");
                    jsonObject.put("payurl", payurl);
                    jsonObject.put("mark", mark);
                    jsonObject.put("money", money);
                    jsonObject.put("type", type);
                    if(!TextUtils.isEmpty(account)){
                    	jsonObject.put("account", account);
                    }

                    LogUtils.d(jsonObject.toString());
                    return responseJson(session,jsonObject.toString());
				}
			} else if (REQUEST_QUERY.equals(session.getUri())) {
				@SuppressWarnings("deprecation")
				Map<String, String> params = session.getParms();
				String mark = params.get("no");
				List<OrderBean> orderBeans=new ArrayList<OrderBean>();
				DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
				orderBeans=dbManager.FindOrders(mark);
				if(orderBeans!=null && orderBeans.size()>0){
					String dt=orderBeans.get(0).getDt();
					String paytime=PayHelperUtils.stampToDate(dt);
					return responseText(session,"当前订单已支付，支付时间:"+paytime+"....");
				}else{
					return responseText(session,"当前订单未支付....");
				}
			} else if (REQUEST_GETRESULT.equals(session.getUri())) {
				@SuppressWarnings("deprecation")
				Map<String, String> params = session.getParms();
				String mark = params.get("trade_no");
				List<OrderBean> orderBeans=new ArrayList<OrderBean>();
				DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
				orderBeans=dbManager.FindOrders(mark);
				JSONObject jsonObject=new JSONObject();
				String returnurl=AbSharedUtil.getString(context, "returnurl");
				if(orderBeans!=null && orderBeans.size()>0){
					String dt=orderBeans.get(0).getDt();
					String money=orderBeans.get(0).getMoney();
					String paytime=PayHelperUtils.stampToDate(dt);
					jsonObject.put("msg", "支付成功");
					jsonObject.put("paytime", paytime);
					jsonObject.put("money", money);
					if(!TextUtils.isEmpty(returnurl)){
						jsonObject.put("returnurl", returnurl);
					}
					return responseJson(session, jsonObject.toString());
				}else{
					jsonObject.put("msg", "未支付");
					return responseJson(session, jsonObject.toString());
				}
			}else{
				return response404(session, session.getUri());
			}
		} catch (Exception e) {
			return response404(session, e.getMessage());
		}
	}

	public Response responseRootPage(IHTTPSession session) {
		StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE html><html><body>");
		builder.append("Hello World!");
		builder.append("</body></html>\n");
		return newFixedLengthResponse(builder.toString());
	}

	public Response responseQRCode(IHTTPSession session,String QRText,String no) {
		Bitmap bitmap=BitmapUtil.createQRImage(QRText, 240, null);
		String imgbase64=BitmapUtil.bitmapToBase64(bitmap);
		imgbase64="\"data:image/gif;base64," + imgbase64 + "\"";
		StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE html><html><body>");
		builder.append("<div style=\"width:100%;height:100%;text-align:center;padding-top:20px;\">");
		builder.append("二维码生成测试<br>");
		builder.append("<image ");
		builder.append("src=" + imgbase64 + " >");
		builder.append("</image><br>");
		String url="http://"+session.getHeaders().get("host");
		builder.append("获取成功，查询订单是否支付：<a href='"+url+"/query?no="+no+"' target='_blank'>查询</a><br><br>");
		builder.append("</div>");
		builder.append("</body></html>\n");
		LogUtils.i(builder.toString());
		return newFixedLengthResponse(Response.Status.OK, "text/html;charset=UTF-8", builder.toString());
	}
	public Response responseText(IHTTPSession session, String text) {
		StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE html><html><body>");
		builder.append("<div style=\"width:100%;height:100%;text-align:center;padding-top:20px;\">");
		builder.append(text);
		builder.append("</div>");
		builder.append("</body></html>\n");
		LogUtils.i(builder.toString());
		return newFixedLengthResponse(Response.Status.OK, "text/html;charset=UTF-8", builder.toString());
	}
	public Response responseJson(IHTTPSession session, String json) {
		LogUtils.i(json);
		return newFixedLengthResponse(Response.Status.OK, "application/json;charset=UTF-8", json);
	}

	public Response response404(IHTTPSession session, String url) {
		StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE html><html><body>");
		builder.append("Sorry, Can't Found " + url + " !");
		builder.append("</body></html>\n");
		return newFixedLengthResponse(builder.toString());
	}

	protected String getQuotaStr(String text) {
		return "\"" + text + "\"";
	}
}
