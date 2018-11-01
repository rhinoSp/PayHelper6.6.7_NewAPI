package com.tools.payhelper.tcp;

import com.tools.payhelper.utils.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;


public class TcpConnection extends Thread {

    private static final String TAG = TcpConnection.class.getSimpleName();

    private Socket mSocket;
//    private InputStream mInputStream;
//    private DataInputStream mDataInputStream;
    private BufferedReader mBufferedReader = null;
    private PrintWriter mPrintWriter = null;

    private String mIpAddress;
    private int mIpPort;
    private String mVerify;

    private OnTcpResultListener mOnTcpResultListener;

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
    }

    @Override
    public void run() {
        super.run();
        connect();
        try {
            while (mSocket != null) {
                if (mSocket.isClosed()) {
                    LogUtils.i("Server closed.");
                    return;
                }
                if (!mSocket.isConnected()) {
                    LogUtils.i("Not connect.");
                    return;
                }
                if (!mSocket.isInputShutdown()) {
                    LogUtils.i("The input is shutdown.");
                    return;
                }
                String line;
                if ((line = mBufferedReader.readLine()) != null) {
                    LogUtils.d(line);
                    line += "\n";
                    if (mOnTcpResultListener != null) {
                        mOnTcpResultListener.onReceive(line);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
            mOnTcpResultListener.onFailed(e.toString());
        }

//        while (true) {
//            if (mSocket.isConnected() && !mSocket.isClosed()) {
//                try {
//                    mInputStream = mSocket.getInputStream();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                LogUtils.e("连接成功");
//                mDataInputStream = new DataInputStream(mInputStream);
//                ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mDataInputStream == null) {
//                            return;
//                        }
//                        final byte[] bytes = new byte[2];
//                        int len = 0;
//                        try {
//                            while ((len = mDataInputStream.read(bytes)) != -1) {
//                                int value1 = bytes[0] & 0xff;
//                                int value2 = bytes[1] & 0xff;
//                                int iii = (value2 & 0xff) << 8 | ((value1 & 0xff));
//                                LogUtils.e("len = " + len + "  " + value1 + " ** " + value2 + "  " + "  " + iii);
//                                if (mOnTcpResultListener != null) {
//                                    mOnTcpResultListener.onSuccess(iii);
//                                }
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            mOnTcpResultListener.onFailed(e.toString());
//                            LogUtils.e(e.toString());
//                        }
//                    }
//                });
//            }
//        }
    }

    private void connect() {
        try {
            mSocket = new Socket(mIpAddress, mIpPort);
            mSocket.setSoTimeout(20000);
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket
                    .getInputStream()));
            mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    mSocket.getOutputStream())), true);
            LogUtils.i("Connect success");
            if (mOnTcpResultListener != null) {
                mOnTcpResultListener.onConnected();
            }

            // verify
            send(mVerify);
            LogUtils.i("Send verify success");
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
        instance = null;
    }

}