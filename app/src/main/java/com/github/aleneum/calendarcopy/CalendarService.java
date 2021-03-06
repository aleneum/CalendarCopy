package com.github.aleneum.calendarcopy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.provider.CalendarContract.Instances;
import android.util.Log;

import com.github.aleneum.calendarcopy.models.AttendeeInfo;
import com.github.aleneum.calendarcopy.models.CalendarInfo;
import com.github.aleneum.calendarcopy.models.EventInfo;
import com.github.aleneum.calendarcopy.models.EventSummary;
import com.github.aleneum.calendarcopy.models.RelationsInfo;
import com.github.aleneum.calendarcopy.models.ReminderInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


class CalendarService {

    final Activity activity;
    List<EventSummary> events;
    long sourceCalendarId;
    long targetCalendarId;

    private final SQLiteDatabase relations;
    private final RelationDatabaseHelper dbHelper;
    private LinkedHashMap<Long, CalendarInfo> calendars;


    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.

    private final static List<String> blacklist = Arrays.asList(
            EventInfo.PROJECTION[EventInfo.ID],
            AttendeeInfo.PROJECTION[AttendeeInfo.EVENT_ID],
            ReminderInfo.PROJECTION[ReminderInfo.EVENT_ID]
    );

    private static final String[] EVENT_ID_PROJECTION = {
            Events._ID
    };

    private static final String DEBUG_TAG = "ccopy.CalendarService";
    private static final int MAX_EVENT_ENTRIES = 30;

    List<CalendarInfo> getCalendarInfo() {
        return new ArrayList<>(calendars.values());
    }


    CalendarService(Activity anActivity) {
        activity = anActivity;
        calendars = new LinkedHashMap<>();
        events = new ArrayList<>();
        dbHelper = new RelationDatabaseHelper(anActivity);
        relations = dbHelper.getWritableDatabase();
    }

    void getCalendars() throws SecurityException {
        Log.d(DEBUG_TAG, "queryCalendar() called...");
        ContentResolver cr = activity.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        Cursor cur = cr.query(uri, CalendarInfo.PROJECTION, null, null, null);
        assert cur != null;
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
        Calendar endTime = (Calendar) beginTime.clone();
        endTime.add(Calendar.MONTH, 1);

        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, beginTime.getTimeInMillis());
        ContentUris.appendId(builder, endTime.getTimeInMillis());


        ContentResolver cr = this.activity.getContentResolver();
        String selection = " (" + Instances.CALENDAR_ID + " = ?)";

        String[] selectionArgs = new String[]{Long.toString(sourceCalendarId)};

        Log.d(DEBUG_TAG, "Event query args:" + Arrays.toString(selectionArgs));

        Cursor cur = cr.query(builder.build(), EventSummary.PROJECTION, selection, selectionArgs,
                Instances.BEGIN + " LIMIT " + MAX_EVENT_ENTRIES);
        assert cur != null;
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

