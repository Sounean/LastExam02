package com.example.im04.controller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.im04.R;
import com.example.im04.controller.activity.LoginActivity;
import com.example.im04.model.Model;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;


//  设置页面
public class SettingFragment extends Fragment {

    private Button btn_setting_out;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_setting, null);

        initView(view);
        return view;
    }

    private void initView(View view) {
        btn_setting_out = view.findViewById(R.id.btn_setting_out);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {// 当Fragment所在的Activity被启动完成后回调该方法。
        super.onActivityCreated(savedInstanceState);

        initData();
    }

    private void initData() {
        //  在button上显示当前用户名称
        btn_setting_out.setText("退出登录 (" + EMClient.getInstance().getCurrentUser() + ")");//EMClient.getInstance().getCurrentUser()是获取用户名的方法

        //退出登录的逻辑处理 (告诉环信服务器我要退出，这是一个联网操作)
        btn_setting_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        // 登录服务器退出登录
                        EMClient.getInstance().logout(false, new EMCallBack() {
                            @Override
                            public void onSuccess() {

                                // 关闭DBHelper
                                Model.getInstance().getDBManager().close();


                                getActivity().runOnUiThread(new Runnable() {// 直接runOnUi不行，必须先getActivity先获取当前活动。
                                    @Override
                                    public void run() {
                                        // 更新ui显示
                                        Toast.makeText(getActivity() , "退出成功" , Toast.LENGTH_LONG).show();

                                        // 退出成功，回到登录页面
                                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                                        startActivity(intent);

                                        getActivity().finish();
                                    }
                                });
                            }

                            @Override
                            public void onError(int code, String error) {// 退出失败
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity() , "退出失败" + error , Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onProgress(int progress, String status) {

                            }
                        });
                    }
                });
            }
        });
    }
}
