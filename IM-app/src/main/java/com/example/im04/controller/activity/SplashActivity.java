package com.example.im04.controller.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.im04.R;
import com.example.im04.model.Model;
import com.example.im04.model.bean.UserInfo;
import com.hyphenate.chat.EMClient;

@Route(path = "/IM/SplashAct")
// 欢迎界面
public class SplashActivity extends Activity {

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // 如果当前activity已经退出，那么我就不处理handler中的消息，直接销毁。
            if (isFinishing()){
                return;
            }

            // 判断进入主页面还是登录页面
            toMainOrLogin();
        }
    };

    // 判断进入主页面还是登录页面    判断是从请求服务器判断的。
    private void toMainOrLogin() {
        /*new Thread(){
            @Override
            public void run() {
                // 业务逻辑代码给放到了线程池中，方便去管理，也防止内存泄漏。
            }
        }.start();*/

        Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // 判断当前账号是否已经登录过
                if (EMClient.getInstance().isLoggedInBefore()){// 登录过

                    // 获取到当前登录用户的信息。
                    UserInfo account = Model.getInstance().getUserAccountDao().getAccountByHxId(EMClient.getInstance().getCurrentUser());
                    if (account == null){   // 如果没有数据（比如说本地数据库被删掉了），则跳转到登录界面。
                        Intent intent = new Intent(SplashActivity.this , LoginActivity.class);
                        startActivity(intent);
                    }else {
                        // 登录成功后的方法
                        Model.getInstance().loginSuccess(account);

                        // 跳转到主页面
                        Intent intent = new Intent(SplashActivity.this , MainActivity.class);
                        startActivity(intent);
                    }
                }else {// 没登录过
                    // 跳转到登录页面
                    Intent intent = new Intent(SplashActivity.this , LoginActivity.class);
                    startActivity(intent);
                }

                // 结束当前页面.否则用户按返回键又会回到引导页，体验很不好。
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 发送两秒钟的延时消息
        handler.sendMessageDelayed(Message.obtain() , 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
