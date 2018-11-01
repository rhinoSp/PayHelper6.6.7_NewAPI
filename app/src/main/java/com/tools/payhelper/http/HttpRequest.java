package com.tools.payhelper.http;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tools.payhelper.http.request.HttpParams;
import com.tools.payhelper.utils.LogUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * <p>HTTP请求工具类</p>
 *
 * @author rhino
 * @since Create on 2018/8/20.
 */
public class HttpRequest {

    private static final String TAG = HttpRequest.class.getSimpleName();

    /**
     * 默认超时时间 （秒）
     **/
    private static final int DF_TIME = 60;

    private String userKey;
    private OkHttpClient mOkHttpClient;

    public HttpRequest() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DF_TIME, TimeUnit.SECONDS)
                .readTimeout(DF_TIME, TimeUnit.SECONDS)
                .writeTimeout(DF_TIME, TimeUnit.SECONDS)
                .cookieJar(new CookieJarManager())
                .build();
    }

    /**
     * 设置userKey
     *
     * @param userKey 从服务器获取
     */
    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    /**
     * POST请求
     *
     * @param url      请求url
     * @param callback 回调
     */
    public void doPost(String url, Callback callback) {
        doPost(url, null, callback);
    }

    /**
     * POST请求
     *
     * @param url      请求url
     * @param params   请求参数
     * @param callback 回调
     */
    public void doPost(String url, HttpParams params, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        FormBody.Builder bodyBuilder = new FormBody.Builder();

        StringBuilder httpUrl = new StringBuilder(url);
        // 是否带有参数
        if (params != null) {
            // 反射得到参数对象
            Class<? extends HttpParams> clazz = params.getClass();
            // 获取参数对象所有属性
            Field fields[] = clazz.getDeclaredFields();
            httpUrl.append("?");
            for (Field field : fields) {
                // 突破private属性
                field.setAccessible(true);
                // 获取该字段的注解
                ParamField json = field.getAnnotation(ParamField.class);
                if (json != null && !TextUtils.isEmpty(json.value())) {
                    try {
                        httpUrl.append(json.value() + "="
                                + String.valueOf(field.get(params)) + "&");
                        bodyBuilder.add(json.value(), String.valueOf(field.get(params)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(userKey)) {
            if (httpUrl.toString().equals(url)) {
                httpUrl.append("?");
            }
            httpUrl.append("userKey=" + userKey + "&");
            bodyBuilder.add("userKey", userKey);
        }

        if (httpUrl.toString().endsWith("&")) {
            LogUtils.i(TAG, "httpUrl = " + httpUrl.delete(httpUrl.length() - 1, httpUrl.length()).toString());
        } else {
            LogUtils.i(TAG, "httpUrl = " + httpUrl.toString());
        }

        Request mRequest = requestBuilder.post(bodyBuilder.build()).build();
        mOkHttpClient.newCall(mRequest).enqueue(callback);
    }

    /**
     * POST请求
     *
     * @param url       请求url
     * @param paramsMap 请求参数
     * @param callback  回调
     */
    public void doPostByMap(String url, Map<String, String> paramsMap, Callback callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        FormBody.Builder bodyBuilder = new FormBody.Builder();

        StringBuilder httpUrl = new StringBuilder(url);
        if (paramsMap != null) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                if (httpUrl.toString().equals(url)) {
                    httpUrl.append("?");
                }
                httpUrl.append(entry.getKey() + "=" + entry.getValue() + "&");
                bodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        if (!TextUtils.isEmpty(userKey)) {
            if (httpUrl.toString().equals(url)) {
                httpUrl.append("?");
            }
            httpUrl.append("userKey=" + userKey + "&");
            bodyBuilder.add("userKey", userKey);
        }

        if (httpUrl.toString().endsWith("&")) {
            LogUtils.i(TAG, "httpUrl = " + httpUrl.delete(httpUrl.length() - 1, httpUrl.length()).toString());
        } else {
            LogUtils.i(TAG, "httpUrl = " + httpUrl.toString());
        }

        Request mRequest = requestBuilder.post(bodyBuilder.build()).build();
        mOkHttpClient.newCall(mRequest).enqueue(callback);
    }

    /**
     * Get请求
     *
     * @param url      请求url
     * @param callback 回调
     */
    public void doGet(String url, Callback callback) {
        doGet(url, null, callback);
    }

    /**
     * Get请求
     *
     * @param url      请求url
     * @param params   请求参数
     * @param callback 回调
     */
    public void doGet(String url, HttpParams params, Callback callback) {
        String httpUrl = buildHttpUrl(url, params, null);
        LogUtils.i(TAG, "httpUrl = " + httpUrl);

        Request.Builder requestBuilder = new Request.Builder().url(httpUrl);
        Request request = requestBuilder.build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * Get请求
     *
     * @param url       请求url
     * @param paramsMap 请求参数
     * @param callback  回调
     */
    public void doGetByMap(String url, Map<String, String> paramsMap, Callback callback) {
        String httpUrl = buildHttpUrl(url, null, paramsMap);
        LogUtils.i(TAG, "httpUrl = " + httpUrl);

        Request.Builder requestBuilder = new Request.Builder().url(httpUrl);
        Request request = requestBuilder.build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }


    /**
     * 拼接url
     *
     * @param url       请求url
     * @param params    请求参数
     * @param paramsMap 请求参数
     * @return 拼接了参数的url
     */
    private String buildHttpUrl(String url, HttpParams params, Map<String, String> paramsMap) {
        StringBuilder httpUrl = new StringBuilder(url);
        // 是否带有参数
        if (params != null) {
            // 反射得到参数对象
            Class<? extends HttpParams> clazz = params.getClass();
            // 获取参数对象所有属性
            Field fields[] = clazz.getDeclaredFields();
            httpUrl.append("?");
            for (Field field : fields) {
                // 突破private属性
                field.setAccessible(true);
                // 获取该字段的注解
                ParamField json = field.getAnnotation(ParamField.class);
                if (json != null && !TextUtils.isEmpty(json.value())) {
                    try {
                        httpUrl.append(json.value() + "="
                                + String.valueOf(field.get(params)) + "&");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            if (httpUrl.toString().equals(url)) {
                httpUrl.append("?");
            }
            httpUrl.append(entry.getKey() + "=" + entry.getValue() + "&");
        }

        if (!TextUtils.isEmpty(userKey)) {
            if (httpUrl.toString().equals(url)) {
                httpUrl.append("?");
            }
            httpUrl.append("userKey=" + userKey + "&");
        }

        if (httpUrl.toString().endsWith("&")) {
            return httpUrl.toString().substring(0, httpUrl.length() - 1);
        }

        return httpUrl.toString();
    }


    /**
     * 开始上传文件
     */
    public void uploadFile(final String url, Map<String, String> paramsMap, final File filePath, Callback callback, CallbackForFile callbackForFile) {
        String httpUrl = buildHttpUrl(url, null, paramsMap);
        LogUtils.i(TAG, "httpUrl = " + httpUrl);
        Request.Builder requestBuilder = new Request.Builder().url(httpUrl);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filePath.getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"), filePath))
                .build();
        Request request = requestBuilder.header("Authorization", "Client-ID " + UUID.randomUUID())
                .url(httpUrl)
                .post(new ProgressRequestBody(requestBody, filePath, callbackForFile)).build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 开始上传文件
     */
    public void downloadFile(final String url, final String filePath, CallbackForFile callback) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e(TAG, e.toString());
                callback.onError(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                FileOutputStream fos = null;
                byte[] buf = new byte[2048];
                int len;
                try {
                    File file = new File(filePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        LogUtils.i(TAG, "download progress = " + progress + ", total = " + total);
                        //TODO 下载中更新进度条
                        callback.onProgressChanged(0);
                    }
                    fos.flush();
                    LogUtils.i(TAG, "download success");
                    callback.onSuccess(file);
                } catch (Exception e) {
                    LogUtils.e(TAG, e.toString());
                    callback.onError(e.toString());
                } finally {
                    closeQuietly(is);
                    closeQuietly(fos);
                }
            }
        });
    }

    /**
     * 关闭流
     *
     * @param closeable 流
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable ignored) {
            }
        }
    }

    public class ProgressRequestBody extends RequestBody {

        private RequestBody mDelegate;
        private CallbackForFile callBack;
        private File mFile;

        public ProgressRequestBody(RequestBody delegate, File file, CallbackForFile listener) {
            mDelegate = delegate;
            mFile = file;
            callBack = listener;
        }

        @Override
        public MediaType contentType() {
            return mDelegate.contentType();
        }

        @Override
        public long contentLength() {
            try {
                return mDelegate.contentLength();
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            BufferedSink bufferedSink = null;
            try {
                bufferedSink = Okio.buffer(new CountingSink(sink));
                bufferedSink.timeout().timeout(120, TimeUnit.SECONDS);
                mDelegate.writeTo(bufferedSink);
                bufferedSink.flush();
                LogUtils.d(TAG, "[上传成功] " + mFile.toString() + mDelegate.toString());
                callBack.onSuccess(mFile);
            } catch (IOException e) {
                LogUtils.e(TAG, e.getMessage());
                bufferedSink.close();
                sink.close();
                throw e;
            }
        }

        protected final class CountingSink extends ForwardingSink {
            private long bytesWritten = 0;
            private int progressTemp = 0;

            public CountingSink(Sink delegate) {
                super(delegate);
            }

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                bytesWritten += byteCount;
                int progress = (int) (100F * bytesWritten / contentLength());
                if (progress > progressTemp) {
                    LogUtils.d(TAG, "[上传进度] " + progress);
                    callBack.onProgressChanged(progress);
                    progressTemp = progress;
                }
            }
        }
    }

    public interface CallbackForFile {

        void onProgressChanged(int progress);

        void onSuccess(File file);

        void onError(String error);
    }

}
