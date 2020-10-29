package com.example.im04.model;


import android.content.Context;

import com.example.im04.model.bean.UserInfo;
import com.example.im04.model.dao.UserAccountDao;
import com.example.im04.model.dao.UserAccountTable;
import com.example.im04.model.db.DBManager;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
* 数据模型全局层
* 数据层和模型层的交互必须走Model，这样的好处是方便二次编写。下方model层更改后不影响上面。这也是MVC的思想。
* 得是单例模式
* */
public class Model {
    private Context mContext;
    private ExecutorService executors = Executors.newCachedThreadPool();    // newCachedThreadPool是一种线程池模式。
    UserAccountDao userAccountDao;
    DBManager dbManager;

    // 创建对象
    private static Model model = new Model();

    // 私有化构造
    private Model(){    // 因为是单例，所以构造方法要给私有化。

    }

    // 获取单例对象
    public static Model getInstance(){

        return model;
    }

    // 初始化得方法
    public void init(Context context){
        mContext = context;

        // 创建用户账号数据库的操作类对象
        userAccountDao = new UserAccountDao(mContext);

        //  开启全局监听
        EventListener eventListener = new EventListener(mContext);
    }


    // 获取全局线程池对象
    public ExecutorService getGlobalThreadPool(){
        return executors;
    }

    //  loginSuccess()是预留的一个方法，用户登录成功后的处理方法
    public void loginSuccess(UserInfo account) {
        //校验
        if(account == null){
            return;
        }
        if(dbManager != null){
            dbManager.close();
        }
        dbManager = new DBManager(mContext,account.getName());
    }

    // 获取管理者
    public DBManager getDBManager(){
        return dbManager;
    }

    // 获取用户账号数据库的操作类对象操作类
    public UserAccountDao getUserAccountDao(){
        return userAccountDao;
    }
}
