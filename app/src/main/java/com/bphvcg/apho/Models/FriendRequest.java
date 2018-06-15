package com.bphvcg.apho.Models;

public class FriendRequest {
    private String uidUserLogin;
    private String uidUserFriend;
    private String status;

    public FriendRequest() {
    }

    public FriendRequest(String uidUserLogin, String uidUserFriend, String status) {
        this.uidUserLogin = uidUserLogin;
        this.uidUserFriend = uidUserFriend;
        this.status = status;
    }

    public String getUidUserLogin() {
        return uidUserLogin;
    }

    public void setUidUserLogin(String uidUserLogin) {
        this.uidUserLogin = uidUserLogin;
    }

    public String getUidUserFriend() {
        return uidUserFriend;
    }

    public void setUidUserFriend(String uidUserFriend) {
        this.uidUserFriend = uidUserFriend;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
