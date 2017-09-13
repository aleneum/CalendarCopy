package com.github.aleneum.calendarcopy.models;

import android.provider.CalendarContract.Calendars;

/**
 * Represents a user's calendar. This representation is limited to fields required by the
 * application.
 */
public class CalendarInfo extends ModelBase {

    /** Projection of all columns of a calendar entry. Required for queries. */
    public static final String[] PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.CALENDAR_COLOR                 // 3
    };

    private static final int ID = 0;
    private static final int ACCOUNT_NAME = 1;
    private static final int CALENDAR_DISPLAY_NAME = 2;
    private static final int CALENDAR_COLOR = 3;

    /**
     * Constructor.
     * @param anInfo  Data array in the projection fields' order.
     */
    public CalendarInfo(String[] anInfo) {
        super(anInfo);
    }


    @Override
    public String toString() {
        return getName() + "(" + getAccount() + ")";
    }

    /**
     * Returns the calendar's unique id.
     * @return calendar ID
     */
    public long getId() { return Long.parseLong(info[ID]); }
    /**
     * Returns the color associated with the calendar.
     * @return int value of the color
     */
    public int getColor() { return Integer.parseInt(info[CALENDAR_COLOR]); }

    /**
     * Returns the calendar's display name.
     * @return calendar display name
     */
    public String getName() { return info[CALENDAR_DISPLAY_NAME]; }
    /**
     * Returns the email adress associated with the calendar. In some cases this might be an
     * application name or a default placeholder.
     * @return account email address
     */
    public String getAccount() {return info[ACCOUNT_NAME]; }

}
