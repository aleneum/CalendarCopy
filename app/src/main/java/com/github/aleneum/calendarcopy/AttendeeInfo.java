package com.github.aleneum.calendarcopy;

import android.provider.CalendarContract.Attendees;

/**
 * Created by alneuman on 08.09.17.
 */

public class AttendeeInfo {

    public String[] info;

    public static final String[] PROJECTION = {
            Attendees._ID, Attendees.EVENT_ID, Attendees.ATTENDEE_NAME, Attendees.ATTENDEE_EMAIL,
            Attendees.ATTENDEE_RELATIONSHIP, Attendees.ATTENDEE_TYPE, Attendees.ATTENDEE_STATUS,
            Attendees.ATTENDEE_IDENTITY, Attendees.ATTENDEE_ID_NAMESPACE };

    public static enum FIELDS {
        ID, EVENT_ID, ATTENDEE_NAME, ATTENDEE_EMAIL, ATTENDEE_RELATIONSHIP, ATTENDEE_TYPE,
        ATTENDEE_STATUS, ATTENDEE_IDENTITY, ATTENDEE_ID_NAMESPACE };

    public AttendeeInfo(String[] anInfo) {
        setInfo(anInfo);
    }

    public void setInfo(String[] anInfo) {
        info = anInfo;
    }

    @Override
    public String toString(){
        return info[FIELDS.ATTENDEE_EMAIL.ordinal()];
    }

}
