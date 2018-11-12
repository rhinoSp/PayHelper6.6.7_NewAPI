package com.tools.payhelper.tcp;

import com.tools.payhelper.utils.JsonHelper;

/**
 * @since Create on 2018/11/8.
 */
public class VerifyData {

    public static final int TYPE_Frist = 9; //第一次验证
    public static final int TYPE_Ping = 0; //心跳 20s/次
    public static final int TYPE_KeyOK = 1; //验证成功
    public static final int TYPE_KeyNO = 2;  //验证失败
    public static final int TYPE_ResOK = 3; //获取数据成功
    public static final int TYPE_ResNO = 4;  //获取数据失败
    public static final int TYPE_Interactive = 5; // 交互
    public static final int TYPE_SuccTransaction = 6; // 付款的订单


    /**
     * 服务器-》  首次验证key 返回 Type
     * 客户端-》  验证Type 状态
     */
    public int type;
    public int restype;
    public String res;
    /**
     * 例：服务器-》  InOutData:{ money:1000,mark:k18dsfsd0,type:alipay}
     * 客户端-》   InOutData: {data:{key:asdasda}}  等待服务器的数据，并发送心跳IntoData:{ping:0}
     */
    public InOut InOutData;

    public static class InOut {
        public String key;
        public String data;

        public static InOut verify(String verify) {
            InOut d = new InOut();
            d.key = verify;
            return d;
        }
        public static InOut heartBeat() {
            InOut d = new InOut();
            d.data = "0";
            return d;
        }
        public static InOut pay(String data) {
            InOut d = new InOut();
            d.data = data;
            return d;
        }
    }

    public static class PayResult {
        public String no;
        public String money;
        public String mark;
        public String type;
        public String dt;
        public String account;
        public String sign;

        public PayResult(String no, String money, String mark, String type, String dt, String account, String sign) {
            this.no = no;
            this.money = money;
            this.mark = mark;
            this.type = type;
            this.dt = dt;
            this.account = account;
            this.sign = sign;
        }
    }

    public static class PayCodeData {
        public String msg;
        public String payurl;
        public String mark;
        public String money;
        public String account;
    }

    public static VerifyData createVerifyData(String verify) {
        VerifyData data = new VerifyData();
        data.type = TYPE_Frist;
        data.InOutData = InOut.verify(verify);
        return data;
    }

    public static VerifyData createHeartBeatData() {
        VerifyData data = new VerifyData();
        data.type = TYPE_Ping;
        data.InOutData = InOut.heartBeat();
        return data;
    }

    public static VerifyData createPayData(String payData) {
        VerifyData data = new VerifyData();
        data.type = TYPE_Interactive;
        data.InOutData = InOut.pay(payData);
        return data;
    }

    public static VerifyData createPayResultData(String no, String money, String mark, String type, String dt, String account, String sign) {
        VerifyData data = new VerifyData();
        data.type = TYPE_SuccTransaction;
        data.InOutData = InOut.pay(JsonHelper.toJson(new PayResult(no, money, mark, type, dt, account, sign)));
        return data;
    }


}
