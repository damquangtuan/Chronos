package com.javaclass.androidcalendar;

import java.util.Calendar;

/**
 * Created by Jeonghwan on 2014-12-16.
 */
public class CalendarEvent {

    public static final int REPEAT_NO = 0;
    public static final int REPEAT_DAY = 1;
    public static final int REPEAT_WEEK = 2;

    private Calendar start, end;
    private int repeat;

    public CalendarEvent() {
        start = end = null;
        repeat = REPEAT_NO;
    }

    public CalendarEvent(Calendar start, Calendar end, int repeat) {
        this.start = start;
        this.end = end;
        this.repeat = repeat;
    }

    public int repeat() { return repeat; }
    public Calendar start() { return start; }
    public Calendar end() { return end; }

    public static Calendar date(int year, int month, int date, int hour, int min) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, date, hour, min);
        return c;
    }
}
