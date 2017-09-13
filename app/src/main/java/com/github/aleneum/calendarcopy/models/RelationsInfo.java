package com.github.aleneum.calendarcopy.models;

import com.github.aleneum.calendarcopy.RelationDatabaseHelper;

public class RelationsInfo extends ModelBase {

    public static final String[] PROJECTION = {
            RelationDatabaseHelper._ID,
            RelationDatabaseHelper.SOURCE_EVENT,
            RelationDatabaseHelper.SOURCE_CALENDAR,
            RelationDatabaseHelper.TARGET_EVENT,
            RelationDatabaseHelper.TARGET_CALENDAR,
    };

    public static final int SOURCE_EVENT = 1;
    private static final int SOURCE_CALENDAR = 2;
    public static final int TARGET_EVENT = 3;
    private static final int TARGET_CALENDAR = 4;

    public RelationsInfo(String[] anInfo) {
        super(anInfo);
    }

    @Override
    public String toString(){
        return getSourceEvent() + " -> " + getTargetEvent();
    }

    public long getSourceEvent() {
        return Long.parseLong(info[SOURCE_EVENT]);
    }

    public long getSourceCalendar() {
        return Long.parseLong(info[SOURCE_CALENDAR]);
    }

    public long getTargetEvent() {
        return Long.parseLong(info[TARGET_EVENT]);
    }

    public long getTargetCalendar() { return Long.parseLong(info[TARGET_CALENDAR]);}
}