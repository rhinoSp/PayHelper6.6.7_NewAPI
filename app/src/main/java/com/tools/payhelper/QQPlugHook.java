package com.tools.payhelper;



import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.QQDBManager;
import com.tools.payhelper.utils.QrCodeBean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * 

* @ClassName: QQPlugHook

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:26:46

*
 */

public class QQPlugHook {
	
	public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";

    public void hook(final ClassLoader classLoader) {
    	XposedHelpers.findAndHookMethod("com.tenpay.sdk.activity.QrcodePayActivity", classLoader, "onCreate",
    			Bundle.class, new XC_MethodHook() {
	    		@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	    			XposedBridge.log("=========qq钱包打开付款start========");
	    			Activity activity=(Activity) param.thisObject;
//	    			PayHelperUtils.sendmsg(activity, "QrcodePayActivity---->>onCreate");
    				XposedHelpers.setIntField(param.thisObject, "n", 1);
    				XposedHelpers.setBooleanField(param.thisObject, "o", true);
    				XposedHelpers.callMethod(param.thisObject, "d");
    				View view=new View(activity);
    				view.setId(2131363428);
    				XposedHelpers.callMethod(param.thisObject, "onClick", view);
    				XposedBridge.log("=========qq钱包打开付款end========");
		    	}
	    	}
    	);
    	
    	XposedHelpers.findAndHookMethod("com.tenpay.sdk.activity.QrcodeSettingActivity", classLoader, "onCreate",
    			Bundle.class, new XC_MethodHook() {
	    		@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	    			XposedBridge.log("=========qq钱包设置金额start========");
	    			Activity activity=(Activity) param.thisObject;
	    			QQDBManager qqdbManager=new QQDBManager(activity);
	    			QrCodeBean qrCodeBean=qqdbManager.GetQQMark();
	    			String money=qrCodeBean.getMoney();
	    			String mark=qrCodeBean.getMark();
	    			if(!TextUtils.isEmpty(money) && !TextUtils.isEmpty(mark)){
	    				qqdbManager.updateOrder(money, mark);
		    			Object d= XposedHelpers.getObjectField(param.thisObject, "d");
		    			XposedHelpers.callMethod(d, "setText",money);
		    			Object e= XposedHelpers.getObjectField(param.thisObject, "e");
		    			XposedHelpers.callMethod(e, "setText",mark);
		    			Button c= (Button) XposedHelpers.getObjectField(param.thisObject, "c");
		    			c.performClick();
	    			}
	    			XposedBridge.log("=========qq钱包设置金额end========");
		    	}
	    	}
    	);
    	/*XposedHelpers.findAndHookMethod("com.tenpay.sdk.activity.NetBaseActivity", classLoader, "a",
    			String.class,Map.class, new XC_MethodHook() {
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    			Activity activity=(Activity) param.thisObject;
    			Map<String, String> map=(Map<String, String>) param.args[1];
    			String url=(String) param.args[0];
    			StringBuffer buffer=new StringBuffer();
    			for (Entry<String, String> entry : map.entrySet()) { 
    				buffer.append(entry.getKey());
    				buffer.append("=");
    				buffer.append(entry.getValue());
    				buffer.append("&");
    			}
    			PayHelperUtils.sendmsg(activity, "NetBaseActivity---->>a");
    			PayHelperUtils.sendmsg(activity, url+buffer.substring(0, buffer.toString().length()-1));
    		}
    	}
    			);
    	XposedHelpers.findAndHookMethod("com.tenpay.sdk.h.y", classLoader, "c",
    			String.class, new XC_MethodHook() {
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    			String header=(String) param.args[0];
    			XposedBridge.log(header);
    		}
    	}
    			);
    	XposedHelpers.findAndHookMethod("com.tenpay.sdk.activity.QrcodePayActivity", classLoader, "a",
    			String.class,JSONObject.class, new XC_MethodHook() {
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    			Activity activity=(Activity) param.thisObject;
    			JSONObject jsonObject=(JSONObject) param.args[1];
    			PayHelperUtils.sendmsg(activity, jsonObject.toString());
    		}
    	}
    			);*/
    	XposedHelpers.findAndHookMethod("com.tenpay.sdk.activity.QrcodePayActivity", classLoader, "c",
    			String.class, new XC_MethodHook() {
	    		@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	    			XposedBridge.log("=========qq钱包生成完成start========");
	    			Activity activity=(Activity) param.thisObject;
	    			String payurl="https://i.qianbao.qq.com/wallet/sqrcode.htm?m=tenpay&f=wallet&";
	    			
	    			String u= (String) XposedHelpers.getObjectField(param.thisObject, "eb");
	    			String n= (String) XposedHelpers.getObjectField(param.thisObject, "ec");
	    			String ac= (String) param.args[0];
	    			payurl+="u="+u+"&a=1"+"&n="+n+"&ac="+ac;
	    			
	    			String money=(String) XposedHelpers.getObjectField(param.thisObject, "aa");
	    			String mark=(String) XposedHelpers.getObjectField(param.thisObject, "ab");
	    			
	    			if(!TextUtils.isEmpty(money) && !TextUtils.isEmpty(mark) && ac.length()>64){
	    				XposedBridge.log("调用增加数据方法==>QQ");
	    				Intent broadCastIntent = new Intent();
	    				broadCastIntent.putExtra("money", money+"");
	    				broadCastIntent.putExtra("mark", mark);
	    				broadCastIntent.putExtra("type", "qq");
	    				broadCastIntent.putExtra("payurl", payurl);
	    				broadCastIntent.setAction(QRCODERECEIVED_ACTION);
	    				activity.sendBroadcast(broadCastIntent);
	    				int activitys=PayHelperUtils.isActivityTop(activity);
	    				if(activitys>2){
	    					activity.finish();
	    				}
	    			}
	    			XposedBridge.log("=========qq钱包生成完成end========");
	    		}
	    	}
    	);
    }
    
}