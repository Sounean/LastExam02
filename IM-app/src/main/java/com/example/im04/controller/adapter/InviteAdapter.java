package com.example.im04.controller.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.im04.R;
import com.example.im04.model.bean.InvatationInfo;
import com.example.im04.model.bean.UserInfo;
import com.example.im04.utils.Constant;

import java.util.ArrayList;
import java.util.List;


// 邀请信息列表页面的适配器
public class InviteAdapter extends BaseAdapter {
    private Context mContext;
    private List<InvatationInfo> mInvatationInfos = new ArrayList<>();
    private OnInviteListener mOnInviteListener;


    // 刷新数据的方法
    public void refresh(List<InvatationInfo> invatationInfos){
        if (invatationInfos != null && invatationInfos.size() >= 0){
            mInvatationInfos.clear();
            mInvatationInfos.addAll(invatationInfos);
            notifyDataSetChanged();
        }
    }

    public InviteAdapter(Context context , OnInviteListener onInviteListener) {
        mContext = context;
        mOnInviteListener = onInviteListener;
    }

    @Override
    public int getCount() {
        return mInvatationInfos == null ?0 :mInvatationInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mInvatationInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1 获取或创建viewHolder
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();

            convertView = View.inflate(mContext , R.layout.item_invite , null);

            holder.name = convertView.findViewById(R.id.tv_invite_name);
            holder.reason = convertView.findViewById(R.id.tv_invite_reason);

            holder.accept = convertView.findViewById(R.id.bt_invite_accept);
            holder.reject = convertView.findViewById(R.id.bt_invite_reject);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        //  2获取当前item数据
        InvatationInfo invationInfo = mInvatationInfos.get(position);

        //  3显示当前item数据
        UserInfo user = invationInfo.getUser();

        if (user != null){// 联系人
            // 拿到名称的展示
            holder.name.setText(invationInfo.getUser().getName());

            holder.accept.setVisibility(View.GONE);
            holder.reject.setVisibility(View.GONE);

            // 原因
            if (invationInfo.getStatus() == InvatationInfo.InvitationStatus.NEW_INVITE){// 新的邀请
               if (invationInfo.getReason() == null){
                    holder.reason.setText("添加好友");
                }else {
                    holder.reason.setText(invationInfo.getReason());
                }
                holder.accept.setVisibility(View.VISIBLE);
                holder.reject.setVisibility(View.VISIBLE);

            }else if (invationInfo.getStatus() == InvatationInfo.InvitationStatus.INVITE_ACCEPT){// 接受邀请
                if (invationInfo.getReason() == null){
                    holder.reason.setText("接受邀请");
                }else {
                    holder.reason.setText(invationInfo.getReason());
                }
            }else  if (invationInfo.getStatus() == InvatationInfo.InvitationStatus.INVITE_ACCEPT_BY_PEER){// 邀请被接受
                if (invationInfo.getReason() == null){
                    holder.reason.setText("邀请被接受");
                }else {
                    holder.reason.setText(invationInfo.getReason());
                }
            }

            //  按钮的处理
            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnInviteListener.onAccept(invationInfo);
                }
            });

            //  拒绝按钮的点击事件处理
            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnInviteListener.onReject(invationInfo);
                }
            });
        }else {// 群组
            // 显示名称
            holder.name.setText(invationInfo.getGroup().getInvatePerson());//显示邀请人的名称

            // 群接收和拒绝按钮先都设置成不显示
            holder.accept.setVisibility(View.GONE);
            holder.reject.setVisibility(View.GONE);
            Log.e("??????", invationInfo.getStatus()+"" );
            Log.e("??????", invationInfo.getUser()+"");   // 这里竟然显示null？？？？？？？

            // 显示原因
            switch (invationInfo.getStatus()){
                //你的群申请已经被接受
                case GROUP_APPLICATION_ACCEPTED:
                    holder.reason.setText("你的群申请已经被接受");
                    break;
                //你的群邀请已经被接受
                case GROUP_INVITE_ACCEPTED:
                    holder.reason.setText("你的群邀请已经被接受");
                    break;
                //你的群申请已经被拒绝
                case GROUP_APPLICATION_DECLINED:
                    holder.reason.setText("你的群申请已经被拒绝");
                    break;
                //你的群邀请已经被拒绝
                case GROUP_INVITE_DECLINED:
                    holder.reason.setText("你的群邀请已经被拒绝");
                    break;
                //你收到了群邀请
                case NEW_GROUP_INVITE:
                    holder.accept.setVisibility(View.VISIBLE);
                    holder.reject.setVisibility(View.VISIBLE);
                    //接受邀请
                    holder.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnInviteListener.onInviteAccept(invationInfo);// 具体的点击事件注册成接口
                        }
                    });
                    //拒绝邀请
                    holder.reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnInviteListener.onInviteReject(invationInfo);
                        }
                    });
                    holder.reason.setText("你收到了群邀请");
                    break;

                //你收到了群申请
                case NEW_GROUP_APPLICATION:
                    holder.accept.setVisibility(View.VISIBLE);
                    holder.reject.setVisibility(View.VISIBLE);
                    //接受申请
                    holder.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnInviteListener.onApplicationAccept(invationInfo);
                        }
                    });
                    //拒绝申请
                    holder.reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnInviteListener.onApplicationReject(invationInfo);
                        }
                    });
                    holder.reason.setText("你收到了群申请");
                    break;
                //你接受了群邀请
                case GROUP_ACCEPT_INVITE:
                    holder.reason.setText("你接受了群邀请");
                    break;
                //你批准了群申请
                case GROUP_ACCEPT_APPLICATION:
                    holder.reason.setText("你批准了群申请");
                    break;
//                //你拒绝了群邀请
//                case GROUP_REJECT_INVITE:
//                    holder.reason.setText("你拒绝了群邀请");
//                    break;
//                //你拒绝了群申请
//                case GROUP_REJECT_APPLICATION:
//                    holder.reason.setText("你拒绝了群申请");
//                    break;
            }

        }

        //  4返回view
        return convertView;
    }

    private class ViewHolder{
        private TextView name;
        private TextView reason;

        private Button accept;
        private Button reject;
    }

    public interface OnInviteListener{
        // 联系人接收按钮的点击事件
        void onAccept(InvatationInfo invatationInfo);
        // 联系人拒绝按钮的点击事件
        void onReject(InvatationInfo invatationInfo);

        // 接受邀请按钮处理
        void onInviteAccept(InvatationInfo invatationInfo);
        // 拒绝邀请按钮处理
        void onInviteReject(InvatationInfo invatationInfo);

        // 接受申请按钮处理
        void onApplicationAccept(InvatationInfo invatationInfo);
        // 拒绝申请按钮处理
        void onApplicationReject(InvatationInfo invatationInfo);
    }

}
