package com.github.aleneum.calendarcopy;

import android.provider.CalendarContract.Reminders;

public class ReminderInfo {
    public String[] info;

    public static final String[] PROJECTION = {
            Reminders._ID, Reminders.EVENT_ID, Reminders.MINUTES, Reminders.METHOD};

    public static enum FIELDS { ID, EVENT_ID, MINUTES, METHOD };

    public ReminderInfo(String[] anInfo) {
        setInfo(anInfo);
    }

    public void setInfo(String[] anInfo) {
        info = anInfo;
    }

    @Override
    public String toString(){
        return info[FIELDS.EVENT_ID.ordinal()] + "->" + info[FIELDS.MINUTES.ordinal()];
    }

}
