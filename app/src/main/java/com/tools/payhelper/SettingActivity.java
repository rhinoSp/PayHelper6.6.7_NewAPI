package com.tools.payhelper;

import com.tools.payhelper.utils.AbSharedUtil;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * 

* @ClassName: SettingActivity

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:26:51

*
 */
public class SettingActivity extends Activity implements OnClickListener{
	
	private EditText et_returnurl,et_notifyurl,et_signkey,et_wxid;
	private Button bt_save,bt_back;
	private RelativeLayout rl_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.activity_setting);
		et_returnurl=(EditText) findViewById(R.id.returnurl);
		et_notifyurl=(EditText) findViewById(R.id.notifyurl);
		et_signkey=(EditText) findViewById(R.id.signkey);
		et_wxid=(EditText) findViewById(R.id.et_wxid);
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "returnurl"))){
			et_returnurl.setText(AbSharedUtil.getString(getApplicationContext(), "returnurl"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "notifyurl"))){
			et_notifyurl.setText(AbSharedUtil.getString(getApplicationContext(), "notifyurl"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "signkey"))){
			et_signkey.setText(AbSharedUtil.getString(getApplicationContext(), "signkey"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "account"))){
			et_wxid.setText(AbSharedUtil.getString(getApplicationContext(), "account"));
		}
		
		bt_save=(Button) findViewById(R.id.save);
		bt_back=(Button) findViewById(R.id.back);
		rl_back=(RelativeLayout) findViewById(R.id.rl_back);
		bt_back.setOnClickListener(this);
		bt_save.setOnClickListener(this);
		rl_back.setOnClickListener(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			String returnurl=et_returnurl.getText().toString();
			if(TextUtils.isEmpty(returnurl)){
				Toast.makeText(getApplicationContext(), "同步跳转地址不能为空！", Toast.LENGTH_LONG).show();
				return;
			}else{
				AbSharedUtil.putString(getApplicationContext(), "returnurl", returnurl);
			}
			String notifyurl=et_notifyurl.getText().toString();
			if(TextUtils.isEmpty(notifyurl)){
				Toast.makeText(getApplicationContext(), "异步通知地址不能为空！", Toast.LENGTH_LONG).show();
				return;
			}else{
				AbSharedUtil.putString(getApplicationContext(), "notifyurl", notifyurl);
			}
			String signkey=et_signkey.getText().toString();
			if(TextUtils.isEmpty(signkey)){
				Toast.makeText(getApplicationContext(), "signkey不能为空！", Toast.LENGTH_LONG).show();
				return;
			}else{
				AbSharedUtil.putString(getApplicationContext(), "signkey", signkey);
			}
			String wxid=et_wxid.getText().toString();
			if(!TextUtils.isEmpty(wxid)){
				AbSharedUtil.putString(getApplicationContext(), "account", wxid);
			}
			Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
			break;
		case R.id.back:
			finish();
			break;
		case R.id.rl_back:
			finish();
			break;
		default:
			break;
		}
	}
}
