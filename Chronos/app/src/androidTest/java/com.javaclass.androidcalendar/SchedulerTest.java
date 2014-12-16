package com.javaclass.androidcalendar;

import junit.framework.TestCase;

import java.util.Calendar;

/**
 * Created by Jeonghwan on 2014-12-16.
 */
public class SchedulerTest extends TestCase {
    public void Test() {
        Scheduler scheduler = new Scheduler();

        CalendarEvent e1 = new CalendarEvent(
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 9, 10),
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 12, 0),
                CalendarEvent.REPEAT_NO);

        CalendarEvent e2 = new CalendarEvent(
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 15, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 17, 0),
                CalendarEvent.REPEAT_NO);

        CalendarEvent e3 = new CalendarEvent(
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 16, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 17, 30),
                CalendarEvent.REPEAT_NO);

        // register a new event
        boolean result = scheduler.Add(e1);
        assertEquals(result, true);

        // register another event
        result = scheduler.Add(e2);
        assertEquals(result, true);

        // try to add a new event, but this have to fail
        // since 'e3' overlap 'e2'
        result = scheduler.Add(e3);
        assertEquals(result, false);

        // remove all event on 25th, december
        // we have 2 events on that day, 'n' must be 2
        int n = scheduler.Remove(
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 0, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 23, 59));
        assertEquals(n, 2);
    }
}
