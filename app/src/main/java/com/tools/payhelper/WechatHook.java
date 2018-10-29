package com.tools.payhelper;

import java.lang.reflect.Field;

import org.json.JSONObject;

import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.XmlToJson;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

/**
 * 

* @ClassName: WechatHook

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:27:01

*
 */
public class WechatHook {
	
	public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
	public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";
	
	protected void hook(final ClassLoader appClassLoader,final Context context) {
		// TODO Auto-generated method stub
		
		XposedHelpers.findAndHookMethod("com.tencent.wcdb.database.SQLiteDatabase",appClassLoader, "insert",String.class, String.class, ContentValues.class,
				new XC_MethodHook() {
			
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				 try {
                    ContentValues contentValues = (ContentValues) param.args[2];
                    String tableName = (String) param.args[0];
                    if (TextUtils.isEmpty(tableName) || !tableName.equals("message")) {
                        return;
                    }
                    Integer type = contentValues.getAsInteger("type");
                    if (null == type) {
                        return;
                    }
                    if(type==318767153){
						 JSONObject msg=new XmlToJson.Builder(contentValues.getAsString("content")).build().getJSONObject("msg");
						 XposedBridge.log(msg.toString());
						 if(!msg.toString().contains("零钱提现")){
							 XposedBridge.log("=========微信收到订单start========");
							 String money=msg.getJSONObject("appmsg").getJSONObject("mmreader").getJSONObject("template_detail").getJSONObject("line_content").getJSONObject("topline").getJSONObject("value").getString("word");
							 money=money.replace("￥", "");
							 String mark=msg.getJSONObject("appmsg").getJSONObject("mmreader").getJSONObject("template_detail").getJSONObject("line_content").getJSONObject("lines").getJSONArray("line").getJSONObject(0).getJSONObject("value").getString("word");
							 String pay_outtradeno="";
							 try {
								 pay_outtradeno=msg.getJSONObject("appmsg").getJSONObject("ext_pay_info").getString("pay_outtradeno");
							 } catch (Exception e) {
								 pay_outtradeno=msg.getJSONObject("appmsg").getString("template_id");
							 }
							 XposedBridge.log("收到微信支付订单："+pay_outtradeno+"=="+money+"=="+mark);
							 Intent broadCastIntent = new Intent();
							 broadCastIntent.putExtra("bill_no", pay_outtradeno);
							 broadCastIntent.putExtra("bill_money", money);
							 broadCastIntent.putExtra("bill_mark", mark);
							 broadCastIntent.putExtra("bill_type", "wechat");
							 broadCastIntent.setAction(BILLRECEIVED_ACTION);
							 context.sendBroadcast(broadCastIntent);
							 XposedBridge.log("=========微信收到订单start========");
						}
					}
                } catch (Exception e) {
                	XposedBridge.log(e.getMessage());
                }
			}
			
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
			}
		});
		
		//hook请求参数
//		try {
//			XposedHelpers.findAndHookMethod("com.tencent.mm.wallet_core.c.i",appClassLoader, "E", Map.class,
//					new XC_MethodHook() {
//				
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param)
//						throws Throwable {
//					Map<String,String> map=(Map<String, String>) param.args[0];
//					XposedBridge.log(map.toString());
//				}
//				
//				@Override
//				protected void afterHookedMethod(MethodHookParam param)
//						throws Throwable {
//				}
//			});
//		} catch (Exception e) {
//		}
		
		//hook更改请求参数
