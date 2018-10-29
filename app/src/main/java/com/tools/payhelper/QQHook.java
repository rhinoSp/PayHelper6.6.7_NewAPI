package com.tools.payhelper;



import com.tools.payhelper.utils.LogToFile;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * 

* @ClassName: QQHook

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:26:39

*
 */

public class QQHook {

	public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
	
    public void hook(final ClassLoader classLoader,final Context context) {
    	try {
			XposedHelpers.findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", classLoader, "a",
			    "com.tencent.mobileqq.app.QQAppInterface",
			    "com.tencent.mobileqq.data.MessageRecord", Boolean.TYPE, new XC_MethodHook() {
			        @Override
			        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			        	byte[] msgData= (byte[]) XposedHelpers.getObjectField(param.args[1], "msgData");
			        	Class<?> clazz=XposedHelpers.findClass("com.tencent.mobileqq.structmsg.StructMsgFactory", classLoader);
						Object object=XposedHelpers.callStaticMethod(clazz, "a", msgData);
						if(object!=null){
							String xml=(String) XposedHelpers.callMethod(object, "getXml");
							XposedBridge.log(xml);
							PayHelperUtils.sendmsg(context, xml);
							if(xml.contains("转账金额")){
								XposedBridge.log("=========qq钱包收到订单start========");
								String tradeno=StringUtils.getTextCenter(xml, "transId=", "\"");
								String money=StringUtils.getTextCenter(xml, "转账金额：", "</summary>");
								String mark=StringUtils.getTextCenter(xml, "转账留言：", "</summary>");
								XposedBridge.log("收到qq支付订单："+tradeno+"=="+money+"=="+mark);
								Intent broadCastIntent = new Intent();
				    			broadCastIntent.putExtra("bill_no", tradeno);
				                broadCastIntent.putExtra("bill_money", money);
				                broadCastIntent.putExtra("bill_mark", mark);
				                broadCastIntent.putExtra("bill_type", "qq");
				                broadCastIntent.setAction(BILLRECEIVED_ACTION);
				                context.sendBroadcast(broadCastIntent);
				                XposedBridge.log("=========qq钱包收到订单start========");
							}
						}
			        }
			    }
			);
			XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", classLoader, "doOnResume",
						new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Activity activity=(Activity) param.thisObject;
						String loginid=PayHelperUtils.getQQLoginId(activity);
						PayHelperUtils.sendLoginId(loginid, "qq", activity);
			    	}
				}
			);
		} catch (Exception e) {
			PayHelperUtils.sendmsg(context, "QQHook异常");
		}
    	/*XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", classLoader, "doOnCreate",
    			Bundle.class, new XC_MethodHook() {
	    		@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	    			Activity activity=(Activity) param.thisObject;
	    			PayHelperUtils.sendmsg(activity, "SplashActivity---->doOnCreate");
	    			ClassLoader walletClassLoader = (ClassLoader) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.tencent.mobileqq.pluginsdk.PluginStatic", classLoader), "getOrCreateClassLoader", context, "qwallet_plugin.apk");
			    	if(walletClassLoader!=null){
			    		PayHelperUtils.sendmsg(context, "walletClassLoader不为空");
			    	}else{
			    		PayHelperUtils.sendmsg(context, "walletClassLoader为空");
			    	}
		    	}
	    	}
    	);
    	XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.JumpActivity", classLoader, "doOnCreate",
    			Bundle.class, new XC_MethodHook() {
	    		@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	    			Activity activity=(Activity) param.thisObject;
	    			PayHelperUtils.sendmsg(activity, "JumpActivity---->doOnCreate");
	    			Intent intent=(Intent) XposedHelpers.callMethod(param.thisObject, "getIntent");
	    			String url=intent.getDataString();
	    			PayHelperUtils.sendmsg(activity, url);
	    		}
	    	}
    	);*/
    }
}