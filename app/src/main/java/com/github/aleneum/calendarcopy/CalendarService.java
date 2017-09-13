package com.github.aleneum.calendarcopy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


public class CalendarService {

    public Activity activity;
    public List<EventSummary> events;
    public SQLiteDatabase relations;
    public RelationDatabaseHelper dbHelper;
    public long sourceCalendarId;
    public long targetCalendarId;

    private LinkedHashMap<Long, CalendarInfo> calendars;


    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.

    private static List<String> blacklist = Arrays.asList(
            EventInfo.PROJECTION[EventInfo.FIELDS.ID.ordinal()],
            AttendeeInfo.PROJECTION[AttendeeInfo.FIELDS.EVENT_ID.ordinal()],
            ReminderInfo.PROJECTION[ReminderInfo.FIELDS.EVENT_ID.ordinal()]
    );

    private static final String DEBUG_TAG = "ccopy.CalendarService";
    public static final int MAX_EVENT_ENTRIES = 20;

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

    public CalendarInfo getCalendarInfoById(Long id) {
        return calendars.get(id);
    }

    public CalendarInfo getCalendarInfoByIndex(int pos) {
        return (CalendarInfo) calendars.values().toArray()[pos];
    }

    public List<CalendarInfo> getCalendarInfos() {
        return new ArrayList<>(calendars.values());
    }


    public CalendarService(Activity anActivity) {
        activity = anActivity;
        calendars = new LinkedHashMap<>();
        events = new ArrayList<>();
        dbHelper = new RelationDatabaseHelper(anActivity);
        relations = dbHelper.getWritableDatabase();
    }

    public void getCalendars() throws SecurityException {
        Log.d(DEBUG_TAG, "queryCalendar() called...");
        ContentResolver cr = activity.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        Cursor cur = cr.query(uri, CalendarInfo.PROJECTION, null, null, null);
        // Use the cursor to step through the returned records
        Log.d(DEBUG_TAG, "Received " + cur.getCount() + " results");
        calendars = new LinkedHashMap<>();
        while (cur.moveToNext()) {
            CalendarInfo info = new CalendarInfo(cursorToArray(cur));
            calendars.put(info.getId(), info);
        }
    }

    void getEvents() throws SecurityException {
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Calendar.HOUR_OF_DAY, 0);

        ContentResolver cr = this.activity.getContentResolver();
        String selection = "((" + Events.DTSTART + " >= ?) AND "
                + " (" + Events.CALENDAR_ID + " = ?))";

        String[] selectionArgs = new String[]{Long.toString(beginTime.getTimeInMillis()),
                Long.toString(sourceCalendarId)};

        Log.d(DEBUG_TAG, "Event query args:" + Arrays.toString(selectionArgs));

