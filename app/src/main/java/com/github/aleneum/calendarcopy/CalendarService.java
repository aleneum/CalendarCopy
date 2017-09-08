package com.github.aleneum.calendarcopy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Reminders;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CalendarService {

    public Activity activity;
    public List<CalendarInfo> calendars;
    public List<EventSummary> events;

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDAR_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME          // 2
    };

    private static List<String> blacklist = Arrays.asList(
            EventInfo.PROJECTION[EventInfo.FIELDS.ID.ordinal()],
            EventInfo.PROJECTION[EventInfo.FIELDS.CALENDAR_ID.ordinal()],
            AttendeeInfo.PROJECTION[AttendeeInfo.FIELDS.EVENT_ID.ordinal()],
            ReminderInfo.PROJECTION[ReminderInfo.FIELDS.EVENT_ID.ordinal()]
    );

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;

    private static final String DEBUG_TAG = "ccopy.CalendarService";

    public static Comparator<EventSummary> eventSummaryComparator = new Comparator<EventSummary>() {
        @Override
        public int compare(EventSummary e1, EventSummary e2) {
            if (e1.dtstart > e1.dtstart)
                return 1;
            if (e1.dtstart < e2.dtstart)
                return -1;
            return 0;
        }
    };


    public CalendarService(Activity anActivity) {
        activity = anActivity;
        calendars = new ArrayList<>();
    }

    public void getCalendars() throws SecurityException {
        Log.d(DEBUG_TAG, "queryCalendar() called...");
        ContentResolver cr = activity.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        Cursor cur = cr.query(uri, CALENDAR_PROJECTION, null, null, null);
        // Use the cursor to step through the returned records
        Log.d(DEBUG_TAG, "Received " + cur.getCount() + " results");
        while (cur.moveToNext()) {
            CalendarInfo info = new CalendarInfo(cur.getLong(PROJECTION_ID_INDEX),
                    cur.getString(PROJECTION_DISPLAY_NAME_INDEX),
                    cur.getString(PROJECTION_ACCOUNT_NAME_INDEX));
            Log.d(DEBUG_TAG, "CalendarInfo: " + info.toString());
            calendars.add(info);
        }
    }

    void getEvents(long calenderID) throws SecurityException {
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Calendar.HOUR_OF_DAY, 0);
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.DAY_OF_MONTH, 14);

        ContentResolver cr = this.activity.getContentResolver();
        ContentValues values = new ContentValues();
        String selection = "((" + Events.DTSTART + " >= ?) AND "
                + "(" + Events.DTEND + " <= ?) AND (" + Events.CALENDAR_ID + " = ?))";

        String[] selectionArgs = new String[]{Long.toString(beginTime.getTimeInMillis()),
                Long.toString(endTime.getTimeInMillis()), Long.toString(calenderID)};

        Log.d(DEBUG_TAG, "Event query args:" + Arrays.toString(selectionArgs));

        Cursor cur = cr.query(Events.CONTENT_URI, EventSummary.PROJECTION, selection, selectionArgs, null);
        Log.d(DEBUG_TAG, "Received " + cur.getCount() + " events");
        events = new ArrayList<>();
        while (cur.moveToNext()) {
            events.add(new EventSummary(cursorToArray(cur)));
        }
        Collections.sort(events, eventSummaryComparator);
    }

    // TODO: Also copy Attendees and Reminders (Instances is not writable)
    public boolean copyEvent(long eventId, long targetCalendar) throws SecurityException {
        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Events._ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(eventId)};

        Cursor cur = cr.query(Events.CONTENT_URI, EventInfo.PROJECTION, selection, selectionArgs, null);
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " to copy to calendar with ID " + targetCalendar);

        // Result size should be 0 or 1
        if (cur.moveToNext()) {
            ContentValues values = new ContentValues();
            long previousCalendarId = cur.getLong(EventInfo.FIELDS.CALENDAR_ID.ordinal());
            int idx = 0;
            for(String field: EventInfo.PROJECTION) {
                if (! blacklist.contains(field)) {
                    Log.d(DEBUG_TAG, "Copy event field " + field);
                    values.put(field, cur.getString(idx));
                }
                idx++;
            }
            Log.d(DEBUG_TAG, "Set new calendar id");
            values.put(Events.CALENDAR_ID, targetCalendar);
            Log.d(DEBUG_TAG, "Check organizer");
            if (values.get(Events.ORGANIZER).equals(getCalendarById(previousCalendarId).getAccount())) {
                Log.d(DEBUG_TAG, "Change organizer to target calendar's account");
                values.put(Events.ORGANIZER, getCalendarById(targetCalendar).getAccount());
            }

            Log.d(DEBUG_TAG, "Insert new event " + values.toString());
            Uri insertUri = cr.insert(Events.CONTENT_URI, values);
            if (insertUri == null) {
                Log.w(DEBUG_TAG, "Event creation failed!");
                return false;
            }
            long targetEventId = Long.parseLong(insertUri.getLastPathSegment());
            if (copyAttendees(eventId, targetEventId)) { copyReminders(eventId, targetEventId); }
            else { return false; }
            return copyReminders(eventId, targetEventId);
        }
        return false;
    }

    private boolean copyAttendees(long sourceEventId, long targetEventId) throws SecurityException {
        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Attendees.EVENT_ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(sourceEventId)};

        Cursor cur = cr.query(Attendees.CONTENT_URI, AttendeeInfo.PROJECTION,
                selection, selectionArgs, null);
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " Attendees to copy to event " + targetEventId);

        if (cur.moveToNext()) {
            ContentValues values = new ContentValues();
            int idx = 0;
            for(String field: AttendeeInfo.PROJECTION) {
                if (!blacklist.contains(field)) {
                    Log.d(DEBUG_TAG, "Copy attendee field " + field);
                    values.put(field, cur.getString(idx));
                }
                idx++;
            }
            Log.d(DEBUG_TAG, "Set new event id");
            values.put(AttendeeInfo.PROJECTION[AttendeeInfo.FIELDS.EVENT_ID.ordinal()], targetEventId);
            Log.d(DEBUG_TAG, "Insert new attendee " + values.toString());
            Uri insertUri = cr.insert(Attendees.CONTENT_URI, values);
            if (insertUri == null) {
                Log.w(DEBUG_TAG, "Attendee creation failed!");
                return false;
            }
        }
        return true;
    }

    private boolean copyReminders(long sourceEventId, long targetEventId) throws SecurityException {
        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Reminders.EVENT_ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(sourceEventId)};

        Cursor cur = cr.query(Reminders.CONTENT_URI, ReminderInfo.PROJECTION,
                selection, selectionArgs, null);
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " Reminders to copy to event " + targetEventId);

        if (cur.moveToNext()) {
            ContentValues values = new ContentValues();
            int idx = 0;
            for(String field: ReminderInfo.PROJECTION) {
                if (!blacklist.contains(field)) {
                    Log.d(DEBUG_TAG, "Copy reminder field " + field);
                    values.put(field, cur.getString(idx));
                }
                idx++;
            }
            Log.d(DEBUG_TAG, "Set new event id");
            values.put(ReminderInfo.PROJECTION[ReminderInfo.FIELDS.EVENT_ID.ordinal()], targetEventId);
            Log.d(DEBUG_TAG, "Insert new reminder " + values.toString());
            Uri insertUri = cr.insert(Reminders.CONTENT_URI, values);
            if (insertUri == null) {
                Log.w(DEBUG_TAG, "Reminder creation failed!");
                return false;
            }
        }
        return true;
    }

    private String[] cursorToArray(Cursor cur) {
        List<String> result = new ArrayList<>();

        for (int i=0; i < cur.getColumnCount(); i++) {
            result.add(cur.getString(i));
        }
        return result.toArray(new String[cur.getColumnCount()]);
    }

    public List<String> getCalendarNames() {
        List<String> names = new ArrayList<>();
        for (CalendarInfo calendar: calendars) {
            names.add(calendar.getName());
        }
        return names;
    }

    public List<Long> getCalendarIds() {
        List<Long> ids = new ArrayList<>();
        for (CalendarInfo calendar: calendars) {
            ids.add(calendar.getId());
        }
        return ids;
    }

    public CalendarInfo getCalendarById(long id) {
        for (CalendarInfo calendar: calendars) {
            if (calendar.getId() == id) {return calendar;}
        }
        return null;

    }
}