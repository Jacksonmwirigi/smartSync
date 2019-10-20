package com.techweezy.smartsync.utils;

import android.telephony.SmsMessage;

import com.techweezy.smartsync.model.Message;

public interface MessageListener {
    void getReceivedMessage(SmsMessage message);
}
