package com.github.aleneum.calendarcopy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.provider.CalendarContract.Events;



public class EventSummary {

    // for sorting events by data
    public long dtstart;
    public String[] info;
    public long parentId;
    public long parentCalendarId;
    public List<Long> childrenEventIds;
    public List<Long> childrenCalendarIds;

    public static final String[] PROJECTION = {
            Events._ID, Events.TITLE, Events.DTSTART
    };

    public enum FIELDS {
        ID, TITLE, DTSTART
    }

    public EventSummary(String[] anInfo) {
        setInfo(anInfo);
        parentId = -1;
        parentCalendarId = -1;
        childrenEventIds = new ArrayList<>();
        childrenCalendarIds =  new ArrayList<>();
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

    public String getIdString() {
        return info[FIELDS.ID.ordinal()];
    }
}
