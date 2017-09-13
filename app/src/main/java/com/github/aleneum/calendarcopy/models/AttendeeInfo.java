package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Attendees;

/**
 * Represents a single attendee or guest of an event
 */
public class AttendeeInfo {

    /** Projection of all columns of an attendee entry. Required for queries. */
    public static final String[] PROJECTION = {
            Attendees._ID, Attendees.EVENT_ID, Attendees.ATTENDEE_NAME, Attendees.ATTENDEE_EMAIL,
            Attendees.ATTENDEE_RELATIONSHIP, Attendees.ATTENDEE_TYPE, Attendees.ATTENDEE_STATUS,
            Attendees.ATTENDEE_IDENTITY, Attendees.ATTENDEE_ID_NAMESPACE };

    /** Event this attendee is assigned to */
    public final static int EVENT_ID = 1;

}
