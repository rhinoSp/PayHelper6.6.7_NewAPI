package com.tools.payhelper.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.telephony.TelephonyManager;

public class StringUtils {

	public static void main(String[] args) {
	}

	public static byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	public static void getFile(byte[] bfile, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists() && !dir.isDirectory()) {// �ж��ļ�Ŀ¼�Ƿ����?
				dir.mkdirs();
			}
			file = new File(filePath + "\\" + fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static String getimei(Context context){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		String imei=tm.getDeviceId();
		if(imei==null){
			return "00000000000000000";
		}else{
			return tm.getDeviceId();
		}
	}
	public static String getTextCenter(String text,String begin,String end){
		try {
			int b=text.indexOf(begin)+begin.length();
			int e=text.indexOf(end,b);
			return text.substring(b,e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
}