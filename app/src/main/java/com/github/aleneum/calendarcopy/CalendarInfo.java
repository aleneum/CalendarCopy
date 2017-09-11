package com.github.aleneum.calendarcopy;

import android.provider.CalendarContract;

/**
 * Created by alneuman on 06.09.17.
 */

public class CalendarInfo {
    public String[] info;

    public static final String[] PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.CALENDAR_COLOR                 // 3
    };

    public enum FIELDS {
        ID, ACCOUNT_NAME, CALENDAR_DISPLAY_NAME, CALENDAR_COLOR
    }

    public CalendarInfo(String[] anInfo) {
        setInfo(anInfo);
    }

    public void setInfo(String[] anInfo) {
        info = anInfo;
    }

    @Override
    public String toString() {
        return getName() + "(" + getAccount() + ")";
    }

    public long getId() { return Long.parseLong(info[FIELDS.ID.ordinal()]); }
    public int getColor() { return Integer.parseInt(info[FIELDS.CALENDAR_COLOR.ordinal()]); }
    public String getName() { return info[FIELDS.CALENDAR_DISPLAY_NAME.ordinal()]; }
    public String getAccount() {return info[FIELDS.ACCOUNT_NAME.ordinal()]; }

}
