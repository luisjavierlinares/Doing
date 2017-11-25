package com.luisjavierlinares.android.doing.messaging;

/**
 * Created by Luis on 09/05/2017.
 */

public class CommentaryMessage {

    private String id;
    private String doing;
    private String sender;
    private String senderFriendName;
    private String senderFriendCode;
    private String receiver;
    private String text;
    private Long timestamp;

    public CommentaryMessage() {};

    public CommentaryMessage(String id, String doing, String sender, String senderFriendName, String senderFriendCode,
                             String receiver, String text, Long timestamp) {
        this.id = id;
        this.doing = doing;
        this.text = text;
        this.sender = sender;
        this.senderFriendName = senderFriendName;
        this.senderFriendCode = senderFriendCode;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoing() {
        return doing;
    }

    public void setDoing(String doing) {
        this.doing = doing;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderFriendName() {
        return senderFriendName;
    }

    public void setSenderFriendName(String senderFriendName) {
        this.senderFriendName = senderFriendName;
    }

    public String getSenderFriendCode() {
        return senderFriendCode;
    }

    public void setSenderFriendCode(String senderFriendCode) {
        this.senderFriendCode = senderFriendCode;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