            events.add(summary);
        }
    }

    /** One event can have more than one instance */
    List<EventSummary> getEventsById(long eventId) {
        List<EventSummary> result = new ArrayList<>();
        for (EventSummary summary: events) {
            if (summary.getId() == eventId) {
                result.add(summary);
            }
        }
        return result;
    }

    long copyEvent(long eventId) throws SecurityException {
        if (getEventsById(eventId).get(0).childrenCalendarIds.contains(targetCalendarId)) {
            Log.d(DEBUG_TAG, "Event already in target calendar. Skip!");
            return -1;
        }

        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Events._ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(eventId)};

        Cursor cur = cr.query(Events.CONTENT_URI, EventInfo.PROJECTION, selection, selectionArgs, null);
        assert cur != null;
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " to copy to calendar with ID " + targetCalendarId);

        // Result size should be 0 or 1
        if (cur.moveToNext()) {
            ContentValues values = new ContentValues();
            long previousCalendarId = cur.getLong(EventInfo.CALENDAR_ID);
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
                return -1;
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
            }

            if (! copyAttendees(eventId, targetEventId)) {
                Log.w(DEBUG_TAG, "Creating attendees  failed!");
            }

            if (! copyReminders(eventId, targetEventId)){
                Log.w(DEBUG_TAG, "Creating reminders  failed!");
            }

            return targetEventId;
        }
        cur.close();
        return -1;
    }

    private boolean copyAttendees(long sourceEventId, long targetEventId) throws SecurityException {
        boolean success = true;
        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Attendees.EVENT_ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(sourceEventId)};

        Cursor cur = cr.query(Attendees.CONTENT_URI, AttendeeInfo.PROJECTION,
                selection, selectionArgs, null);
        assert cur != null;
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " Attendees to copy to event " + targetEventId);


        try {
            if (cur.moveToNext()) {
                ContentValues values = new ContentValues();
                int idx = 0;
                for (String field : AttendeeInfo.PROJECTION) {
                    if (!blacklist.contains(field)) {
                        Log.d(DEBUG_TAG, "Copy attendee field " + field);
                        values.put(field, cur.getString(idx));
                    }
                    idx++;
                }
                Log.d(DEBUG_TAG, "Set new event id");
                values.put(AttendeeInfo.PROJECTION[AttendeeInfo.EVENT_ID],
                        targetEventId);
                Log.d(DEBUG_TAG, "Insert new attendee " + values.toString());
                Uri insertUri = cr.insert(Attendees.CONTENT_URI, values);
                if (insertUri == null) {
                    throw new IllegalArgumentException("Attendee creation failed!");
                }
            }
        } catch (IllegalArgumentException err) {
            Log.d(DEBUG_TAG, err.toString());
            success = false;
        }
        cur.close();
        return success;
    }

    private boolean copyReminders(long sourceEventId, long targetEventId) throws SecurityException {
        boolean success = true;
        ContentResolver cr = this.activity.getContentResolver();
        String selection = "(" + Reminders.EVENT_ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(sourceEventId)};

        Cursor cur = cr.query(Reminders.CONTENT_URI, ReminderInfo.PROJECTION,
                selection, selectionArgs, null);
        assert cur != null;
        Log.d(DEBUG_TAG, "Got " + cur.getCount() + " Reminders to copy to event " + targetEventId);

        try {
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
                values.put(ReminderInfo.PROJECTION[ReminderInfo.EVENT_ID], targetEventId);
                Log.d(DEBUG_TAG, "Insert new reminder " + values.toString());
                Uri insertUri = cr.insert(Reminders.CONTENT_URI, values);
                if (insertUri == null) {
                    throw new IllegalArgumentException("Reminder creation failed!");
                }
            }
        } catch (IllegalArgumentException err) {
            Log.w(DEBUG_TAG, err.toString());
            success = false;
        }
        cur.close();
        return success;
    }

    private String[] cursorToArray(Cursor cur) {
        List<String> result = new ArrayList<>();

        for (int i=0; i < cur.getColumnCount(); i++) {
            result.add(cur.getString(i));
        }
        return result.toArray(new String[cur.getColumnCount()]);
    }

    List<Long> getCalendarIds() {
        return new ArrayList<>(calendars.keySet());
    }

    CalendarInfo getCalendarById(long id) {
        for (CalendarInfo calendar: calendars.values()) {
            if (calendar.getId() == id) {return calendar;}
        }
        return null;

    }

    void clearDatabase() throws SecurityException {
        Cursor cur = relations.query(RelationDatabaseHelper.TABLE_NAME, RelationsInfo.PROJECTION, null, null,
                null, null, null);
        Set<Long> eventIds = new HashSet<>();
        while(cur.moveToNext()) {
            eventIds.add(cur.getLong(RelationsInfo.SOURCE_EVENT));
            eventIds.add(cur.getLong(RelationsInfo.TARGET_EVENT));
        }
        cur.close();

        String selection = Events._ID + "=?";
        String deleteSelection = "((" + RelationDatabaseHelper.SOURCE_EVENT + "=?) OR (" +
                RelationDatabaseHelper.TARGET_EVENT + "=?))";
        ContentResolver cr = this.activity.getContentResolver();
        for (long event: eventIds) {
            String[] args = new String[]{Long.toString(event)};
            cur = cr.query(Events.CONTENT_URI, EVENT_ID_PROJECTION, selection, args, null);
            assert cur != null;
            if (cur.getCount() == 0) {
                String[] deleteArgs = new String[]{Long.toString(event), Long.toString(event)};
                relations.delete(RelationDatabaseHelper.TABLE_NAME, deleteSelection, deleteArgs);
            }
            cur.close();
        }
    }

    void stop() {
        dbHelper.close();
    }
}