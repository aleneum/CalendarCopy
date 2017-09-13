package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class EventSummary extends ModelBase {

    // for sorting events by data
    public long parentId;
    public long parentCalendarId;
    public final List<Long> childrenEventIds;
    public final List<Long> childrenCalendarIds;

    private long dtstart;

    public static final String[] PROJECTION = {
            Events._ID, Events.TITLE, Events.DTSTART
    };

    public enum FIELDS {
        ID, TITLE, DTSTART
    }

    public EventSummary(String[] anInfo) {
        super(anInfo);
        parentId = -1;
        parentCalendarId = -1;
        childrenEventIds = new ArrayList<>();
        childrenCalendarIds =  new ArrayList<>();
    }

    @Override
    public void setInfo(String[] anInfo) {
        super.setInfo(anInfo);
        dtstart = Long.parseLong(info[FIELDS.DTSTART.ordinal()]);
    }

    @Override
    public String toString() {
        return new SimpleDateFormat("dd.MM. (HH:mm) ", Locale.getDefault()).format(dtstart)
                + info[FIELDS.TITLE.ordinal()];
    }

    public long getId() {
        return Long.parseLong(info[FIELDS.ID.ordinal()]);
    }

    public String getIdString() {
        return info[FIELDS.ID.ordinal()];
    }
}
