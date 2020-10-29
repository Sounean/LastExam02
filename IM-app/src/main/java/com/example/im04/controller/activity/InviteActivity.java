package com.example.im04.controller.activity;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.im04.R;
import com.example.im04.controller.adapter.InviteAdapter;
import com.example.im04.model.Model;
import com.example.im04.model.bean.InvatationInfo;
import com.example.im04.utils.Constant;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;

// 邀请信息列表页面
public class InviteActivity extends Activity {
    ListView lv_invite;
    InviteAdapter inviteAdapter;
    private LocalBroadcastManager mLBM;
    private BroadcastReceiver InviteChangedReceiver = new BroadcastReceiver() {//刷新页面的广播(好友被邀请时需要广播，群消息变化时需要)
        @Override
        public void onReceive(Context context, Intent intent) {// 如果接收到了邀请人信息变化了，就开始执行这个方法
            // 刷新页面
            refresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        initView();
        initData();
    }

    private void initView() {
        lv_invite = findViewById(R.id.lv_invite);
    }



    private void initData() {
        // 初始化ListView
        inviteAdapter = new InviteAdapter(this ,mOnInviteListener );

        lv_invite.setAdapter(inviteAdapter);

        // 刷新方法
        refresh();

        // 注册邀请信息变化的广播
        mLBM = LocalBroadcastManager.getInstance(this);
        mLBM.registerReceiver(InviteChangedReceiver , new IntentFilter(Constant.CONTACT_INVITE_CHANGED));//第一个参数是广播接收，第二个参数是过滤器（里面传联系人邀请信息变化的意图）。    邀请人信息变化的广播
        mLBM.registerReceiver(InviteChangedReceiver , new IntentFilter(Constant.GROUP_INVITE_CHANGED));//群邀信息的变化广播。
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //销毁开启的广播（只要有广播就记得要销毁！否则会导致内存泄漏）
        mLBM.unregisterReceiver(InviteChangedReceiver);
    }

    private void refresh() {
        // 获取数据库中的所有邀请信息
        List<InvatationInfo> invitations = Model.getInstance().getDBManager().getInviteTableDao().getInvitations();

        // 刷新适配器
        inviteAdapter.refresh(invitations);// 信息都通过EventListener存在本地数据库中。通过上一句getInvitations来获取的本地信息。
    }

    private InviteAdapter.OnInviteListener mOnInviteListener = new InviteAdapter.OnInviteListener() {
        @Override
        public void onAccept(InvatationInfo invatationInfo) {
            // 通知环信服务器，点击了接受按钮
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().contactManager().acceptInvitation(invatationInfo.getUser().getHxid());//先通知环信服务器，接受了变化

                        // 数据库更新
                        Model.getInstance().getDBManager().getInviteTableDao().updateInvitationStatus(InvatationInfo.InvitationStatus.INVITE_ACCEPT , invatationInfo.getUser().getHxid());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 页面发生变化
                                Toast.makeText(InviteActivity.this, "接受了邀请" , Toast.LENGTH_LONG).show();

                                // 刷新页面
                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this, "接受邀请失败" , Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });

        }

        @Override
        public void onReject(InvatationInfo invatationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().contactManager().declineInvitation(invatationInfo.getUser().getHxid());

                        //数据库变化
                        Model.getInstance().getDBManager().getInviteTableDao().removeInvitation(invatationInfo.getUser().getHxid());

                        //页面变化
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this , "拒绝成功了" , Toast.LENGTH_LONG).show();

                                // 刷新页面
                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        //页面变化
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this , "拒绝失败了" , Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }

        //接受邀请按钮
        @Override
        public void onInviteAccept(InvatationInfo invatationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //告诉环信服务器接受了邀请
                        EMClient.getInstance().groupManager().acceptInvitation(invatationInfo.getGroup().getGroupId(),invatationInfo.getGroup().getInvatePerson());//第一个参数群id；第二个是接受了谁的（邀请人）。

                        //本地数据库更新
                        invatationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_ACCEPT_INVITE);
                        Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invatationInfo);

                        //内存数据变化
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this,"接受邀请",Toast.LENGTH_SHORT).show();

                                //刷新页面
                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this,"接受邀jain请失败",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        //拒绝邀请按钮
        @Override
        public void onInviteReject(InvatationInfo invatationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //告诉环信服务器拒绝了邀请
                        EMClient.getInstance().groupManager().declineInvitation(invatationInfo.getGroup().getGroupId(),invatationInfo.getGroup().getInvatePerson(),"拒绝邀请的理由");//.getInvitePerson()

                        invatationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_REJECT_INVITE);
                        //  更新本地数据库
                        Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invatationInfo);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this,"拒绝邀请",Toast.LENGTH_SHORT).show();

                                refresh();
                            }
                        });

                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        //接受申请按钮
        @Override
        public void onApplicationAccept(InvatationInfo invatationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //告诉环信服务器接受了申请
                        EMClient.getInstance().groupManager().acceptApplication(invatationInfo.getGroup().getGroupId(),invatationInfo.getGroup().getInvatePerson());

                        invatationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_ACCEPT_APPLICATION);
                        Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invatationInfo);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this,"接受申请",Toast.LENGTH_SHORT).show();

                                refresh();
                            }
                        });

                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        //拒绝申请按钮
        @Override
        public void onApplicationReject(InvatationInfo invatationInfo) {
            Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //告诉环信服务器拒绝了申请
                        EMClient.getInstance().groupManager().declineApplication(invatationInfo.getGroup().getGroupId(),invatationInfo.getGroup().getInvatePerson() ,"拒绝申请");

                        invatationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_REJECT_APPLICATION);
                        Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invatationInfo);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this,"拒绝申请",Toast.LENGTH_SHORT).show();

                                refresh();
                            }
                        });

                    } catch (HyphenateException e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(InviteActivity.this,"拒绝申请失败",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    };


}
