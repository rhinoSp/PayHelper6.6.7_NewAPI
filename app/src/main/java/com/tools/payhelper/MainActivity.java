package com.tools.payhelper;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.tools.payhelper.http.HttpRequest;
import com.tools.payhelper.tcp.TcpConnection;
import com.tools.payhelper.tcp.TcpSettingActivity;
import com.tools.payhelper.tcp.VerifyData;
import com.tools.payhelper.utils.AbSharedUtil;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.JsonHelper;
import com.tools.payhelper.utils.LogToFile;
import com.tools.payhelper.utils.LogUtils;
import com.tools.payhelper.utils.MD5;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.QrCodeBean;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author SuXiaoliang
 * @ClassName: MainActivity
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2018年6月23日 下午1:26:32
 */
public class MainActivity extends Activity implements TcpConnection.OnTcpResultListener{

    public static final String QQ = "/getpay?money=0.1&mark=k123467789&type=qq";
    public static final String ALIPAY = "/getpay?money=0.1&mark=k123467789&type=alipay";

    public static TextView console;
    private static ScrollView scrollView;
    private BillReceived billReceived;
    private AlarmReceiver alarmReceiver;
    public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
    public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";
    public static String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
    public static String TRADENORECEIVED_ACTION = "com.tools.payhelper.tradenoreceived";
    public static String LOGINIDRECEIVED_ACTION = "com.tools.payhelper.loginidreceived";
    public static String NOTIFY_ACTION = "com.tools.payhelper.notify";
    public static String SAVEALIPAYCOOKIE_ACTION = "com.tools.payhelper.savealipaycookie";
    public static int WEBSEERVER_PORT = 8080;
    private WebServer mVideoServer;

