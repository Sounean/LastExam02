package com.example.im04.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.example.im04.R;
import com.example.im04.controller.adapter.PickContactAdapter;
import com.example.im04.model.Model;
import com.example.im04.model.bean.PickContactInfo;
import com.example.im04.model.bean.UserInfo;
import com.example.im04.utils.Constant;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;

import java.util.ArrayList;
import java.util.List;

// 选择联系人页面
public class PickContactActivity extends Activity {

    private TextView tv_pick_save;
    private ListView lv_pick;
    private List<PickContactInfo> mPicks;
    private PickContactAdapter pickContactAdapter;
    private List<String> mExistMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_contact);

        //获取从GroupDetailActivity类中传递过来的数据，用于选择联系人用。
        getData();

        initView();

        initData();

        initListener();
    }

    private void getData() {
        String groupId = getIntent().getStringExtra(Constant.GROUP_ID);

        if(groupId != null ){
            EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);

            //获取群中已经存在的所有群成员  获取到后方便将这些人作为默认选中并且不可修改（表示本来就在群里了）
            mExistMembers = group.getMembers();
        }

        if(mExistMembers == null){
            mExistMembers = new ArrayList<>();
        }
    }

    private void initListener() {
        // listview条目的点击事件(被选中的取消选中，未被选中的变成选中)
        lv_pick.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//此处的view就是被点击的该条目
                // checkbox的切换
                CheckBox cb_pick = view.findViewById(R.id.cb_pick);
                cb_pick.setChecked(!cb_pick.isChecked());//cb_pick.isChecked()表示当前是否被选择的状态

                // 修改数据
                PickContactInfo pickContactInfo = mPicks.get(position);// 通过用户组的position来获取具体的数据
                pickContactInfo.setChecked(cb_pick.isChecked());

                // 刷新页面
                pickContactAdapter.notifyDataSetChanged();
            }
        });

        // 保存按钮的点击事件
        tv_pick_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取到已经选择的联系人
                List<String> names = pickContactAdapter.getPickContacts();

                // 给启动页面返回数据
                Intent intent = new Intent();

                intent.putExtra("members",names.toArray(new String[0]));

                // 设置返回的结果码
                setResult(RESULT_OK, intent);

                // 结束当前页面
                finish();
            }
        });
    }

    private void initData() {

        // 从本地数据库里获取所有的联系人信息
        List<UserInfo> contacts = Model.getInstance().getDBManager().getContactTableDao().getContacts();

        mPicks = new ArrayList<>();
        if (contacts != null && contacts.size() >= 0){
            // 转换
            for (UserInfo contact : contacts){
                PickContactInfo pickContactInfo = new PickContactInfo(contact, false);// 构造新建一个PickContactInfo，默认是没有选中
                mPicks.add(pickContactInfo);
            }
        }
        // 初始化listview
        pickContactAdapter = new PickContactAdapter(this , mPicks , mExistMembers);
        lv_pick.setAdapter(pickContactAdapter);
    }

    private void initView() {
        tv_pick_save = findViewById(R.id.tv_pick_save);
        lv_pick = findViewById(R.id.lv_pick);
    }
}
