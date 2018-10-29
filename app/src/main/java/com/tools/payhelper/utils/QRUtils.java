package com.tools.payhelper.utils;

import java.util.Hashtable;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

public class QRUtils {

	/**
     * 解析二维码图片
     * @param path
     * @return
     */
    public static Result scanningImage(String path) {
    	Bitmap scanBitmap;
        if (TextUtils.isEmpty(path)) {
            return null;

        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8"); // 设置二维码内容的编码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
//        scanBitmap = BitmapFactory.decodeFile(path,options);
        scanBitmap=ImageUtils.getBitmapByFile(path);
        options.inJustDecodeBounds = false;
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;

        options.inSampleSize = sampleSize;

        scanBitmap = BitmapFactory.decodeFile(path, options);
        int[] data = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(data, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(scanBitmap.getWidth(),scanBitmap.getHeight(),data);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {
            Log.e("hxy","NotFoundException");
        }catch (ChecksumException e){
            Log.e("hxy","ChecksumException");
        } catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return result;


    }
}
