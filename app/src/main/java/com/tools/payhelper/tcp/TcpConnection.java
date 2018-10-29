package com.tools.payhelper.tcp;

import android.util.Log;

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
                    Log.i(TAG, "Server closed.");
                    return;
                }
                if (!mSocket.isConnected()) {
                    Log.i(TAG, "Not connect.");
                    return;
                }
                if (!mSocket.isInputShutdown()) {
                    Log.i(TAG, "The input is shutdown.");
                    return;
                }
                String line;
                if ((line = mBufferedReader.readLine()) != null) {
                    Log.d(TAG, line);
                    line += "\n";
                    if (mOnTcpResultListener != null) {
                        mOnTcpResultListener.onSuccess(line);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
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
//                Log.e(TAG, "连接成功");
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
//                                Log.e(TAG, "len = " + len + "  " + value1 + " ** " + value2 + "  " + "  " + iii);
//                                if (mOnTcpResultListener != null) {
//                                    mOnTcpResultListener.onSuccess(iii);
//                                }
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            mOnTcpResultListener.onFailed(e.toString());
//                            Log.e(TAG, e.toString());
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
            Log.i(TAG, "Connect success");

            // verify
            send(mVerify);
            Log.i(TAG, "Send verify success");
        } catch (IOException e) {
            Log.e(TAG, e.toString());
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
        void onSuccess(String result);

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
        Log.i(TAG, "close");
        closeReader(mBufferedReader);
        closeWriter(mPrintWriter);
        closeSocket(mSocket);
        instance = null;
    }

}