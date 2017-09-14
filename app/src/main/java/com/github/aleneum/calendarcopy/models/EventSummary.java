package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Instances;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents a calendar event derived from {@link Instances}. In contrast to {@link EventInfo},
 * this representation is limited to required fields only.
 * Additionally, it is extended with relational information.
 */
public class EventSummary extends ModelBase {

    /** ID of the original event if this event is a copy. No parent is represented by -1. */
    public long parentId;
    /** ID of the original calendar if the parent event. No calendar is represented by -1. */
    public long parentCalendarId;
    /** IDs all known copies of this event. */
    public final List<Long> childrenEventIds;
    /** Calendars of the copies of this event in the same order as IDs. */
    public final List<Long> childrenCalendarIds;

    /** (Reduced) Projection of all columns of an event entry. Required for queries. */
    public static final String[] PROJECTION = {
            Instances.EVENT_ID, Instances.TITLE, Instances.BEGIN,
    };

    private static final int ID = 0;
    private static final int TITLE = 1;
    private static final int BEGIN = 2;

    // for sorting events by data
    private long begin;

    /**
     * Constructor. Data array in the projection fields' order.
     * @param anInfo  Data array in the projection fields' order.
     */
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
        begin = Long.parseLong(info[BEGIN]);
    }

    @Override
    public String toString() {
        return new SimpleDateFormat("dd.MM. (HH:mm) ", Locale.getDefault()).format(begin)
                + info[TITLE];
    }

    /**
     * Returns the event's ID as a long. The ID will be parsed on the fly.
     * @return event ID
     */
    public long getId() {
        return Long.parseLong(info[ID]);
    }

    /**
     * Returns the event's ID as a String.
     * @return event ID
     */
    public String getIdString() {
        return info[ID];
    }
}
