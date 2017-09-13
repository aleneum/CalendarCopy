package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Reminders;

/**
 * Represents a single reminder for an event.
 */
public class ReminderInfo {

    /** Projection of all columns of a reminder entry. Required for queries. */
    public static final String[] PROJECTION = {
            Reminders._ID, Reminders.EVENT_ID, Reminders.MINUTES, Reminders.METHOD};

    /** Position of the {@link android.provider.CalendarContract.Reminders#EVENT_ID} */
    public final static int EVENT_ID = 1;

}
