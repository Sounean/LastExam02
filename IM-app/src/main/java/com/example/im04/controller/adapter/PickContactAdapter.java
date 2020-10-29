package com.example.im04.controller.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.baidu.platform.comapi.map.A;
import com.example.im04.R;
import com.example.im04.model.bean.PickContactInfo;

import java.util.ArrayList;
import java.util.List;


// 选择联系人的页面适配器
public class PickContactAdapter extends BaseAdapter {
    private Context mContext;
    private List<PickContactInfo> mPicks = new ArrayList<>();
    private List<String> mExistMembers = new ArrayList<>();// 保存群中已存在的成员集合

    public PickContactAdapter(Context context , List<PickContactInfo> picks , List<String> existMembers) {
        mContext = context;

        if (picks != null && picks.size() >= 0){// 这是好的编码习惯，先判断数据空不为空。
            mPicks.clear();
            mPicks.addAll(picks);
        }

        // 加载已经存在的成员集合
        mExistMembers.clear();
        mExistMembers.addAll(existMembers);
    }

    @Override
    public int getCount() {
        return mPicks == null?0 :mPicks.size();
    }

    @Override
    public Object getItem(int position) {
        return mPicks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 创建或获取ViewHolder
        ViewHolder holder = null;

        if (convertView == null){
            holder = new ViewHolder();

            convertView = View.inflate(mContext , R.layout.item_pick , null);
            holder.cb = convertView.findViewById(R.id.cb_pick);
            holder.tv_name = convertView.findViewById(R.id.tv_pick_name);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 获取当前item数据
        PickContactInfo pickContactInfo = mPicks.get(position);

        // 显示数据
        holder.tv_name.setText(pickContactInfo.getUser().getName());
        holder.cb.setChecked(pickContactInfo.isChecked());

        //判断    mExistMembers是从环信服务器上获取到的群成员， 如果好友列表中有，则设置成默认选中并且不可更改的
        if(mExistMembers.contains(pickContactInfo.getUser().getHxid())){
            holder.cb.setChecked(true);
            pickContactInfo.setChecked(true);
        }
        return convertView;
    }

    // 返回当前被选中的联系人
    public List<String> getPickContacts() {

        List<String> picks = new ArrayList<>();

        for (PickContactInfo pick:mPicks){// 遍历所有的联系人，然后去判断该联系人是否被选中了。
            if (pick.isChecked()){
                picks.add(pick.getUser().getName());
            }
        }
        return picks;
    }

    private class ViewHolder{
        private CheckBox cb;
        private TextView tv_name;
    }
}
