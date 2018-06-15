package com.bphvcg.apho.Models;

public class RecentlyChat {
    private String uidSender; // uid người gửi để biết là mình gửi hay người khác gửi
    private String uidRecentlyChat; // uid người đang nói chuyện để lấy avt
    private String nameRecentlychat;
    private String content;
    private String type;
    private String lastMessageTime;
    private boolean seen; // lấy trạng thái xem tin nhắn hay chưa

    public RecentlyChat(){

    }

    public RecentlyChat(String uidSender, String uidRecentlyChat, String nameRecentlychat, String content,
                        String type, String lastMessageTime, boolean seen) {
        this.uidSender = uidSender;
        this.uidRecentlyChat = uidRecentlyChat;
        this.nameRecentlychat = nameRecentlychat;
        this.content = content;
        this.type = type;
        this.lastMessageTime = lastMessageTime;
        this.seen = seen;
    }

    public String getUidSender() {
        return uidSender;
    }

    public void setUidSender(String uidSender) {
        this.uidSender = uidSender;
    }

    public String getUidRecentlyChat() {
        return uidRecentlyChat;
    }

    public void setUidRecentlyChat(String uidRecentlyChat) {
        this.uidRecentlyChat = uidRecentlyChat;
    }

    public String getNameRecentlychat() {
        return nameRecentlychat;
    }

    public void setNameRecentlychat(String nameRecentlychat) {
        this.nameRecentlychat = nameRecentlychat;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
