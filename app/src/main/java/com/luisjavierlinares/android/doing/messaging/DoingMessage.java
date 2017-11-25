package com.luisjavierlinares.android.doing.messaging;

import java.util.List;

/**
 * Created by Luis on 08/05/2017.
 */

public class DoingMessage {

    private String id;
    private String user;
    private String action;
    private String text;
    private Long timestamp;
    private List<DoingReceiverMessage> receivers;

    public DoingMessage() {};

    public DoingMessage(String id, String user, String action, String text, Long timestamp, List<DoingReceiverMessage> receivers) {
        this.id = id;
        this.user = user;
        this.action = action;
        this.text = text;
        this.timestamp = timestamp;
        this.receivers = receivers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public List<DoingReceiverMessage> getReceivers() { return receivers;}

    public void setReceivers(List<DoingReceiverMessage> receivers) {
        this.receivers = receivers;
    }

}
