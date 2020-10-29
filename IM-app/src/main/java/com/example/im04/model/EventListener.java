package com.example.im04.model;


import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.im04.model.bean.GroupInfo;
import com.example.im04.model.bean.InvatationInfo;
import com.example.im04.model.bean.UserInfo;
import com.example.im04.model.dao.ContactTable;
import com.example.im04.utils.Constant;
import com.example.im04.utils.SpUtils;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMucSharedFile;

import java.util.List;

//全局事件监听类
public class EventListener {

    private Context mContext;
    private LocalBroadcastManager mLBM;

    public EventListener(Context context) {
        mContext = context;

        //创建一个发送广播的管理者对象
        mLBM = LocalBroadcastManager.getInstance(mContext);

        //注册一个联系人变化的监听
        EMClient.getInstance().contactManager().setContactListener(emContactListener);

        //注册一个群信息变化的监听
        EMClient.getInstance().groupManager().addGroupChangeListener(eMGroupChangeListener);
    }

    //注册一个联系人变化的监听
    private final EMContactListener emContactListener = new EMContactListener() {

        //添加联系人方法
        @Override
        public void onContactAdded(String hxid) {
            //数据库更新
            Model.getInstance().getDBManager().getContactTableDao().saveContact(new UserInfo(hxid), true);

            //发送联系人变化的广播
            mLBM.sendBroadcast(new Intent(Constant.CONTACT_CHANGED));
        }

        //联系人删除后执行的方法
        @Override
        public void onContactDeleted(String hxid) {
            //数据库更新
            Model.getInstance().getDBManager().getContactTableDao().deleteContactByHxId(hxid);
            Model.getInstance().getDBManager().getInviteTableDao().removeInvitation(hxid);
            //发送联系人变化的广播
            mLBM.sendBroadcast(new Intent(Constant.CONTACT_CHANGED));
        }

        //接受到联系人的新邀请
        @Override
        public void onContactInvited(String hxid, String reason) {
            //数据库更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setUser(new UserInfo(hxid));
            invitationInfo.setReason(reason);
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.NEW_INVITE);  //新邀请

            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点的处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送邀请信息变化的广播
            mLBM.sendBroadcast(new Intent(Constant.CONTACT_INVITE_CHANGED));
        }

        //别人同意了你的好友邀请
        @Override
        public void onFriendRequestAccepted(String hxid) {
            //数据库更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setUser(new UserInfo(hxid));
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.INVITE_ACCEPT_BY_PEER);  //别人同意了你的邀请
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点的处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);


            //发送邀请信息变化的广播
            mLBM.sendBroadcast(new Intent(Constant.CONTACT_INVITE_CHANGED));
        }

        //别人拒绝了你的好友邀请
        @Override
        public void onFriendRequestDeclined(String s) {
            //红点的处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送邀请信息变化的广播
            mLBM.sendBroadcast(new Intent(Constant.CONTACT_INVITE_CHANGED));
        }
    };

    //群信息变化的监听
    private final EMGroupChangeListener eMGroupChangeListener = new EMGroupChangeListener() {
        //收到群邀请
        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
            //数据更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setReason(reason);
            invitationInfo.setGroup(new GroupInfo(groupName, groupId, inviter));
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.NEW_GROUP_INVITE);
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送广播
            mLBM.sendBroadcast(new Intent(Constant.GROUP_INVITE_CHANGED));
        }


        //收到群申请通知
        @Override
        public void onRequestToJoinReceived(String groupId, String groupName, String applicant, String reason) {
            //数据更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setReason(reason);
            invitationInfo.setGroup(new GroupInfo(groupName, groupId, applicant));// applicant是申请人
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.NEW_GROUP_APPLICATION);
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送广播
            mLBM.sendBroadcast(new Intent(Constant.GROUP_INVITE_CHANGED));
        }

        //收到群申请被接受
        @Override
        public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {
            //数据更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setGroup(new GroupInfo(groupName, groupId, accepter));
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_APPLICATION_ACCEPTED);
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送广播
            mLBM.sendBroadcast(new Intent(Constant.GROUP_INVITE_CHANGED));
        }

        //收到群申请被拒绝
        @Override
        public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {
            //数据更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setReason(reason);
            invitationInfo.setGroup(new GroupInfo(groupName, groupId, decliner));//decliner拒绝的人
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_APPLICATION_DECLINED);
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送广播
            mLBM.sendBroadcast(new Intent(Constant.GROUP_INVITE_CHANGED));
        }

        //收到群邀请被同意
        @Override
        public void onInvitationAccepted(String groupId, String inviter, String reason) {
            //数据更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setReason(reason);
            invitationInfo.setGroup(new GroupInfo(groupId, groupId, inviter));
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_INVITE_ACCEPTED);
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送广播
            mLBM.sendBroadcast(new Intent(Constant.GROUP_INVITE_CHANGED));
        }

        //收到群邀请被拒绝
        @Override
        public void onInvitationDeclined(String groupId, String inviter, String reason) {
            //数据更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setReason(reason);
            invitationInfo.setGroup(new GroupInfo(groupId, groupId, inviter));
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_INVITE_DECLINED);
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送广播
            mLBM.sendBroadcast(new Intent(Constant.GROUP_INVITE_CHANGED));
        }

        //收到群成员被拒绝
        @Override
        public void onUserRemoved(String groupId, String groupName) {

        }

        //收到群被解散
        @Override
        public void onGroupDestroyed(String groupId, String groupName) {

        }

        //收到群邀请被自动接受
        @Override
        public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviterMessage) {
            //数据更新
            InvatationInfo invitationInfo = new InvatationInfo();
            invitationInfo.setReason(inviterMessage);
            invitationInfo.setGroup(new GroupInfo(groupId, groupId, inviter));
            invitationInfo.setStatus(InvatationInfo.InvitationStatus.GROUP_INVITE_ACCEPTED);
            Model.getInstance().getDBManager().getInviteTableDao().addInvitation(invitationInfo);

            //红点处理
            SpUtils.getInstance().save(SpUtils.IS_NEW_INVITE, true);

            //发送广播
            mLBM.sendBroadcast(new Intent(Constant.GROUP_INVITE_CHANGED));
        }

        @Override
        public void onMuteListAdded(String s, List<String> list, long l) {

        }

        @Override
        public void onMuteListRemoved(String s, List<String> list) {

        }

        @Override
        public void onAdminAdded(String s, String s1) {

        }

        @Override
        public void onAdminRemoved(String s, String s1) {

        }

        @Override
        public void onOwnerChanged(String s, String s1, String s2) {

        }

        @Override
        public void onMemberJoined(String s, String s1) {

        }

        @Override
        public void onMemberExited(String s, String s1) {

        }

        @Override
        public void onAnnouncementChanged(String s, String s1) {

        }

        @Override
        public void onSharedFileAdded(String s, EMMucSharedFile emMucSharedFile) {

        }

        @Override
        public void onSharedFileDeleted(String s, String s1) {

        }
    };


}