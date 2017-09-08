package com.github.aleneum.calendarcopy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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
    public List<String> calendarNames;

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] CALENDAR_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME          // 2
    };

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
        calendarNames = new ArrayList<>();
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
            calendarNames.add(info.toString());
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

    public void copyEvent(long eventID, long targetCalendar) throws SecurityException {
        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Events._ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(eventID)};

        Cursor cur = cr.query(Events.CONTENT_URI, EventInfo.PROJECTION, selection, selectionArgs, null);
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " to copy to calendar with ID " + targetCalendar);

        // Result size should be 0 or 1

        List<String> blacklist = Arrays.asList(
                EventInfo.PROJECTION[EventInfo.FIELDS.ID.ordinal()],
                EventInfo.PROJECTION[EventInfo.FIELDS.CALENDAR_ID.ordinal()]
        );

        if (cur.moveToNext()) {
            ContentValues values = new ContentValues();
            int idx = 0;
            for(String field: EventInfo.PROJECTION) {
                if (! blacklist.contains(field)) {
                    Log.d(DEBUG_TAG, "Copy field " + field);
                    values.put(field, cur.getString(idx));
                }
                idx++;
            }
            Log.d(DEBUG_TAG, "Set new calendar id");
            values.put(EventInfo.PROJECTION[EventInfo.FIELDS.CALENDAR_ID.ordinal()], targetCalendar);
            Log.d(DEBUG_TAG, "Insert new event " + values.toString());
            Uri insertUri = cr.insert(Events.CONTENT_URI, values);
            if (insertUri == null) {
                Log.w(DEBUG_TAG, "Event creation failed!");
            }

        }
    }

    private String[] cursorToArray(Cursor cur) {
        List<String> result = new ArrayList<>();

        for (int i=0; i < cur.getColumnCount(); i++) {
            result.add(cur.getString(i));
        }
        return result.toArray(new String[cur.getColumnCount()]);
    }
}