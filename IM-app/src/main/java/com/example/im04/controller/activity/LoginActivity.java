package com.example.im04.controller.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.im04.R;
import com.example.im04.model.Model;
import com.example.im04.model.bean.UserInfo;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

//@Route(path = "/IM/SplashAct")
//  登录页面
public class LoginActivity extends Activity {
    private EditText et_login_name;
    private EditText et_login_pwd;
    private Button btn_login_register;
    private Button btn_login_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        initView();

        // 初始化监听
        initListener();
    }

    private void initListener() {
        // 注册按钮的点击事件处理。
        btn_login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registe();
            }
        });

        //  登录按钮的点击事件处理
        btn_login_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    // 登录按钮的业务逻辑处理
    private void login() {
        // 1.获取输入的用户名和密码
        String loginName = et_login_name.getText().toString();
        String loginPwd = et_login_pwd.getText().toString();
        // 2.校验输入的用户名和密码
        if (TextUtils.isEmpty(loginName) || TextUtils.isEmpty(loginPwd)){
            Toast.makeText(LoginActivity.this , "输入的用户名或密码不能为空" , Toast.LENGTH_LONG).show();
            return;
        }

        //  3.登录逻辑处理（从服务器那边去判断，是联网操作，需要开启线程）
        Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // 去环信服务器登录
                EMClient.getInstance().login(loginName, loginPwd, new EMCallBack() {
                    // 登录成功后的处理
                    @Override
                    public void onSuccess() {
                        //  对模型层数据的处理
                        Model.getInstance().loginSuccess(new UserInfo(loginName));//loginSuccess()是预留的一个方法，用户登录成功后需要进行的一些更改。

                        //  保存用户账号信息到本地数据库。
                        Model.getInstance().getUserAccountDao().addAccount(new UserInfo(loginName));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //  提示登录成功
                                Toast.makeText(LoginActivity.this ,"登录成功" , Toast.LENGTH_LONG).show();

                                //  跳转到主界面
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();   // 登录进去后这个页面就不需要了，直接销毁掉就好了。
                            }
                        });

                    }

                    // 登录失败的处理
                    @Override
                    public void onError(int code, String error) {// error是失败的原因，相当于之前的e。
                        // 提示登陆失败
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this , "登录失败" +error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    // 登录过程中的处理
                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        });
    }

    // 注册的业务逻辑处理
    private void registe() {
        // 1.获取输入的用户名和密码
        String registName = et_login_name.getText().toString();
        String registPwd = et_login_pwd.getText().toString();
        // 2.校验输入的用户名和密码
        if (TextUtils.isEmpty(registName) || TextUtils.isEmpty(registPwd)){
            Toast.makeText(LoginActivity.this , "输入的用户名或密码不能为空" , Toast.LENGTH_LONG).show();
            return;
        }

        // 3.去服务器注册账号
        Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 去环信服务器注册账号
                    EMClient.getInstance().createAccount(registName , registPwd);

                    // 更新页面显示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this , "注册成功" , Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this , "注册失败"+e.toString() , Toast.LENGTH_LONG).show();
                            Log.e("出现的错误", e.toString() );
                        }
                    });
                }
            }
        });
    }

    private void initView() {
        et_login_name = findViewById(R.id.et_login_name);
        et_login_pwd = findViewById(R.id.et_login_pwd);
        btn_login_register = findViewById(R.id.btn_login_register);
        btn_login_login = findViewById(R.id.btn_login_login);
    }
}
