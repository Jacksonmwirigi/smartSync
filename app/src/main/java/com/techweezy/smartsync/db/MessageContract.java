package com.techweezy.smartsync.db;

import android.provider.BaseColumns;

public final class MessageContract {

    private MessageContract() {}

    /* Inner class that defines the table contents */
    public static class TxtMessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "message_table";
        public static final String COLUMN_SENDER_PHONE = "sender";
        public static final String COLUMN_TEXT_MESSAGE = "message";
        public static final String COLUMN_SMS_ID = "sms_id";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_SYNC_STATUS = "sync_status";


        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TxtMessageEntry.TABLE_NAME + " (" +
                        TxtMessageEntry._ID + " INTEGER PRIMARY KEY," +
                        TxtMessageEntry.COLUMN_SENDER_PHONE + " TEXT," +
                        TxtMessageEntry.COLUMN_TEXT_MESSAGE + " TEXT," +
                        TxtMessageEntry.COLUMN_SMS_ID + " TEXT," +
                        TxtMessageEntry.COLUMN_SYNC_STATUS + " TEXT," +
                        TxtMessageEntry.COLUMN_TIMESTAMP + " TEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TxtMessageEntry.TABLE_NAME;

    }


}
