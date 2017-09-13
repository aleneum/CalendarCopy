package com.github.aleneum.calendarcopy.models;

import com.github.aleneum.calendarcopy.RelationDatabaseHelper;

/**
 * Represents a relation between an original event and ONE copy. This relation is used to
 * keep track of already conducted copy operation.
 */
public class RelationsInfo extends ModelBase {

    /** Projection of all columns of a relation entry. Required for queries. */
    public static final String[] PROJECTION = {
            RelationDatabaseHelper._ID,
            RelationDatabaseHelper.SOURCE_EVENT,
            RelationDatabaseHelper.SOURCE_CALENDAR,
            RelationDatabaseHelper.TARGET_EVENT,
            RelationDatabaseHelper.TARGET_CALENDAR,
    };

    /** Position of {@link RelationDatabaseHelper#SOURCE_EVENT} */
    public static final int SOURCE_EVENT = 1;
    /** Position of {@link RelationDatabaseHelper#SOURCE_CALENDAR} */
    private static final int SOURCE_CALENDAR = 2;
    /** Position of {@link RelationDatabaseHelper#TARGET_EVENT} */
    public static final int TARGET_EVENT = 3;
    /** Position of {@link RelationDatabaseHelper#TARGET_CALENDAR} */
    private static final int TARGET_CALENDAR = 4;

    /**
     * Constructor.
     * @param anInfo  Data array in the projection fields' order.
     */
    public RelationsInfo(String[] anInfo) {
        super(anInfo);
    }

    @Override
    public String toString(){
        return getSourceEvent() + " -> " + getTargetEvent();
    }

    /**
     * Returns the ID of the original event.
     * @return ID of original event
     */
    public long getSourceEvent() {
        return Long.parseLong(info[SOURCE_EVENT]);
    }

    /**
     * Returns the ID of the calendar of the original event.
     * @return ID of original event's calendar
     */
    public long getSourceCalendar() {
        return Long.parseLong(info[SOURCE_CALENDAR]);
    }

    /**
     * Returns the ID of the event copy.
     * @return ID of copied event
     */
    public long getTargetEvent() {
        return Long.parseLong(info[TARGET_EVENT]);
    }

    /**
     * Returns the ID of the event copy calendar.
     * @return ID of the copy's calendar.
     */
    public long getTargetCalendar() { return Long.parseLong(info[TARGET_CALENDAR]);}
}