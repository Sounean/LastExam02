package com.example.im04.controller.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.im04.R;
import com.example.im04.controller.adapter.GroupListAdapter;
import com.example.im04.model.Model;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.exceptions.HyphenateException;

import java.security.spec.ECField;
import java.util.List;

// 群组列表页面
public class GroupListActivity extends Activity {

    private ListView lv_grouplist;
    private GroupListAdapter groupListAdapter;
    LinearLayout ll_grouplist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        initView();

        initData();

        // 创建监听，群组listview条目的点击事件监听
        initListener();
    }

    private void initListener() {
        // listview的点击事件
        lv_grouplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0){ // 为0说明是头布局
                    return;
                }
                Intent intent = new Intent(GroupListActivity.this, ChatActivity.class);

                // 传递会话类型
                intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE , EaseConstant.CHATTYPE_GROUP);

                // 群id
                EMGroup emGroup = EMClient.getInstance().groupManager().getAllGroups().get(position - 1);// 这里需要减掉1是因为有头布局在，不算正常的群组内。所以需要减个1.
                intent.putExtra(EaseConstant.EXTRA_USER_ID , emGroup.getGroupId());

                startActivity(intent);
            }
        });

        //  跳转到新建群。
        ll_grouplist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupListActivity.this, NewGroupActivity.class);

                startActivity(intent);
            }
        });
    }

    private void initData() {
        // 初始化ListView
        groupListAdapter = new GroupListAdapter(this);
        lv_grouplist.setAdapter(groupListAdapter);

        // 从环信服务器获取所有群的相关信息
        getGroupsFromServer();
    }

    private void getGroupsFromServer() {
        Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 从网络上获取数据
                    List<EMGroup> mGroups = EMClient.getInstance().groupManager().getJoinedGroupsFromServer();

                    // 更新页面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupListActivity.this , "加载群信息成功" , Toast.LENGTH_LONG).show();

                            // 刷新页面
                            refresh();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupListActivity.this , "加载群信息失败" , Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    // 刷新
    private void refresh() {
        groupListAdapter.refresh(EMClient.getInstance().groupManager().getAllGroups());
    }

    private void initView() {
        // 获取listview对象
        lv_grouplist = findViewById(R.id.lv_grouplist);

        // 添加头布局
        View headerView = View.inflate(this, R.layout.header_grouplist, null);
        lv_grouplist.addHeaderView(headerView);

        ll_grouplist = headerView.findViewById(R.id.ll_grouplist);
    }


    @Override
    protected void onResume() {// 当再次可见时，要刷新页面，因为可能添加了新群。
        super.onResume();

        // 刷新页面
        refresh();
    }
}
