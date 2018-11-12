package com.tools.payhelper.http.result;


import java.io.Serializable;

/**
 * <p>返回数据基类</p>
 */
public class BaseResult implements Serializable {

    public Error error;
    public String value;

    @Override
    public String toString() {
        return "BaseResult{" +
                "error=" + error +
                ", value='" + value + '\'' +
                '}';
    }

    public static class Error {
        public String errorCode;
        public String message;
        public String errorType;
        @Override
        public String toString() {
            return "Error{" +
                    "errorCode='" + errorCode + '\'' +
                    ", message='" + message + '\'' +
                    ", errorType='" + errorType + '\'' +
                    '}';
        }
    }
}