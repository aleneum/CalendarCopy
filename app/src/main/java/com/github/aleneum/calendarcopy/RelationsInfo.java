package com.github.aleneum.calendarcopy;

/**
 * Created by alneuman on 11.09.17.
 */

public class RelationsInfo {
    public String[] info;

    public static final String[] PROJECTION = {
            RelationDatabaseHelper._ID,
            RelationDatabaseHelper.SOURCE_EVENT,
            RelationDatabaseHelper.SOURCE_CALENDAR,
            RelationDatabaseHelper.TARGET_EVENT,
            RelationDatabaseHelper.TARGET_CALENDAR,
    };

    public static enum FIELDS { ID, SOURCE_EVENT, SOURCE_CALENDAR, TARGET_EVENT, TARGET_CALENDAR };

    public RelationsInfo(String[] anInfo) {
        setInfo(anInfo);
    }

    public void setInfo(String[] anInfo) {
        info = anInfo;
    }

    @Override
    public String toString(){
        return getSourceEvent() + " -> " + getTargetEvent();
    }


    public long getSourceEvent() {
        return Long.parseLong(info[FIELDS.SOURCE_EVENT.ordinal()]);
    }

    public long getSourceCalendar() {
        return Long.parseLong(info[FIELDS.SOURCE_CALENDAR.ordinal()]);
    }

    public long getTargetEvent() {
        return Long.parseLong(info[FIELDS.TARGET_EVENT.ordinal()]);
    }

    public Long getTargetCalendar() { return Long.parseLong(info[FIELDS.TARGET_CALENDAR.ordinal()]);}
}