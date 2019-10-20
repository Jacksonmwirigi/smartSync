package com.techweezy.smartsync.model;

import com.google.gson.annotations.SerializedName;
import com.loopj.android.http.RequestParams;

public class Message  {
    @SerializedName("message")
    private String message;
    @SerializedName("sender")
    private String sender;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("sms_id")
    private String sms_id;
    @SerializedName("sync_status")
    private String sync_status;

//    public Message() {
//    }
    public Message(String sender,  String message,
                    String timestamp, String sms_id,
                   String sync_status ) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.sms_id = sms_id;
        this.sync_status = sync_status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSms_id() {
        return sms_id;
    }

    public void setSms_id(String  sms_id) {
        this.sms_id = sms_id;
    }


    public String getSync_status() {
        return sync_status;
    }

    public void setSync_status(String sync_status) {
        this.sync_status = sync_status;
    }
}