        Cursor cur = cr.query(Events.CONTENT_URI, EventSummary.PROJECTION, selection, selectionArgs,
                Events.DTSTART + " LIMIT " + MAX_EVENT_ENTRIES);
        Log.d(DEBUG_TAG, "Received " + cur.getCount() + " events");
        events = new ArrayList<>();
        while (cur.moveToNext()) {
            EventSummary summary = new EventSummary(cursorToArray(cur));

            String parentSelection = "(" + RelationDatabaseHelper.TARGET_EVENT + " = ?)";
            String[] eventArgs = new String[]{summary.getIdString()};
            Cursor parentCur = relations.query(RelationDatabaseHelper.TABLE_NAME,
                    RelationsInfo.PROJECTION, parentSelection, eventArgs, null, null, null);

            if (parentCur.moveToNext()) {
                RelationsInfo relations = new RelationsInfo(cursorToArray(parentCur));
                summary.parentId = relations.getSourceEvent();
                summary.parentCalendarId = relations.getSourceCalendar();
            }

            String childrenSelection = "(" + RelationDatabaseHelper.SOURCE_EVENT + " = ?)";
            Cursor childCur = relations.query(RelationDatabaseHelper.TABLE_NAME,
                    RelationsInfo.PROJECTION, childrenSelection, eventArgs, null, null, null);

            while (childCur.moveToNext()) {
                RelationsInfo relations = new RelationsInfo(cursorToArray(childCur));
                summary.childrenEventIds.add(relations.getTargetEvent());
                summary.childrenCalendarIds.add(relations.getTargetCalendar());
            }

//            Log.i(DEBUG_TAG, "Event: " + summary.toString());
//            Log.i(DEBUG_TAG, "  parent: " + summary.parentId);
//            Log.i(DEBUG_TAG, "  children: " + summary.childrenEventIds);

            events.add(summary);
        }
        //Collections.sort(events, eventSummaryComparator);
    }

    EventSummary getEventById(long eventId) {
        for (EventSummary summary: events) {
            if (summary.getId() == eventId) {
                return summary;
            }
        }
        return null;
    }

    public boolean copyEvent(long eventId) throws SecurityException {
        if (getEventById(eventId).childrenCalendarIds.contains(targetCalendarId)) {
            Log.d(DEBUG_TAG, "Event already in target calendar. Skip!");
            return true;
        }

        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Events._ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(eventId)};

        Cursor cur = cr.query(Events.CONTENT_URI, EventInfo.PROJECTION, selection, selectionArgs, null);
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " to copy to calendar with ID " + targetCalendarId);

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
            long sourceCalendar = values.getAsLong(Events.CALENDAR_ID);
            Log.d(DEBUG_TAG, "Set new calendar id");
            values.put(Events.CALENDAR_ID, targetCalendarId);
            Log.d(DEBUG_TAG, "ValuesBefore:" + values.toString());
            Log.d(DEBUG_TAG, "Check organizer");
            if (values.get(Events.ORGANIZER).equals(getCalendarById(previousCalendarId).getAccount())) {
                Log.d(DEBUG_TAG, "Change organizer to target calendar's account");
                String organizer = getCalendarById(targetCalendarId).getAccount();
                if (organizer.contains("@")) {
                    values.put(Events.ORGANIZER, organizer);
                } else {
                    values.remove(Events.ORGANIZER);
                }

            }
            Log.d(DEBUG_TAG, "ValuesAfter:" + values.toString());
            Log.d(DEBUG_TAG, "Insert new event " + values.toString());
            Uri insertUri = cr.insert(Events.CONTENT_URI, values);
            if (insertUri == null) {
                Log.w(DEBUG_TAG, "Event creation failed!");
                return false;
            }
            long targetEventId = Long.parseLong(insertUri.getLastPathSegment());

            ContentValues relation = new ContentValues();
            relation.put(RelationDatabaseHelper.SOURCE_CALENDAR, sourceCalendar);
            relation.put(RelationDatabaseHelper.SOURCE_EVENT, eventId);
            relation.put(RelationDatabaseHelper.TARGET_CALENDAR, targetCalendarId);
            relation.put(RelationDatabaseHelper.TARGET_EVENT, targetEventId);
            long newRowId = relations.insert(RelationDatabaseHelper.TABLE_NAME, null, relation);

            if (newRowId < 0) {
                Log.w(DEBUG_TAG, "Relation creation failed!");
                return false;
            }

            if (! copyAttendees(eventId, targetEventId)) { return false; }
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
        for (CalendarInfo calendar: calendars.values()) {
            names.add(calendar.getName());
        }
        return names;
    }

    public List<Long> getCalendarIds() {
        return new ArrayList<>(calendars.keySet());
    }

    public CalendarInfo getCalendarById(long id) {
        for (CalendarInfo calendar: calendars.values()) {
            if (calendar.getId() == id) {return calendar;}
        }
        return null;

    }

    public void clearDatabase() throws SecurityException {
        Cursor cur = relations.query(RelationDatabaseHelper.TABLE_NAME, RelationsInfo.PROJECTION, null, null,
                null, null, null);
        Set<Long> eventIds = new HashSet<>();
        while(cur.moveToNext()) {
            eventIds.add(cur.getLong(RelationsInfo.FIELDS.SOURCE_EVENT.ordinal()));
            eventIds.add(cur.getLong(RelationsInfo.FIELDS.TARGET_EVENT.ordinal()));
        }

        String selection = Events._ID + "=?";
        String deleteSelection = "((" + RelationDatabaseHelper.SOURCE_EVENT + "=?) OR (" +
                RelationDatabaseHelper.TARGET_EVENT + "=?))";
        ContentResolver cr = this.activity.getContentResolver();
        for (long event: eventIds) {
            String[] args = new String[]{Long.toString(event)};
            cur = cr.query(Events.CONTENT_URI, EventSummary.PROJECTION, selection, args, null);
            if (cur.getCount() == 0) {
                String[] deleteArgs = new String[]{Long.toString(event), Long.toString(event)};
                relations.delete(RelationDatabaseHelper.TABLE_NAME, deleteSelection, deleteArgs);
            }

        }
    }

    public void stop() {
        dbHelper.close();
    }
}