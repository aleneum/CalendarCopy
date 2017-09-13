package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Attendees;

public class AttendeeInfo {

    public static final String[] PROJECTION = {
            Attendees._ID, Attendees.EVENT_ID, Attendees.ATTENDEE_NAME, Attendees.ATTENDEE_EMAIL,
            Attendees.ATTENDEE_RELATIONSHIP, Attendees.ATTENDEE_TYPE, Attendees.ATTENDEE_STATUS,
            Attendees.ATTENDEE_IDENTITY, Attendees.ATTENDEE_ID_NAMESPACE };

    public final static int EVENT_ID = 1;

//    public enum FIELDS {
//        ID, EVENT_ID, ATTENDEE_NAME, ATTENDEE_EMAIL, ATTENDEE_RELATIONSHIP, ATTENDEE_TYPE,
//        ATTENDEE_STATUS, ATTENDEE_IDENTITY, ATTENDEE_ID_NAMESPACE }

}
