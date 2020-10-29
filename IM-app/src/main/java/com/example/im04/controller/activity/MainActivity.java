package com.example.im04.controller.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.im04.R;
import com.example.im04.controller.fragment.ChatFragment;
import com.example.im04.controller.fragment.ContactListFragment;
import com.example.im04.controller.fragment.SettingFragment;

public class MainActivity extends FragmentActivity {

    private RadioGroup rg_main;
    ChatFragment chatFragment;
    ContactListFragment contactListFragment;
    SettingFragment settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initData();

        initListener();// 初始化监听
    }

    private void initListener() {
        // RadioGroup的选择事件
        rg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {//checkedId就是你选择了，点击了哪个id。
                Fragment fragment = null;

                // 会话列表页面
                if (checkedId == R.id.rb_main_chat) {
                    fragment = chatFragment;
                    // 联系人列表页面
                } else if (checkedId == R.id.rb_main_contact) {
                    fragment = contactListFragment;
                    // 设置页面
                } else if (checkedId == R.id.rb_main_setting) {
                    fragment = settingFragment;
                }
                ;


                // 实现fragment切换的方法
                switchFragment(fragment);
            }
        });

        // 默认选择一个确认的页面
        rg_main.check(R.id.rb_main_chat);
    }

    // 实现切换fragment的方法。
    private void switchFragment(Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();// 拿到这个管理者对象。
        supportFragmentManager.beginTransaction().replace(R.id.fl_main , fragment).commit();// 开启事务.把布局里的内容替换成此处的fragment，最后再提交。
    }

    private void initData() {
        // 创建三个fragment对象
        chatFragment = new ChatFragment();
        contactListFragment = new ContactListFragment();
        settingFragment = new SettingFragment();
    }


    @SuppressLint("WrongViewCast")
    private void initView() {
        rg_main = findViewById(R.id.rg_main);
    }
}
