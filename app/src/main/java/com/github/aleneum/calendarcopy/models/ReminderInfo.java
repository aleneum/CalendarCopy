package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Reminders;

public class ReminderInfo {

    public static final String[] PROJECTION = {
            Reminders._ID, Reminders.EVENT_ID, Reminders.MINUTES, Reminders.METHOD};


    public final static int EVENT_ID = 1;

}
