package com.example.im04;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.im04.model.Model;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.EaseUI;

public class IMApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化EaseUI
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);   // 设置需要同意后才能接受邀请。
        options.setAutoAcceptGroupInvitation(false);    // 设置需要同意后才能接受群邀请。
        options.setAutoTransferMessageAttachments(true);    // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载.
        options.setAutoDownloadThumbnail(true); // 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联.
        EaseUI.getInstance().init(this , options);
        EMClient.getInstance().setDebugMode(true);  // 在做打包混淆时，关闭debug模式，避免消耗不必要的资源.
        EaseUI.getInstance().init(this, options);
        Log.e("???????", "已经进行初始化");
        // 初始化模型数据
        Model.getInstance().init(this);

        // 初始化全局上下文
        mContext = this;
    }


    //获取全局上下文对象
    public static Context getGlobalApplication(){
        return mContext;
    }

    private static Toast toast;
    /**
     * [简化Toast]
     */
    @SuppressLint("ShowToast")
    public static void setShortToast(Context context, String msg) {
        Looper.prepare();

        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else{
            toast.setText(msg);
        }
        toast.show();
        Looper.loop();
    }



}
