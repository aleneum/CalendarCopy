package com.github.aleneum.calendarcopy;

import android.provider.CalendarContract.Events;



public class EventSummary {
    public String[] info;

    public static final String[] PROJECTION = {
            Events._ID, Events.TITLE, Events.DTSTART
    };

    public static enum FIELDS {
        ID, TITLE, DTSTART
    }

    public EventSummary() {
        info = new String[]{};
    }

    public EventSummary(String[] anInfo) {
        setInfo(anInfo);
    }

    public void setInfo(String[] anInfo) {
        info = anInfo;
    }

    // TODO: Add formatted Date String to the output
    @Override
    public String toString() {
        return info[FIELDS.TITLE.ordinal()];
    }

    public long getId() {
        return Long.parseLong(info[FIELDS.ID.ordinal()]);
    }
}
