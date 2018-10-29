package com.tools.payhelper;



import java.lang.reflect.Field;
import java.util.Map;

import org.json.JSONObject;

import com.tools.payhelper.utils.LogToFile;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * 

* @ClassName: AlipayHook

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:25:54

*
 */

public class AlipayHook {

	public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
	public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";
	public static String SAVEALIPAYCOOKIE_ACTION = "com.tools.payhelper.savealipaycookie";

    public void hook(final ClassLoader classLoader,final Context context) {
        securityCheckHook(classLoader);
        try {
            Class<?> insertTradeMessageInfo = XposedHelpers.findClass("com.alipay.android.phone.messageboxstatic.biz.dao.TradeDao", classLoader);
            XposedBridge.hookAllMethods(insertTradeMessageInfo, "insertMessageInfo", new XC_MethodHook() {
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		try {
            			XposedBridge.log("======支付宝个人账号订单start=========");
            			
            			//更新cookie
                		Intent cookieBroadCastIntent = new Intent();
                		String alipaycookie=PayHelperUtils.getCookieStr(classLoader);
                		cookieBroadCastIntent.putExtra("alipaycookie", alipaycookie);
                		cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
                        context.sendBroadcast(cookieBroadCastIntent);
            			
            			//获取content字段
//            			String content=(String) XposedHelpers.getObjectField(param.args[0], "content");
//            			XposedBridge.log(content);
            			//获取全部字段
            			Object object = param.args[0];
            			String MessageInfo = (String) XposedHelpers.callMethod(object, "toString");
            			XposedBridge.log(MessageInfo);
            			String content=StringUtils.getTextCenter(MessageInfo, "content='", "'");
            			if(content.contains("二维码收款") || content.contains("收到一笔转账")){
            				JSONObject jsonObject=new JSONObject(content);
                			String money=jsonObject.getString("content").replace("￥", "");
                			String mark=jsonObject.getString("assistMsg2");
                			String tradeNo=StringUtils.getTextCenter(MessageInfo,"tradeNO=","&");
                			XposedBridge.log("收到支付宝支付订单："+tradeNo+"=="+money+"=="+mark);
                			
                			Intent broadCastIntent = new Intent();
                			broadCastIntent.putExtra("bill_no", tradeNo);
                            broadCastIntent.putExtra("bill_money", money);
                            broadCastIntent.putExtra("bill_mark", mark);
                            broadCastIntent.putExtra("bill_type", "alipay");
                            broadCastIntent.setAction(BILLRECEIVED_ACTION);
                            context.sendBroadcast(broadCastIntent);
            			}
                        XposedBridge.log("======支付宝个人账号订单end=========");
            		} catch (Exception e) {
            			XposedBridge.log(e.getMessage());
            		}
            		super.beforeHookedMethod(param);
            	}
            });
           Class<?> insertServiceMessageInfo = XposedHelpers.findClass("com.alipay.android.phone.messageboxstatic.biz.dao.ServiceDao", classLoader);
            XposedBridge.hookAllMethods(insertServiceMessageInfo, "insertMessageInfo", new XC_MethodHook() {
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		try {
						XposedBridge.log("======支付宝商家服务订单start=========");
						
						//更新cookie
                		Intent cookieBroadCastIntent = new Intent();
                		String alipaycookie=PayHelperUtils.getCookieStr(classLoader);
                		cookieBroadCastIntent.putExtra("alipaycookie", alipaycookie);
                		cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
                        context.sendBroadcast(cookieBroadCastIntent);
						
						Object object = param.args[0];
						String MessageInfo = (String) XposedHelpers.callMethod(object, "toString");
						String content=StringUtils.getTextCenter(MessageInfo, "extraInfo='", "'").replace("\\", "");
						XposedBridge.log(content);
						if(content.contains("店员通")){
							String money=StringUtils.getTextCenter(content, "mainAmount\":\"", "\",\"mainTitle");
							String time=StringUtils.getTextCenter(content, "gmtCreate\":", ",gmtValid");
							String no=PayHelperUtils.getOrderId();
							Intent broadCastIntent = new Intent();
                			broadCastIntent.putExtra("bill_no", no);
                            broadCastIntent.putExtra("bill_money", money);
                            broadCastIntent.putExtra("bill_mark", "");
                            broadCastIntent.putExtra("bill_time", time);
                            broadCastIntent.putExtra("bill_type", "alipay_dy");
                            broadCastIntent.setAction(BILLRECEIVED_ACTION);
                            context.sendBroadcast(broadCastIntent);
						}else if(content.contains("收钱到账") || content.contains("收款到账")){
							LogToFile.i("payhelper", "Hook到商家服务通知，开始调用getBill获取订单详细信息");
							String userId=PayHelperUtils.getAlipayUserId(classLoader);
							PayHelperUtils.getBill(context,alipaycookie,userId);
						}
						XposedBridge.log("======支付宝商家服务订单end=========");
					} catch (Exception e) {
						PayHelperUtils.sendmsg(context, e.getMessage());
					}
            		super.beforeHookedMethod(param);
            	}
            });
            
