package com.tools.payhelper;

import java.util.List;

import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.PayHelperUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 

* @ClassName: AlarmReceiver

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:25:47

*
 */

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			DBManager dbManager=new DBManager(context);
			List<OrderBean> orderBeans=dbManager.FindAllOrders();
			for (OrderBean orderBean : orderBeans) {
				PayHelperUtils.notify(context, orderBean.getType(), orderBean.getNo(), orderBean.getMoney(), orderBean.getMark(), orderBean.getDt());
			}
			long currentTimeMillis=System.currentTimeMillis()/1000;
			long currentTimeMillis2=Long.parseLong(PayHelperUtils.getcurrentTimeMillis(context));
			long currentTimeMillis3=currentTimeMillis-currentTimeMillis2;
			if(currentTimeMillis3>120 && currentTimeMillis2!=0){
				PayHelperUtils.sendmsg(context, "轮询任务出现异常,重启中...");
				PayHelperUtils.startAlipayMonitor(context);
				PayHelperUtils.sendmsg(context, "轮询任务重启成功");
			}
		} catch (Exception e) {
			PayHelperUtils.sendmsg(context, "AlarmReceiver异常->>"+e.getMessage());
		}
	}

}
