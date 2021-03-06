package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Events;

/**
 * Represents a calendar event. This does not include reoccuring events (so far).
 */
public class EventInfo {

    /** Projection of all columns of an event entry. Required for queries. */
    public static final String[] PROJECTION = {
            Events._ID, Events.CALENDAR_ID, Events.ORGANIZER, Events.TITLE, Events.EVENT_LOCATION,
            Events.DESCRIPTION, Events.EVENT_COLOR, Events.DTSTART, Events.DTEND,
            Events.EVENT_TIMEZONE, Events.EVENT_END_TIMEZONE, Events.DURATION, Events.ALL_DAY,
            Events.RRULE, Events.RDATE, Events.EXRULE, Events.EXDATE, Events.ORIGINAL_ID,
            Events.ORIGINAL_SYNC_ID, Events.ORIGINAL_INSTANCE_TIME, Events.ORIGINAL_ALL_DAY,
            Events.ACCESS_LEVEL, Events.AVAILABILITY, Events.GUESTS_CAN_MODIFY,
            Events.GUESTS_CAN_INVITE_OTHERS, Events.GUESTS_CAN_SEE_GUESTS, Events.CUSTOM_APP_PACKAGE,
            Events.CUSTOM_APP_URI, Events.UID_2445
    };

    /** Position of ID in the projection */
    public final static int ID = 0;
    /** Position of the associated calendar id in the projection */
    public final static int CALENDAR_ID = 1;
}