            // hook设置金额和备注的onCreate方法，自动填写数据并点击
            XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	XposedBridge.log("========支付宝设置金额start=========");
                	
                	//更新cookie
            		Intent cookieBroadCastIntent = new Intent();
            		String alipaycookie=PayHelperUtils.getCookieStr(classLoader);
            		cookieBroadCastIntent.putExtra("alipaycookie", alipaycookie);
            		cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
                    context.sendBroadcast(cookieBroadCastIntent);
                	
                    Field jinErField = XposedHelpers.findField(param.thisObject.getClass(), "b");
                    final Object jinErView = jinErField.get(param.thisObject);
                    Field beiZhuField = XposedHelpers.findField(param.thisObject.getClass(), "c");
                    final Object beiZhuView = beiZhuField.get(param.thisObject);
                    Intent intent = ((Activity) param.thisObject).getIntent();
					String mark=intent.getStringExtra("mark");
					String money=intent.getStringExtra("money");
					//设置支付宝金额和备注
                    XposedHelpers.callMethod(jinErView, "setText", money);
                    XposedHelpers.callMethod(beiZhuView, "setText", mark);
                    //点击确认
                    Field quRenField = XposedHelpers.findField(param.thisObject.getClass(), "e");
                    final Button quRenButton = (Button) quRenField.get(param.thisObject);
                    quRenButton.performClick();
                    XposedBridge.log("=========支付宝设置金额end========");
                }
            });
            
            // hook获得二维码url的回调方法
            XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader, "a",
            		XposedHelpers.findClass("com.alipay.transferprod.rpc.result.ConsultSetAmountRes", classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                	XposedBridge.log("=========支付宝生成完成start========");
                    Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "g");
					String money = (String) moneyField.get(param.thisObject);
					
					Field markField = XposedHelpers.findField(param.thisObject.getClass(), "c");
					Object markObject = markField.get(param.thisObject);
					String mark=(String) XposedHelpers.callMethod(markObject, "getUbbStr");
					
					Object consultSetAmountRes = param.args[0];
					Field consultField = XposedHelpers.findField(consultSetAmountRes.getClass(), "qrCodeUrl");
					String payurl = (String) consultField.get(consultSetAmountRes);
					XposedBridge.log(money+"  "+mark+"  "+payurl);
					
					if(money!=null){
						XposedBridge.log("调用增加数据方法==>支付宝");
						Intent broadCastIntent = new Intent();
	                    broadCastIntent.putExtra("money", money);
	                    broadCastIntent.putExtra("mark", mark);
	                    broadCastIntent.putExtra("type", "alipay");
	                    broadCastIntent.putExtra("payurl", payurl);
	                    broadCastIntent.setAction(QRCODERECEIVED_ACTION);
	                    context.sendBroadcast(broadCastIntent);
					}
					XposedBridge.log("=========支付宝生成完成end========");
                }
            });
            
            // hook获取loginid
            XposedHelpers.findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", classLoader, "onResume",
            		 new XC_MethodHook() {
            	@Override
            	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            		PayHelperUtils.isFirst=true;
            		String loginid=PayHelperUtils.getAlipayLoginId(classLoader);
            		PayHelperUtils.sendLoginId(loginid, "alipay", context);
            	}
            });
            
            //拦截“人气大爆发，一会再试试”
            XposedHelpers.findAndHookMethod("com.alipay.mobile.antui.basic.AUDialog", classLoader, "show",
            		new XC_MethodHook() {
            	
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		Context mContext=(Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
            		if (mContext.getClass().getSimpleName().equals("PayeeQRSetMoneyActivity")){
            			XposedHelpers.setObjectField(param.thisObject, "mContext", null);
            		}
            	}
            	
            });
            
          //拦截设置cookie
            XposedHelpers.findAndHookMethod("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", classLoader, "setCookies",String.class,Map.class,
            		new XC_MethodHook() {
            	
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		String domain=param.args[0].toString();
            		Map<String, String> cookie=(Map<String, String>)param.args[1];
            		String ck=(String) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", classLoader), "toCookieString",cookie);
            		if(ck.contains("ALIPAYJSESSIONID")){
            			//更新cookie
            			Intent cookieBroadCastIntent = new Intent();
            			cookieBroadCastIntent.putExtra("alipaycookie", ck);
            			cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
            			context.sendBroadcast(cookieBroadCastIntent);
            		}
            	}
            	
            });
        } catch (Error | Exception e) {
        	PayHelperUtils.sendmsg(context, e.getMessage());
        }
    }

    private void securityCheckHook(ClassLoader classLoader) {
        try {
            Class<?> securityCheckClazz = XposedHelpers.findClass("com.alipay.mobile.base.security.CI", classLoader);
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", String.class, String.class, String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object object = param.getResult();
                    XposedHelpers.setBooleanField(object, "a", false);
                    param.setResult(object);
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", Class.class, String.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", ClassLoader.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
                }
            });

        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
}