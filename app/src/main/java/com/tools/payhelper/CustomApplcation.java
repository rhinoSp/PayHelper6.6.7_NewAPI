package com.tools.payhelper;


import com.tools.payhelper.utils.CrashHandler;
import com.tools.payhelper.utils.LogUtils;

import android.app.Application;
import android.content.Context;

/**
 * @author SuXiaoliang
 * @ClassName: CustomApplcation
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2018年6月23日 下午1:26:02
 */

public class CustomApplcation extends Application {

    public static CustomApplcation mInstance;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        // 崩溃记录
        context = getApplicationContext();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(context);
        LogUtils.init(context, true, false);
        mInstance = this;
    }

    public static CustomApplcation getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return context;
    }
}
