package com.example.im04.model.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.im04.model.bean.GroupInfo;
import com.example.im04.model.bean.InvatationInfo;
import com.example.im04.model.bean.UserInfo;
import com.example.im04.model.db.DBHelper;

import java.util.ArrayList;
import java.util.List;

//邀请信息表的操作类
public class InviteTableDao {
    private DBHelper mhelper;

    public InviteTableDao(DBHelper helper) {
        mhelper = helper;
    }

    //添加邀请
    public void addInvitation(InvatationInfo invitationInfo) {
        //获取数据库连接
        SQLiteDatabase db = mhelper.getReadableDatabase();

        //执行添加语句
        ContentValues values = new ContentValues();
        values.put(InviteTable.COL_REASON, invitationInfo.getReason());  //原因
        values.put(InviteTable.COL_STATUS, invitationInfo.getStatus().ordinal()); // 状态

        UserInfo user = invitationInfo.getUser();

        if (user != null) {//联系人
            values.put(InviteTable.COL_USER_HXID, invitationInfo.getUser().getHxid());
            values.put(InviteTable.COL_USER_NAME, invitationInfo.getUser().getName());
        } else {//群组
            values.put(InviteTable.COL_GROUP_HXID, invitationInfo.getGroup().getGroupId());
            values.put(InviteTable.COL_GROUP_NAME, invitationInfo.getGroup().getGroupName());
            values.put(InviteTable.COL_USER_HXID, invitationInfo.getGroup().getInvatePerson());
        }

        db.replace(InviteTable.TAB_NAME, null, values);
    }

    //获取所有邀请信息
    public List<InvatationInfo> getInvitations() {
        //获取数据库连接
        SQLiteDatabase db = mhelper.getReadableDatabase();
        //执行查询语句
        String sql = "select * from " + InviteTable.TAB_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        List<InvatationInfo> invationInfos = new ArrayList<>();

        while (cursor.moveToNext()) {
            InvatationInfo invitationInfo = new InvatationInfo();

            invitationInfo.setReason(cursor.getString(cursor.getColumnIndex(InviteTable.COL_REASON))); //原因
            invitationInfo.setStatus(int2InviteStatus(cursor.getInt(cursor.getColumnIndex(InviteTable.COL_STATUS))));// 状态

            String groupId = cursor.getString(cursor.getColumnIndex(InviteTable.COL_GROUP_HXID));
            if (groupId == null) {//联系人的邀请信息
                UserInfo userInfo = new UserInfo();
                userInfo.setHxid(cursor.getString(cursor.getColumnIndex(InviteTable.COL_USER_HXID)));
                userInfo.setName(cursor.getString(cursor.getColumnIndex(InviteTable.COL_USER_NAME)));
                userInfo.setNick(cursor.getString(cursor.getColumnIndex(InviteTable.COL_USER_NAME)));

                invitationInfo.setUser(userInfo);
            } else {//群组的邀请信息
                GroupInfo groupInfo = new GroupInfo();

                groupInfo.setGroupId(cursor.getString(cursor.getColumnIndex(InviteTable.COL_GROUP_HXID)));
                groupInfo.setGroupName(cursor.getString(cursor.getColumnIndex(InviteTable.COL_GROUP_NAME)));
                groupInfo.setInvatePerson(cursor.getString(cursor.getColumnIndex(InviteTable.COL_USER_HXID)));

                invitationInfo.setGroup(groupInfo);
            }
            //添加本次循环的邀请信息到总的集合当中
            invationInfos.add(invitationInfo);
        }

        //关闭资源
        cursor.close();
        //返回数据
        return invationInfos;
    }

    //将int类型状态转换为邀请的状态
    private InvatationInfo.InvitationStatus int2InviteStatus(int intStatus) {

        if (intStatus == InvatationInfo.InvitationStatus.NEW_INVITE.ordinal()) {
            return InvatationInfo.InvitationStatus.NEW_INVITE;
        }
        if (intStatus == InvatationInfo.InvitationStatus.INVITE_ACCEPT.ordinal()) {
            return InvatationInfo.InvitationStatus.INVITE_ACCEPT;
        }
        if (intStatus == InvatationInfo.InvitationStatus.INVITE_ACCEPT_BY_PEER.ordinal()) {
            return InvatationInfo.InvitationStatus.INVITE_ACCEPT_BY_PEER;
        }

        if (intStatus == InvatationInfo.InvitationStatus.NEW_GROUP_INVITE.ordinal()) {
            return InvatationInfo.InvitationStatus.NEW_GROUP_INVITE;
        }
        if (intStatus == InvatationInfo.InvitationStatus.NEW_GROUP_APPLICATION.ordinal()) {
            return InvatationInfo.InvitationStatus.NEW_GROUP_APPLICATION;
        }

        if (intStatus == InvatationInfo.InvitationStatus.GROUP_INVITE_ACCEPTED.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_INVITE_ACCEPTED;
        }
        if (intStatus == InvatationInfo.InvitationStatus.GROUP_APPLICATION_ACCEPTED.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_APPLICATION_ACCEPTED;
        }
        if (intStatus == InvatationInfo.InvitationStatus.GROUP_INVITE_DECLINED.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_INVITE_DECLINED;
        }
        if (intStatus == InvatationInfo.InvitationStatus.GROUP_APPLICATION_DECLINED.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_APPLICATION_DECLINED;
        }
        if (intStatus == InvatationInfo.InvitationStatus.GROUP_ACCEPT_INVITE.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_ACCEPT_INVITE;
        }
        if (intStatus == InvatationInfo.InvitationStatus.GROUP_ACCEPT_APPLICATION.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_ACCEPT_APPLICATION;
        }
        if (intStatus == InvatationInfo.InvitationStatus.GROUP_REJECT_APPLICATION.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_REJECT_APPLICATION;
        }
        if (intStatus == InvatationInfo.InvitationStatus.GROUP_REJECT_INVITE.ordinal()) {
            return InvatationInfo.InvitationStatus.GROUP_REJECT_INVITE;
        }
        return null;
    }

    //删除邀请状态
    public void removeInvitation(String hxId) {
        if (hxId == null) {
            return;
        }

        //获取数据库连接
        SQLiteDatabase db = mhelper.getReadableDatabase();
        //执行删除语句
        db.delete(InviteTable.TAB_NAME, InviteTable.COL_USER_HXID + "=?", new String[]{hxId});
    }

    //更新邀请状态
    public void updateInvitationStatus(InvatationInfo.InvitationStatus invitationStatus, String hxId) {
        if (hxId == null) {
            return;
        }

        //获取数据库连接
        SQLiteDatabase db = mhelper.getReadableDatabase();
        //执行更新语句
        ContentValues values = new ContentValues();

        values.put(InviteTable.COL_STATUS, invitationStatus.ordinal());
        db.update(InviteTable.TAB_NAME, values, InviteTable.COL_USER_HXID + "=?", new String[]{hxId});
    }
}
