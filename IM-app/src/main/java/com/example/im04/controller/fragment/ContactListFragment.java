package com.example.im04.controller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.baidu.lbsapi.auth.LBSAuthManager;
import com.example.im04.R;
import com.example.im04.controller.activity.AddContactActivity;
import com.example.im04.controller.activity.ChatActivity;
import com.example.im04.controller.activity.GroupListActivity;
import com.example.im04.controller.activity.InviteActivity;
import com.example.im04.model.Model;
import com.example.im04.model.bean.UserInfo;
import com.example.im04.utils.Constant;
import com.example.im04.utils.SpUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 联系人列表页面
public class ContactListFragment extends EaseContactListFragment {// 将此处的fragment改成EaseContactListFragment。
    ImageView iv_contact_red;
    LocalBroadcastManager mLBM;
    LinearLayout ll_contact_invite;
    String mHxid;


    private BroadcastReceiver ContactChangeReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //刷新页面
            refreshContact();
        }
    };

    //刷新页面
    private void refreshContact() {
        //获取数据
        List<UserInfo> contacts = Model.getInstance().getDBManager().getContactTableDao().getContacts();
        //校验
        if (contacts != null && contacts.size() >= 0) {
            //设置数据
            Map<String, EaseUser> contactsMap = new HashMap<>();

            //转换   把联系人的信息contacts转换成contactsMap。
            for (UserInfo contact : contacts) {
                EaseUser easeUser = new EaseUser( contact.getHxid() );
                contactsMap.put( contact.getHxid(), easeUser );
            }
            //设置数据
            setContactsMap( contactsMap );//这是环信提供了的api，向数据库提供数据。

            //刷新页面
            refresh();//这是环信提供了的api
        }

    }

    private BroadcastReceiver ContactInviteChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新红点显示
            iv_contact_red.setVisibility( View.VISIBLE );
            SpUtils.getInstance().save( SpUtils.IS_NEW_INVITE, true );

        }
    };
    private BroadcastReceiver GroupChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //显示红点
            iv_contact_red.setVisibility( View.VISIBLE );
            SpUtils.getInstance().save( SpUtils.IS_NEW_INVITE, true );
        }
    };


    @Override
    protected void initView() {
        super.initView();// 因为EaseContactListFragment里就有initView()方法

        // 布局显示加号
        titleBar.setRightImageResource(R.drawable.em_add);// titleBar就是最上方的东西，继承了EaseContactListFragment后就自带的布局(但功能很弱，推荐自己新建一个)。

        // 添加头布局
        View headerView = View.inflate(getActivity(), R.layout.header_fragment_contact, null);
        listView.addHeaderView(headerView);//这里listView是因为EaseContactListFragment内的initView里就有。

        // 获取红点对象
        iv_contact_red = headerView.findViewById(R.id.iv_contact_red);

        // 获取邀请信息条目的对象
        ll_contact_invite = headerView.findViewById(R.id.ll_contact_invite);

        // 设置listview条目的点击事件
        setContactListItemClickListener(new EaseContactListItemClickListener() {
            @Override
            public void onListItemClicked(EaseUser user) {

                if (user == null) {
                    return;
                }
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                // 因为也分群聊单聊，所以这里还需要传递参数。
                intent.putExtra(EaseConstant.EXTRA_USER_ID , user.getUsername());
                startActivity(intent);
            }
        });//EaseContactListFragment给封装好的listview条目的点击事件。

        // 跳转到群组列表页面
        LinearLayout ll_contact_group = headerView.findViewById(R.id.ll_contact_group);
        ll_contact_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GroupListActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void setUpView() {// 处理业务逻辑
        super.setUpView();

        // 添加按钮的点击事件处理
        titleBar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddContactActivity.class);
                startActivity(intent);
            }
        });


        // 初始化红点的显示
        boolean isNewInvite = SpUtils.getInstance().getBoolean(SpUtils.IS_NEW_INVITE, false);
        iv_contact_red.setVisibility(isNewInvite ? View.VISIBLE : View.GONE);

        // 邀请信息条目的点击事件
        ll_contact_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 红点处理
                iv_contact_red.setVisibility(View.GONE);
                SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE , false);

                // 跳转到邀请信息列表页面
                Intent intent = new Intent(getActivity(), InviteActivity.class);
                startActivity(intent);
            }
        });

        //  注册广播
        mLBM = LocalBroadcastManager.getInstance(getActivity());
        mLBM.registerReceiver(ContactInviteChangeReceiver , new IntentFilter(Constant.CONTACT_INVITE_CHANGED));
        mLBM.registerReceiver(ContactChangeReceiver , new IntentFilter(Constant.CONTACT_CHANGED));
        mLBM.registerReceiver(GroupChangeReceiver , new IntentFilter(Constant.GROUP_INVITE_CHANGED));
        //  从环信服务器获取所有的联系人信息
        getContactFromHxServer();

        //  绑定listview和contextmenu
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // 获取环信id
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        EaseUser easeUser = (EaseUser) listView.getItemAtPosition(position);

        mHxid = easeUser.getUsername();// 这里环信id和环信的username值一样，所以通过获取username来获取hxid。

        // 添加布局
        getActivity().getMenuInflater().inflate(R.menu.delete , menu);
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.contact_delete){
            // 执行删除选中的联系人操作
            deleteContact();

            return true;
        }
        return super.onContextItemSelected(item);
    }

    // 执行删除选中的联系人操作
    private void deleteContact() {
        Model.getInstance().getGlobalThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(mHxid);

                    // 本地数据库的更新
                    Model.getInstance().getDBManager().getContactTableDao().deleteContactByHxId(mHxid);
                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // toast提示
                            Toast.makeText(getActivity() , "删除"+mHxid+"成功" , Toast.LENGTH_LONG).show();

                            // 刷新页面
                            refreshContact();
                        }
                    });

                } catch (HyphenateException e) {
                    e.printStackTrace();

                    if (getActivity() == null){
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // toast提示
                            Toast.makeText(getActivity() , "删除"+mHxid+"失败" , Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void getContactFromHxServer() {
        //从环信服务器获取所有的联系人信息
        Model.getInstance().getGlobalThreadPool().execute( new Runnable() {
            @Override
            public void run() {
                try {
                    //获取到所有好友的环信id
                    List<String> hxids = EMClient.getInstance().contactManager().getAllContactsFromServer();

                    //校验
                    if (hxids != null && hxids.size() >= 0) {
                        List<UserInfo> contacts = new ArrayList<>();
                        //转换
                        for (String hxid : hxids) {
                            UserInfo userInfo = new UserInfo( hxid );
                            contacts.add( userInfo );
                        }

                        //保存好友信息到本地数据库
                        Model.getInstance().getDBManager().getContactTableDao().saveContacts( contacts, true );

                        if (getActivity() == null) {
                            return;
                        }
                        //刷新页面
                        getActivity().runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                //刷新页面方法
                                refreshContact();
                            }
                        } );

                    }

                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        } );

    }

    // 注册了广播还要注意要关掉广播
    @Override
    public void onDestroy() {
        super.onDestroy();

        mLBM.unregisterReceiver(ContactInviteChangeReceiver);
        mLBM.unregisterReceiver(ContactChangeReceiver);
    }
}
