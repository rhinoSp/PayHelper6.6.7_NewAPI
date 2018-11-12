package com.tools.payhelper.tcp;

import com.tools.payhelper.utils.JsonHelper;
import com.tools.payhelper.utils.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class TcpConnection extends Thread {

    // auto.1899pay.com  8011

    private Socket mSocket;
    private BufferedReader mBufferedReader = null;
    private PrintWriter mPrintWriter = null;

    private String mIpAddress;
    private int mIpPort;
    private String mVerify;

    private OnTcpResultListener mOnTcpResultListener;
    private long mLastHeartBeatTimestamp;
    private HeartBeatThread mHeartBeatThread;

    private static TcpConnection instance = null;
    public static TcpConnection getInstance() {
        if (instance == null) {
            instance = new TcpConnection();
        }
        return instance;
    }

    public void init(String address, int port, String verify) {
        this.mIpAddress = address;
        this.mIpPort = port;
        this.mVerify = verify;
        this.mLastHeartBeatTimestamp = System.currentTimeMillis();
    }

    @Override
    public void run() {
        super.run();
        connect();
        try {
            while (mSocket != null && !mSocket.isClosed() && mSocket.isConnected()) {
                if (!mSocket.isInputShutdown()) {
                    try {
                        InputStream inputStream = mSocket.getInputStream();
                        DataInputStream input = new DataInputStream(inputStream);
                        byte[] b = new byte[2 * 1024];
                        int length = 0;
                        while ((length = input.read(b)) != -1) {
                            String msg = new String(b, 0, length, "utf-8").replace("\0", "");
                            LogUtils.d("length = " + length + ", msg = " + msg);
                            if (!msg.isEmpty() && mOnTcpResultListener != null) {
                                mOnTcpResultListener.onReceive(msg);
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
            mOnTcpResultListener.onFailed(e.toString());
        }
    }

    private void connect() {
        try {
            mSocket = new Socket(mIpAddress, mIpPort);
            mSocket.setSoTimeout(3000);
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket
                    .getInputStream()));
            mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    mSocket.getOutputStream())), true);
            LogUtils.i("Connect success");
            if (mOnTcpResultListener != null) {
                mOnTcpResultListener.onConnected();
            }

            // verify
            String data = JsonHelper.toJson(VerifyData.createVerifyData(mVerify));
            send(data);
            LogUtils.i("Send verify success, " + data);

            // heart beat
            mHeartBeatThread = new HeartBeatThread();
            mHeartBeatThread.start();
        } catch (IOException e) {
            LogUtils.e(e.toString());
            mOnTcpResultListener.onFailed(e.toString());
            e.printStackTrace();
        }
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeReader(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeWriter(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeSocket(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnTcpResultListener {

        void onConnected();

        void onReceive(String data);

        void onFailed(String error);
    }

    public OnTcpResultListener getOnTcpResultListener() {
        return mOnTcpResultListener;
    }

    public void setOnTcpResultListener(OnTcpResultListener listener) {
        this.mOnTcpResultListener = listener;
    }

    public void send(String msg) {
        LogUtils.d("msg: " + msg);
        if (mPrintWriter != null) {
            mPrintWriter.write(msg);
            mPrintWriter.flush();
        }
    }

    public void close() {
        LogUtils.i("close");
        closeReader(mBufferedReader);
        closeWriter(mPrintWriter);
        closeSocket(mSocket);
        if (mHeartBeatThread != null) {
            mHeartBeatThread.interrupt();
            mHeartBeatThread = null;
        }
        instance = null;
    }

    private class HeartBeatThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (mSocket != null && !mSocket.isClosed() && mSocket.isConnected()) {
                long currentTimestamp = System.currentTimeMillis();
                if (mLastHeartBeatTimestamp + 20 * 1000 < currentTimestamp ) {
                    String data = JsonHelper.toJson(VerifyData.createHeartBeatData());
                    send(data);
                    LogUtils.d("Heart beat, " + data);
                    mLastHeartBeatTimestamp = currentTimestamp;
                }
            }
        }
    }

}