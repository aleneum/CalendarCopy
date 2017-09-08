package com.github.aleneum.calendarcopy;

import java.text.SimpleDateFormat;
import android.provider.CalendarContract.Events;



public class EventSummary {
    public String[] info;

    // for sorting events by data
    public long dtstart;

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
        dtstart = Long.parseLong(info[FIELDS.DTSTART.ordinal()]);
    }

    @Override
    public String toString() {
        return new SimpleDateFormat("dd.MM. (HH:mm) ").format(dtstart)
                + info[FIELDS.TITLE.ordinal()];
    }

    public long getId() {
        return Long.parseLong(info[FIELDS.ID.ordinal()]);
    }
}