//		try {
//			XposedHelpers.findAndHookConstructor("com.tencent.mm.plugin.collect.b.s", appClassLoader, double.class,String.class,String.class,new XC_MethodHook() {
//				
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param)
//						throws Throwable {
//					String mark="备注啦啦啦";
//					param.args[2]=mark;
//					XposedBridge.log("拦截请求修改参数:mark="+param.args[2].toString()+"money="+String.valueOf(param.args[0])+"type="+param.args[1].toString());
//				}
//				
//				@Override
//				protected void afterHookedMethod(MethodHookParam param)
//						throws Throwable {
//				}
//			});
//		} catch (Exception e) {
//		}
		//获取请求返回数据
		/*try {
			Class<?> bfj=XposedHelpers.findClass("com.tencent.mm.protocal.c.bfj", appClassLoader);
			XposedHelpers.findAndHookMethod("com.tencent.mm.platformtools.aa",appClassLoader, "b", bfj,
					new XC_MethodHook() {
				
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
				}
				
				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					if(param.args[0]!=null){
						String result=param.getResult().toString();
						XposedBridge.log("拦截返回数据=="+result);
					}
				}
			});
			
		} catch (Exception e) {
		}*/
		try {
			Class<?> clazz=XposedHelpers.findClass("com.tencent.mm.plugin.collect.b.s", appClassLoader);
			XposedBridge.hookAllMethods(clazz, "a", new XC_MethodHook() {
				
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
				}
				
				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					XposedBridge.log("=========微信生成完成start========");
					if(PayHelperUtils.getVerName(context).equals("6.6.7")){
						Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "hUL");
						double money = (double) moneyField.get(param.thisObject);
						
						Field markField = XposedHelpers.findField(param.thisObject.getClass(), "desc");
						String mark = (String) markField.get(param.thisObject);
						
						Field payurlField = XposedHelpers.findField(param.thisObject.getClass(), "hUK");
						String payurl = (String) payurlField.get(param.thisObject);
						
						XposedBridge.log(money+"  "+mark+"  "+payurl);
						
						XposedBridge.log("调用增加数据方法==>微信");
						Intent broadCastIntent = new Intent();
	                    broadCastIntent.putExtra("money", money+"");
	                    broadCastIntent.putExtra("mark", mark);
	                    broadCastIntent.putExtra("type", "wechat");
	                    broadCastIntent.putExtra("payurl", payurl);
	                    broadCastIntent.setAction(QRCODERECEIVED_ACTION);
	                    context.sendBroadcast(broadCastIntent);
					}else if(PayHelperUtils.getVerName(context).equals("6.6.6")){
						Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "llG");
						double money = (double) moneyField.get(param.thisObject);
						
						Field markField = XposedHelpers.findField(param.thisObject.getClass(), "desc");
						String mark = (String) markField.get(param.thisObject);
						
						Field payurlField = XposedHelpers.findField(param.thisObject.getClass(), "llF");
						String payurl = (String) payurlField.get(param.thisObject);
						
						XposedBridge.log(money+"  "+mark+"  "+payurl);
						
						XposedBridge.log("调用增加数据方法==>微信");
						Intent broadCastIntent = new Intent();
	                    broadCastIntent.putExtra("money", money+"");
	                    broadCastIntent.putExtra("mark", mark);
	                    broadCastIntent.putExtra("type", "wechat");
	                    broadCastIntent.putExtra("payurl", payurl);
	                    broadCastIntent.setAction(QRCODERECEIVED_ACTION);
	                    context.sendBroadcast(broadCastIntent);
					}
					XposedBridge.log("=========微信生成完成end========");
				}
			});
			
		} catch (Exception e) {
			PayHelperUtils.sendmsg(context, "异常"+e.getMessage());
		}
		try {
			XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI",appClassLoader, "initView",
					new XC_MethodHook() {
				
				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
				}
				
				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					//微信6.6.7新版修复
					XposedBridge.log("=========微信设置金额start========");
					if(PayHelperUtils.getVerName(context).equals("6.6.7")){
						Intent intent = ((Activity) param.thisObject).getIntent();
						String mark=intent.getStringExtra("mark");
						String money=intent.getStringExtra("money");
						//获取WalletFormView控件
						Field WalletFormViewField = XposedHelpers.findField(param.thisObject.getClass(), "hXD");
						Object WalletFormView = WalletFormViewField.get(param.thisObject);
						Class<?> WalletFormViewClass=XposedHelpers.findClass("com.tencent.mm.wallet_core.ui.formview.WalletFormView", appClassLoader);
						//获取金额控件
						Field AefField = XposedHelpers.findField(WalletFormViewClass, "uZy");
						Object AefView = AefField.get(WalletFormView);
						//call设置金额方法
						XposedHelpers.callMethod(AefView, "setText", money);
						//call设置备注方法
						Class<?> clazz=XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
						XposedHelpers.callStaticMethod(clazz, "a", param.thisObject,mark);
						XposedHelpers.callStaticMethod(clazz, "c", param.thisObject);
						//点击确定
						Button click=(Button)XposedHelpers.callMethod(param.thisObject, "findViewById",2131756838);
						click.performClick();
					}else if(PayHelperUtils.getVerName(context).equals("6.6.6")){
						Intent intent = ((Activity) param.thisObject).getIntent();
						String mark=intent.getStringExtra("mark");
						String money=intent.getStringExtra("money");
						//获取WalletFormView控件
						Field WalletFormViewField = XposedHelpers.findField(param.thisObject.getClass(), "loz");
						Object WalletFormView = WalletFormViewField.get(param.thisObject);
						Class<?> WalletFormViewClass=XposedHelpers.findClass("com.tencent.mm.wallet_core.ui.formview.WalletFormView", appClassLoader);
						//获取金额控件
						Field AefField = XposedHelpers.findField(WalletFormViewClass, "Aef");
						Object AefView = AefField.get(WalletFormView);
						//call设置金额方法
						XposedHelpers.callMethod(AefView, "setText", money);
						//call设置备注方法
						Class<?> clazz=XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", appClassLoader);
						XposedHelpers.callStaticMethod(clazz, "a", param.thisObject,mark);
						XposedHelpers.callStaticMethod(clazz, "c", param.thisObject);
						//点击确定
						Button click=(Button)XposedHelpers.callMethod(param.thisObject, "findViewById",2131756780);
						click.performClick();
					}
					XposedBridge.log("=========微信设置金额start========");
//					Field MoneyField = XposedHelpers.findField(WalletFormViewClass, "jyA");
//					Object MoneyView = MoneyField.get(WalletFormView);
////					
//					Field MarkField = XposedHelpers.findField(WalletFormViewClass, "qdA");
//					Object MarkView = MarkField.get(WalletFormView);
//					
//					Field qdDField = XposedHelpers.findField(WalletFormViewClass, "qdD");
//					Object qdDView = qdDField.get(WalletFormView);
					
//					XposedHelpers.callMethod(MoneyView, "setText", "1");
//                  XposedHelpers.callMethod(MarkView, "setText", "2");
//                  XposedHelpers.callMethod(qdDView, "setText", "3");
                    
//                  Button click=(Button)XposedHelpers.callMethod(param.thisObject, "findViewById",2131756780);
//                  click.performClick();
//                  Field quRenField = XposedHelpers.findField(param.thisObject.getClass(), "loA");
//                  Button quRenButton = (Button) quRenField.get(param.thisObject);
//                  quRenButton.performClick();
//					XposedHelpers.callMethod(constructor.newInstance(0.01d,"1","test"));
//		        	Class clazz = XposedHelpers.findClass("com.tencent.mm.plugin.collect.b.s",context.getClassLoader());
//					Constructor constructor = clazz.getConstructor(double.class,String.class,String.class);
//					Object object=constructor.newInstance(10,"1","test");
//					Class<?> test = XposedHelpers.findClass("com.tencent.mm.wallet_core.ui.WalletBaseUI", context.getClassLoader());
//					XposedHelpers.callMethod(test.newInstance(), "a", object,true,true);
				}
			});
		} catch (Exception e) {
			PayHelperUtils.sendmsg(context, "异常"+e.getMessage());
		}
		try {
			// hook获取loginid
            XposedHelpers.findAndHookMethod("com.tencent.mm.ui.LauncherUI", appClassLoader, "onResume",
            		 new XC_MethodHook() {
            	@Override
            	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            		String loginid=PayHelperUtils.getWechatLoginId(context);
            		loginid=loginid.replace("+86", "");
            		PayHelperUtils.sendLoginId(loginid, "wechat", context);
            	}
            });
		} catch (Exception e) {
		}
	}
}
