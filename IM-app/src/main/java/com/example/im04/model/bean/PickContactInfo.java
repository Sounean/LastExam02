package com.example.im04.model.bean;

// 选择联系人的bean类(是创建群聊时，选择联系人专用的bean类。比原先的UserInfo多了个是否选中的选项)
public class PickContactInfo {
    private UserInfo user;  // 联系人
    private boolean isChecked;  // 联系人是否被选中的标记

    public PickContactInfo(UserInfo user, boolean isChecked) {
        this.user = user;
        this.isChecked = isChecked;
    }

    public PickContactInfo() {
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
