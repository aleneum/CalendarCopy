package com.github.aleneum.calendarcopy;

/**
 * Created by alneuman on 06.09.17.
 */

public class CalendarInfo {
    private long id;
    private String name;
    private String account;

    public CalendarInfo(long anId, String aName, String anAccout) {
        id = anId;
        name = aName;
        account = anAccout;
    }

    @Override
    public String toString() {
        return name + "(" + account + ")";
    }

    public long getId() {
        return id;
    }

}
