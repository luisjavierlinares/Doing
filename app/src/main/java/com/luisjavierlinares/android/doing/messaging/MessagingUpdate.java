package com.luisjavierlinares.android.doing.messaging;

/**
 * Created by Luis on 15/05/2017.
 */

public class MessagingUpdate {

    public static enum UpdateType {USER, AVATAR, DOINGS, LIKES, COMMENTARIES};

    private String id;
    private UpdateType type;
    private Long lastUpdate;

    public MessagingUpdate(String id, UpdateType type, Long lastUpdate) {
        this.id = id;
        this.type = type;
        this.lastUpdate = lastUpdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UpdateType getType() {
        return type;
    }

    public void setType(UpdateType type) {
        this.type = type;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
