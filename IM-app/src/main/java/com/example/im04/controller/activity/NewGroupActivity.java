package com.example.im04.controller.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.im04.R;
import com.example.im04.model.Model;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.exceptions.HyphenateException;

// 新建群组
public class NewGroupActivity extends Activity {

    private EditText et_newgroup_name;
    private EditText et_newgroup_desc;
    private CheckBox cb_newgroup_public;
    private CheckBox cb_newgroup_invite;
    private Button bt_newgroup_create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        initView();
        initListener();
    }

    // 初始化监听
    private void initListener() {
        // 创建按钮的点击事件处理
        bt_newgroup_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到选择联系人页面
                Intent intent = new Intent(NewGroupActivity.this, PickContactActivity.class);
                startActivityForResult(intent , 1);//这里返回值，需要另外实现下方的onActivityResult方法才能去获取
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//data即为带回来的被选中的联系人
        super.onActivityResult(requestCode, resultCode, data);
        //判断是否成功获取到联系人
        if (resultCode == RESULT_OK){
            // 创建群
            createGroup(data.getStringArrayExtra("members"));//新建一个创建群的方法
        }
    }

    //创建群
    private void createGroup(String[] members) {
        //  群名称
        String groupName = et_newgroup_name.getText().toString();
        //  群描述
        String groupDesc = et_newgroup_desc.getText().toString();


        Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //去环信服务器创建群
                //参数一：群名称；  参数二：群描述；    参数三：群成员；    参数四：原因； 参数五：参数设置
                EMGroupOptions options = new EMGroupOptions();
                options.maxUsers = 200;// 群最多可以容纳多少人
                EMGroupManager.EMGroupStyle groupStyle = null;// 群样式根据“是否公开”和“是否开放群邀请”来设置（两种变量有4种组合）
                if (cb_newgroup_public.isChecked()){// 公开
                    if (cb_newgroup_invite.isChecked()){// 开放了群邀请
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePublicOpenJoin;// 这里的参数是sdk里面有的，要根据选项来设置参数
                    }else {// 没有开放群邀请
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePublicJoinNeedApproval;
                    }
                }else {
                    if (cb_newgroup_invite.isChecked()){// 开放了群邀请
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
                    }else {// 没有开放群邀请
                        groupStyle = EMGroupManager.EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                    }
                }
                options.style = groupStyle;//   创建群的类型

                try {
                    EMClient.getInstance().groupManager().createGroup(groupName,groupDesc,members,"申请加入群",options);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NewGroupActivity.this , "创建群成功" , Toast.LENGTH_LONG).show();
                            Log.e("?????", "3");
                            // 结束当前页面
                            finish();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NewGroupActivity.this , "创建群失败" , Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    // 初始化view
    private void initView() {
        et_newgroup_name = findViewById(R.id.et_newgroup_name);
        et_newgroup_desc = findViewById(R.id.et_newgroup_desc);
        cb_newgroup_public = findViewById(R.id.cb_newgroup_public);
        cb_newgroup_invite = findViewById(R.id.cb_newgroup_invite);
        bt_newgroup_create = findViewById(R.id.bt_newgroup_create);
    }
}
