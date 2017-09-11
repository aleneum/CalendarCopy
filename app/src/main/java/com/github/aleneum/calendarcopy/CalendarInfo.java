package com.github.aleneum.calendarcopy;

/**
 * Created by alneuman on 06.09.17.
 */

public class CalendarInfo {
    private long id;
    private String name;
    private String account;
    public int color;

    public CalendarInfo(long anId, String aName, String anAccout, int aColor) {
        id = anId;
        name = aName;
        account = anAccout;
        color = aColor;
    }

    @Override
    public String toString() {
        return name + "(" + account + ")";
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getAccount() {return account; }

}
