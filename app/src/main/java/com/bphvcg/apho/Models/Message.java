package com.bphvcg.apho.Models;

public class Message {
    private String uidSender;
    private String uidReceiver;
    private String content;
    private boolean image;
    private boolean audio;
    private String timeMessage;
    private boolean lastMessageSeen;

    public Message(){

    }

    public Message(String uidSender, String uidReceiver, String content, boolean image,
                                                boolean audio, String timeMessage, boolean lastMessageSeen) {
        this.uidSender = uidSender;
        this.uidReceiver = uidReceiver;
        this.content = content;
        this.image = image;
        this.audio = audio;
        this.timeMessage = timeMessage;
        this.lastMessageSeen = lastMessageSeen;
    }

    public String getUidSender() {
        return uidSender;
    }

    public void setUidSender(String uidSender) {
        this.uidSender = uidSender;
    }

    public String getUidReceiver() {
        return uidReceiver;
    }

    public void setUidReceiver(String uidReceiver) {
        this.uidReceiver = uidReceiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public String getTimeMessage() {
        return timeMessage;
    }

    public void setTimeMessage(String timeMessage) {
        this.timeMessage = timeMessage;
    }

    public boolean isLastMessageSeen() {
        return lastMessageSeen;
    }

    public void setLastMessageSeen(boolean lastMessageSeen) {
        this.lastMessageSeen = lastMessageSeen;
    }
}
