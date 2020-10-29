package com.example.im04.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;

import com.example.im04.model.dao.ContactTable;
import com.example.im04.model.dao.InviteTable;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context, @Nullable String name) {
        super(context, name , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建联系人的表
        db.execSQL(ContactTable.CREATE_TAB);

        // 创建邀请信息的表
        db.execSQL(InviteTable.CREATE_TAB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
