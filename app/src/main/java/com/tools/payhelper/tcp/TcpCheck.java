package com.tools.payhelper.tcp;

/**
 * @author LuoLin
 * @since Create on 2018/10/29.
 */
public class TcpCheck {

    public Verify data;

    public static class Verify {
        public String key;
        public Verify(String key) {
            this.key = key;
        }
    }

}
