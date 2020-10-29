package com.example.im04.model.bean;


/*
* 用户账号信息的bean类
* */
public class UserInfo {

    private String name;// 用户名称
    private String hxid;//  环信id（在服务器中的唯一标识）
    private String nick;//  用户的昵称
    private String photo;// 头像

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", hxid='" + hxid + '\'' +
                ", nick='" + nick + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHxid() {
        return hxid;
    }

    public void setHxid(String hxid) {
        this.hxid = hxid;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /*
     * 这里为了方便将用户名称，环信id和用户昵称都用同一个
     * */
    public UserInfo(String name) {
        this.name = name;
        this.hxid = name;
        this.nick = name;
    }

    public UserInfo() {
    }
}
