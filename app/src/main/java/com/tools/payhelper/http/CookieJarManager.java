package com.tools.payhelper.http;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;


/**
 * <p>Cookie管理类</p>
 *
 * @author rhino
 * @since Create on 2018/8/20.
 */
public class CookieJarManager implements CookieJar {

	/**
	 * 保存cookie
	 */
	private List<Cookie> mCookieList = new ArrayList<Cookie>();
	/**
	 * 这里注册需要你保存Cookie的URL
	 */
	private String COOKIE_URL[] = new String[]{
	};
	
	/**
	 * 是否保存cookie
	 * 
	 * @param url
	 *            请求URL
	 * @return true 保存
	 */
    private boolean isSaveCookie(String url){
    	for(String cookieUrl : COOKIE_URL){
    		if(url.contains(cookieUrl)){
    			return true;
    		}
    	}
    	return false;
    }
    
	
    @Override
    public void saveFromResponse(okhttp3.HttpUrl httpUrl, List<Cookie> list) {
    	String url = httpUrl.uri().toString();
    	if(isSaveCookie(url)){
//    		mCookieList.clear(); // 这里只保存一次cookie
    		mCookieList.addAll(list);
    	}
    }

    @Override
    public List<Cookie> loadForRequest(okhttp3.HttpUrl httpUrl) {
        return mCookieList;
    }
}