    private String currentWechat = "";
    private String currentAlipay = "";
    private String currentQQ = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        console = (TextView) findViewById(R.id.console);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        try {
            mVideoServer = new WebServer(this, WEBSEERVER_PORT);
            mVideoServer.start();
            sendmsg("web服务器启动成功，端口:" + WEBSEERVER_PORT);
        } catch (Exception e) {
            sendmsg("web服务器启动失败，错误:" + e.getMessage());
        }
        this.findViewById(R.id.start_alipay).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent broadCastIntent = new Intent();
                        broadCastIntent.setAction("com.payhelper.alipay.start");
                        String time=System.currentTimeMillis()/10000L+"";
                        broadCastIntent.putExtra("mark", "test"+time);
                        broadCastIntent.putExtra("money", "0.01");
                        sendBroadcast(broadCastIntent);
                    }
                });
        this.findViewById(R.id.start_wechat).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent broadCastIntent = new Intent();
                        broadCastIntent.setAction("com.payhelper.wechat.start");
                        String time = System.currentTimeMillis() / 10000L + "";
                        broadCastIntent.putExtra("mark", "test" + time);
                        broadCastIntent.putExtra("money", "0.01");
                        sendBroadcast(broadCastIntent);
                    }
                });
        this.findViewById(R.id.start_qq).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent broadCastIntent = new Intent();
                        broadCastIntent.setAction("com.payhelper.qq.start");
                        String time = System.currentTimeMillis() / 10000L + "";
                        broadCastIntent.putExtra("mark", "test" + time);
                        broadCastIntent.putExtra("money", "0.01");
                        sendBroadcast(broadCastIntent);
                    }
                });
        this.findViewById(R.id.setting).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent);
                    }
                });
        this.findViewById(R.id.tcp_connect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        String text = ((TextView)findViewById(R.id.tcp_connect)).getText().toString();
                        if ("断开连接".equals(text)) {
                            PayHelperUtils.sendmsg(getApplicationContext(), "断开连接");
                            ((TextView)findViewById(R.id.tcp_connect)).setText("TCP连接");
                            TcpConnection.getInstance().close();
                        } else {
                            String ip = AbSharedUtil.getString(getApplicationContext(), "tcp_ip");
                            int port = AbSharedUtil.getInt(getApplicationContext(), "tcp_port");
                            String verify = AbSharedUtil.getString(getApplicationContext(), "tcp_verify");
                            if (TextUtils.isEmpty(ip) || port == 0 || TextUtils.isEmpty(verify)) {
                                Toast.makeText(getApplicationContext(), "请配置IP、端口和认证信息", Toast.LENGTH_LONG).show();
                                return;
                            }
                            TcpConnection.getInstance().close();
                            TcpConnection.getInstance().init(ip, port, verify);
                            TcpConnection.getInstance().setOnTcpResultListener(MainActivity.this);
                            TcpConnection.getInstance().start();
                        }
//                        request(ALIPAY);
                    }
                });
        this.findViewById(R.id.tcp_setting).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(MainActivity.this, TcpSettingActivity.class);
                        startActivity(intent);


                    }
                });
        //注册广播
        billReceived = new BillReceived();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BILLRECEIVED_ACTION);
        intentFilter.addAction(MSGRECEIVED_ACTION);
        intentFilter.addAction(QRCODERECEIVED_ACTION);
        intentFilter.addAction(TRADENORECEIVED_ACTION);
        intentFilter.addAction(LOGINIDRECEIVED_ACTION);
        intentFilter.addAction(SAVEALIPAYCOOKIE_ACTION);
        registerReceiver(billReceived, intentFilter);

        alarmReceiver = new AlarmReceiver();
        IntentFilter alarmIntentFilter = new IntentFilter();
        alarmIntentFilter.addAction(NOTIFY_ACTION);
        registerReceiver(alarmReceiver, alarmIntentFilter);
        startService(new Intent(this, DaemonService.class));

        PayHelperUtils.startAlipayMonitor(this);
        sendmsg("当前软件版本:" + PayHelperUtils.getVerName(getApplicationContext()));
    }

    public static Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String txt = msg.getData().getString("log");
            if (console != null) {
                if (console.getText() != null) {
                    if (console.getText().toString().length() > 7500) {
                        console.setText("日志定时清理完成..." + "\n\n" + txt);
                    } else {
                        console.setText(console.getText().toString() + "\n\n" + txt);
                    }
                } else {
                    console.setText(txt);
                }
                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onDestroy() {
        TcpConnection.getInstance().close();
        unregisterReceiver(alarmReceiver);
        unregisterReceiver(billReceived);
        mVideoServer.stop();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public static void sendmsg(String txt) {
        LogToFile.i("payhelper", txt);
        Message msg = new Message();
        msg.what = 1;
        Bundle data = new Bundle();
        long l = System.currentTimeMillis();
        Date date = new Date(l);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = dateFormat.format(date);
        data.putString("log", d + ":" + " " + txt);
        msg.setData(data);
        try {
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 过滤按键动作
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConnected() {
        LogUtils.d("onConnected");
    }

    @Override
    public void onReceive(String data) {
        VerifyData verifyData = JsonHelper.fromJson(data, VerifyData.class);
        if (verifyData == null) {
            return;
        }
        switch (verifyData.type) {
            case VerifyData.TYPE_KeyOK:
                PayHelperUtils.sendmsg(getApplicationContext(), "连接成功：" + data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.tcp_connect)).setText("断开连接");
                        Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case VerifyData.TYPE_KeyNO:
                PayHelperUtils.sendmsg(getApplicationContext(), "连接失败：" + data);
                TcpConnection.getInstance().close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "连接失败：" + verifyData.res, Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case VerifyData.TYPE_Interactive:
                PayHelperUtils.sendmsg(getApplicationContext(), "请求二维码：" + data);
                if (verifyData.InOutData == null || TextUtils.isEmpty(verifyData.InOutData.data)) {
                    return;
                }
                request(verifyData.InOutData.data);
                break;
            default:
                break;
        }
    }

    private void request(String url) {
        new HttpRequest().doGet("http://127.0.0.1:8080" + url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                LogUtils.d("data = " + data);
                PayHelperUtils.sendmsg(getApplicationContext(), "二维码请求结果：" + data);

                TcpConnection.getInstance().send(JsonHelper.toJson(VerifyData.createPayData(data)));
            }
        });
    }

    @Override
    public void onFailed(String error) {
        LogUtils.e("error: " + error);
    }

    //自定义接受订单通知广播
    class BillReceived extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            try {
                if (intent.getAction().contentEquals(BILLRECEIVED_ACTION)) {
                    String no = intent.getStringExtra("bill_no");
                    String money = intent.getStringExtra("bill_money");
                    String mark = intent.getStringExtra("bill_mark");
                    String type = intent.getStringExtra("bill_type");

                    DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
                    String dt = System.currentTimeMillis() + "";
                    dbManager.addOrder(new OrderBean(money, mark, type, no, dt, "", 0));

                    String typestr = "";
                    if (type.equals("alipay")) {
                        typestr = "支付宝";
                    } else if (type.equals("wechat")) {
                        typestr = "微信";
                    } else if (type.equals("qq")) {
                        typestr = "QQ";
                    } else if (type.equals("alipay_dy")) {
                        typestr = "支付宝店员";
                        dt = intent.getStringExtra("time");
                    }
                    sendmsg("收到" + typestr + "订单,订单号：" + no + "金额：" + money + "备注：" + mark);
                    notifyapi(type, no, money, mark, dt);
                    TcpConnection.getInstance().send(JsonHelper.toJson(VerifyData.createPayResultData(no, money, mark, type)));
                } else if (intent.getAction().contentEquals(QRCODERECEIVED_ACTION)) {
                    String money = intent.getStringExtra("money");
                    String mark = intent.getStringExtra("mark");
                    String type = intent.getStringExtra("type");
                    String payurl = intent.getStringExtra("payurl");
                    DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
                    String dt = System.currentTimeMillis() + "";
                    DecimalFormat df = new DecimalFormat("0.00");
                    money = df.format(Double.parseDouble(money));
                    dbManager.addQrCode(new QrCodeBean(money, mark, type, payurl, dt));
                    sendmsg("生成成功,金额:" + money + "备注:" + mark + "二维码:" + payurl);
                } else if (intent.getAction().contentEquals(MSGRECEIVED_ACTION)) {
                    String msg = intent.getStringExtra("msg");
                    sendmsg(msg);
                } else if (intent.getAction().contentEquals(SAVEALIPAYCOOKIE_ACTION)) {
                    String cookie = intent.getStringExtra("alipaycookie");
                    PayHelperUtils.updateAlipayCookie(MainActivity.this, cookie);
                } else if (intent.getAction().contentEquals(LOGINIDRECEIVED_ACTION)) {
                    String loginid = intent.getStringExtra("loginid");
                    String type = intent.getStringExtra("type");
                    if (!TextUtils.isEmpty(loginid)) {
                        if (type.equals("wechat") && !loginid.equals(currentWechat)) {
                            sendmsg("当前登录微信账号：" + loginid);
                            currentWechat = loginid;
                            AbSharedUtil.putString(getApplicationContext(), type, loginid);
                        } else if (type.equals("alipay") && !loginid.equals(currentAlipay)) {
                            sendmsg("当前登录支付宝账号：" + loginid);
                            currentAlipay = loginid;
                            AbSharedUtil.putString(getApplicationContext(), type, loginid);
                        } else if (type.equals("qq") && !loginid.equals(currentQQ)) {
                            sendmsg("当前登QQ账号：" + loginid);
                            currentQQ = loginid;
                            AbSharedUtil.putString(getApplicationContext(), type, loginid);
                        }
                    }
                } else if (intent.getAction().contentEquals(TRADENORECEIVED_ACTION)) {
                    //商家服务
                    final String tradeno = intent.getStringExtra("tradeno");
                    String cookie = intent.getStringExtra("cookie");
                    final DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
                    if (!dbManager.isExistTradeNo(tradeno)) {
                        dbManager.addTradeNo(tradeno, "0");
                        String url = "https://tradeeportlet.alipay.com/wireless/tradeDetail.htm?tradeNo=" + tradeno + "&source=channel&_from_url=https%3A%2F%2Frender.alipay.com%2Fp%2Fz%2Fmerchant-mgnt%2Fsimple-order._h_t_m_l_%3Fsource%3Dmdb_card";
                        try {
                            HttpUtils httpUtils = new HttpUtils(15000);
                            httpUtils.configResponseTextCharset("GBK");
                            RequestParams params = new RequestParams();
                            params.addHeader("Cookie", cookie);

                            httpUtils.send(HttpMethod.GET, url, params, new RequestCallBack<String>() {

                                @Override
                                public void onFailure(HttpException arg0, String arg1) {
                                    PayHelperUtils.sendmsg(context, "服务器异常" + arg1);
                                }

                                @Override
                                public void onSuccess(ResponseInfo<String> arg0) {
                                    try {
                                        String result = arg0.result;
                                        Document document = Jsoup.parse(result);
                                        Elements elements = document.getElementsByClass("trade-info-value");
                                        if (elements.size() >= 5) {
                                            dbManager.updateTradeNo(tradeno, "1");
                                            String money = document.getElementsByClass("amount").get(0).ownText().replace("+", "").replace("-", "");
                                            String mark = elements.get(3).ownText();
                                            String dt = System.currentTimeMillis() + "";
                                            dbManager.addOrder(new OrderBean(money, mark, "alipay", tradeno, dt, "", 0));
                                            sendmsg("收到支付宝订单,订单号：" + tradeno + "金额：" + money + "备注：" + mark);
                                            notifyapi("alipay", tradeno, money, mark, dt);
                                        }
                                    } catch (Exception e) {
                                        PayHelperUtils.sendmsg(context, "TRADENORECEIVED_ACTION-->>onSuccess异常" + e.getMessage());
                                    }
                                }
                            });
                        } catch (Exception e) {
                            PayHelperUtils.sendmsg(context, "TRADENORECEIVED_ACTION异常" + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                PayHelperUtils.sendmsg(context, "BillReceived异常" + e.getMessage());
            }
        }

        public void notifyapi(String type, final String no, String money, String mark, String dt) {
            try {
                String notifyurl = AbSharedUtil.getString(getApplicationContext(), "notifyurl");
                String signkey = AbSharedUtil.getString(getApplicationContext(), "signkey");
                if (TextUtils.isEmpty(notifyurl) || TextUtils.isEmpty(signkey)) {
                    sendmsg("发送异步通知异常，异步通知地址为空");
                    update(no, "异步通知地址为空");
                    return;
                }

                String account = "";
                if (type.equals("alipay")) {
                    account = AbSharedUtil.getString(getApplicationContext(), "alipay");
                } else if (type.equals("wechat")) {
                    account = AbSharedUtil.getString(getApplicationContext(), "wechat");
                } else if (type.equals("qq")) {
                    account = AbSharedUtil.getString(getApplicationContext(), "qq");
                }

                HttpUtils httpUtils = new HttpUtils(15000);

                String sign = MD5.md5(dt + mark + money + no + type + signkey);
                RequestParams params = new RequestParams();
                params.addBodyParameter("type", type);
                params.addBodyParameter("no", no);
                params.addBodyParameter("money", money);
                params.addBodyParameter("mark", mark);
                params.addBodyParameter("dt", dt);
                if (!TextUtils.isEmpty(account)) {
                    params.addBodyParameter("account", account);
                }
                params.addBodyParameter("sign", sign);
                httpUtils.send(HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        sendmsg("发送异步通知异常，服务器异常" + arg1);
                        update(no, arg1);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        String result = arg0.result;
                        if (result.contains("success")) {
                            sendmsg("发送异步通知成功，服务器返回" + result);
                        } else {
                            sendmsg("发送异步通知失败，服务器返回" + result);
                        }
                        update(no, result);
                    }
                });
            } catch (Exception e) {
                sendmsg("notifyapi异常" + e.getMessage());
            }
        }

        private void update(String no, String result) {
            DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
            dbManager.updateOrder(no, result);
        }
    }
}
