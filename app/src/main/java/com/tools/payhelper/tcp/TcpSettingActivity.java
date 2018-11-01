package com.tools.payhelper.tcp;

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

import com.tools.payhelper.R;
import com.tools.payhelper.utils.AbSharedUtil;


public class TcpSettingActivity extends Activity implements OnClickListener {

    private EditText et_ip, et_port, et_verify;
    private Button bt_save, bt_back;
    private RelativeLayout rl_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_tcp_setting);
        et_ip = (EditText) findViewById(R.id.et_ip);
        et_port = (EditText) findViewById(R.id.et_port);
        et_verify = (EditText) findViewById(R.id.et_verify);
        if (!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "tcp_ip"))) {
            et_ip.setText(AbSharedUtil.getString(getApplicationContext(), "tcp_ip"));
        }
        if (AbSharedUtil.getInt(getApplicationContext(), "tcp_port") != 0) {
            et_port.setText("" + AbSharedUtil.getInt(getApplicationContext(), "tcp_port"));
        }
        if (!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "tcp_verify"))) {
            et_verify.setText("" + AbSharedUtil.getString(getApplicationContext(), "tcp_verify"));
        }

        bt_save = (Button) findViewById(R.id.save);
        bt_back = (Button) findViewById(R.id.back);
        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
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
                String tcp_ip = et_ip.getText().toString();
                if (TextUtils.isEmpty(tcp_ip)) {
                    Toast.makeText(getApplicationContext(), "IP不能为空！", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    AbSharedUtil.putString(getApplicationContext(), "tcp_ip", tcp_ip);
                }
                String tcp_port = et_port.getText().toString();
                if (TextUtils.isEmpty(tcp_port)) {
                    Toast.makeText(getApplicationContext(), "PORT不能为空！", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    AbSharedUtil.putInt(getApplicationContext(), "tcp_port", Integer.valueOf(tcp_port));
                }
                String tcp_verify = et_verify.getText().toString();
                if (TextUtils.isEmpty(tcp_verify)) {
                    Toast.makeText(getApplicationContext(), "认证信息不能为空！", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    AbSharedUtil.putString(getApplicationContext(), "tcp_verify", tcp_verify);
                }
                Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
                finish();
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
