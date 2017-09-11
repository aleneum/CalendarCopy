package com.github.aleneum.calendarcopy;

import android.provider.CalendarContract.Events;

import java.util.ArrayList;
import java.util.List;

public class EventInfo extends EventSummary {

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

    public static enum FIELDS {
        ID, CALENDAR_ID, ORGANIZER, TITLE, EVENT_LOCATION, DESCRIPTION, EVENT_COLOR, DTSTART, DTEND,
        EVENT_TIMEZONE, EVENT_END_TIMEZONE, DURATION, ALL_DAY, RRULE, RDATE, EXRULE, EXDATE,
        ORIGINAL_ID, ORIGINAL_SYNC_ID, ORIGINAL_INSTANCE_TIME, ORIGINAL_ALL_DAY, ACCESS_LEVEL,
        AVAILABILITY, GUESTS_CAN_MODIFY, GUESTS_CAN_INVITE_OTHERS, GUESTS_CAN_SEE_GUESTS,
        CUSTOM_APP_PACKAGE, CUSTOM_APP_URI, UID_2445
    }

    public EventInfo(String[] anInfo) {
        super(anInfo);
    }
}
