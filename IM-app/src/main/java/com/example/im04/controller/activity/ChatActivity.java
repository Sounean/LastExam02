package com.example.im04.controller.activity;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.example.im04.R;
import com.example.im04.utils.Constant;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.widget.chatrow.EaseCustomChatRowProvider;

// 会话详情页面
public class ChatActivity extends FragmentActivity {

    private String mHxid;
    private EaseChatFragment easeChatFragment;
    private int mChatType;
    private LocalBroadcastManager mLBM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initData();

        initListener();
    }

    private void initListener() {
        easeChatFragment.setChatFragmentHelper(new EaseChatFragment.EaseChatFragmentHelper() {
            @Override
            public void onSetMessageAttributes(EMMessage message) {

            }

            @Override
            public void onEnterToChatDetails() {// 进入到群详情页面
                Intent intent = new Intent(ChatActivity.this, GroupDetailActivity.class);
                // 群id
                intent.putExtra(Constant.GROUP_ID , mHxid);
                startActivity(intent);
            }

            @Override
            public void onAvatarClick(String username) {

            }

            @Override
            public void onAvatarLongClick(String username) {

            }

            @Override
            public boolean onMessageBubbleClick(EMMessage message) {
                return false;
            }

            @Override
            public void onMessageBubbleLongClick(EMMessage message) {

            }

            @Override
            public boolean onExtendMenuItemClick(int itemId, View view) {
                return false;
            }

            @Override
            public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
                return null;
            }
        });

        // 如果当前类型为群聊
        if (mChatType == EaseConstant.CHATTYPE_GROUP){

            BroadcastReceiver ExitGroupReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mHxid.equals(intent.getStringExtra(Constant.GROUP_ID))){//判断下当前会话的id是否和群id一样
                        // 结束当前页面
                        finish();
                    }
                }
            };
            // 注册退群广播
            mLBM.registerReceiver(ExitGroupReceiver , new IntentFilter(Constant.EXIT_GROUP));
        }
    }

    private void initData() {
        mHxid = getIntent().getStringExtra(EaseConstant.EXTRA_USER_ID);
        //获取聊天类型
        mChatType = getIntent().getExtras().getInt(EaseConstant.EXTRA_CHAT_TYPE);

        // 替换fragment
        easeChatFragment = new EaseChatFragment();
        easeChatFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fl_chat , easeChatFragment).commit();

        // 获取发送广播的管理者
        mLBM = LocalBroadcastManager.getInstance(ChatActivity.this);
    }
}
