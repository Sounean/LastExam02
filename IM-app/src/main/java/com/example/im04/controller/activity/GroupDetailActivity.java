package com.example.im04.controller.activity;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.example.im04.R;
import com.example.im04.controller.adapter.GroupDetailAdapter;
import com.example.im04.model.Model;
import com.example.im04.model.bean.UserInfo;
import com.example.im04.utils.Constant;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

// 群详情页面
public class GroupDetailActivity extends Activity {

    private GridView gv_groupdetail;
    private Button bt_groupdetail_out;
    private EMGroup mGroup;
    private List<UserInfo> mUsers;
    private GroupDetailAdapter groupDetailAdapter;
    private GroupDetailAdapter.OnGroupDetailListener mOnGroupDetailListener = new GroupDetailAdapter.OnGroupDetailListener() {
       // 添加群成员
        @Override
        public void onAddMembers() {
            //跳转到选择联系人页面,选择好人后，再把联系人返回  （跳转过去时，注意一些选择人是默认选中并且不可删除的，因为他们原先就在群里面）
            Intent intent = new Intent(GroupDetailActivity.this, PickContactActivity.class);

            //传递群id
            intent.putExtra(Constant.GROUP_ID,mGroup.getGroupId());
            startActivityForResult(intent,2);// 这里需要回调，我们用配套的onActivityResult方法去接收
        }

        // 删除群成员
        @Override
        public void onDeleteMembers(UserInfo user) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //从环信服务器删除此人
                        EMClient.getInstance().groupManager().removeUserFromGroup(mGroup.getGroupId(),user.getHxid());

                        //更新页面
                        getMembersFromHxServer();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GroupDetailActivity.this,"删除"+user.getName()+"成功",Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (final HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GroupDetailActivity.this,"删除失败"+e.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        
        initView();

        getDate();

        initData();

        initListener();
    }

    private void initListener() {
        // 做gridview的点击事件，方便点击减号后，点击空白处把减号隐藏回去。
        gv_groupdetail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN://如果是摁下的方法
                        //判断当前是否是删除模式
                        if(groupDetailAdapter.ismIsCanModify()){// 括号里意义为获取当前的删除模式
                            //切换为非删除模式
                            groupDetailAdapter.setmIsDeleteModel(false);

                            //刷新页面
                            groupDetailAdapter.notifyDataSetChanged();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void initData() {
        // 初始化button显示
        initButtonDisplay();// 因为如果是群主才显示是解散群，不是群主只能退出群

        // 初始化gridview
        initGridview();

        // 从环信服务器获取所有的群成员
        getMembersFromHxServer();
    }

    private void initGridview() {
        // 当前用户是群主 || 群公开了
        boolean isCanModify = EMClient.getInstance().getCurrentUser().equals(mGroup.getOwner()) || mGroup.isPublic();// 判断有没有邀请人的权限
        groupDetailAdapter = new GroupDetailAdapter(this, isCanModify , mOnGroupDetailListener);
        gv_groupdetail.setAdapter(groupDetailAdapter);

    }

    // 从环信服务器获取所有的群成员
    private void getMembersFromHxServer() {
        Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //从环信服务器获取所有群成员信息
                    List<String> memberList = new ArrayList<>();
                    EMCursorResult<String> result = null;
                    final int pageSize = 20;
                    do {
                        result = EMClient.getInstance().groupManager().fetchGroupMembers(mGroup.getGroupId(),
                                result != null ? result.getCursor() : "", pageSize);
                        memberList.addAll(result.getData());
                    } while (!TextUtils.isEmpty(result.getCursor()) && result.getData().size() == pageSize);//获取群信息

                    if(memberList != null && memberList.size() >= 0){
                        mUsers = new ArrayList<>();

                        //转换
                        for(String member : memberList){
                            UserInfo userInfo = new UserInfo(member);
                            mUsers.add(userInfo);
                        }

                        //刷新页面
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //刷新适配器
                                groupDetailAdapter.refresh(mUsers);
                            }
                        });
                    }
                } catch (final HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroupDetailActivity.this,"获取群信息失败"+e.toString(),Toast.LENGTH_SHORT).show();
                            Log.e("", "获取群信息失败"+e.toString());
                        }
                    });
                }

            }
        });
    }


    private void initButtonDisplay() {
        if (EMClient.getInstance().getCurrentUser().equals(mGroup.getOwner())){//EMClient.getInstance().getCurrentUser()是获取当前用户的意思，这里是判断当前是否为群主
            bt_groupdetail_out.setText("解散群");

            bt_groupdetail_out.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 去环信服务器解散群
                                EMClient.getInstance().groupManager().destroyGroup(mGroup.getGroupId());// 解散该群

                                // 发送退群的广播
                                exitGroupBroadCast();

                                // 更新页面
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(GroupDetailActivity.this , "解散群成功" , Toast.LENGTH_LONG).show();

                                        // 解散群后当前的页面就不需要留着了
                                        finish();
                                    }
                                });
                            } catch (HyphenateException e) {
                                e.printStackTrace();
                                // 更新页面
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(GroupDetailActivity.this , "解散群失败"+e.toString() , Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }else {// 群成员
            bt_groupdetail_out.setText("退群");
            bt_groupdetail_out.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 告诉环信服务器退群
                                EMClient.getInstance().groupManager().leaveGroup(mGroup.getGroupId());

                                // 发送退群广播
                                exitGroupBroadCast();

                                // 更新页面
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(GroupDetailActivity.this , "退出群成功" , Toast.LENGTH_LONG).show();

                                        // 解散群后当前的页面就不需要留着了
                                        finish();
                                    }
                                });
                            } catch (HyphenateException e) {
                                e.printStackTrace();
                                // 更新页面
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(GroupDetailActivity.this , "退群失败"+e.toString() , Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    // 发送退群和解散群的广播
    private void exitGroupBroadCast() {
        LocalBroadcastManager mLBM = LocalBroadcastManager.getInstance(GroupDetailActivity.this);

        Intent intent = new Intent(Constant.EXIT_GROUP);

        intent.putExtra(Constant.GROUP_ID , mGroup.getGroupId());

        mLBM.sendBroadcast(intent);// 这样子就发送出去广播了。
    }

    // 获取传递过来的数据
    private void getDate() {
        Intent intent = getIntent();
        String groupId = intent.getStringExtra(Constant.GROUP_ID);

        if (groupId == null){//如果没有群id，直接结束
            return;
        }else {// 通过群id来获取群信息
            mGroup = EMClient.getInstance().groupManager().getGroup(groupId);//拿到这个群的所有相关信息
        }
    }

    private void initView() {
        gv_groupdetail = findViewById(R.id.gv_groupdetail);
        bt_groupdetail_out = findViewById(R.id.bt_groupdetail_out);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            //获取返回的准备邀请的群成员信息
            final String[] memberses = data.getStringArrayExtra("members");

            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //去环信服务器，发送邀请信息
                        EMClient.getInstance().groupManager().addUsersToGroup(mGroup.getGroupId(),memberses);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GroupDetailActivity.this,"发送邀请成功",Toast.LENGTH_SHORT).show();
                                groupDetailAdapter.refresh(mUsers);
                            }
                        });
                    } catch (final HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GroupDetailActivity.this,"发送邀请失败"+e.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

}
