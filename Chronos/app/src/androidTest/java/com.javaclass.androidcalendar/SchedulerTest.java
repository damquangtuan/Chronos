package com.javaclass.androidcalendar;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jeonghwan on 2014-12-16.
 */
public class SchedulerTest extends TestCase {
    @Test
    public void Test()  throws Exception {
        Scheduler scheduler = new Scheduler();

        CalendarEvent e1 = new CalendarEvent(
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 9, 10),
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 12, 0),
                CalendarEvent.REPEAT_NO
        );

        CalendarEvent e2 = new CalendarEvent(
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 15, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 17, 0),
                CalendarEvent.REPEAT_NO
        );

        CalendarEvent e3 = new CalendarEvent(
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 16, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 17, 30),
                CalendarEvent.REPEAT_NO
        );

        CalendarEvent e4 = new CalendarEvent(
                CalendarEvent.date(2014, Calendar.DECEMBER, 24, 1, 23),
                CalendarEvent.date(2014, Calendar.DECEMBER, 24, 2, 45),
                CalendarEvent.REPEAT_DAY
        );

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
                CalendarEvent.date(2014, Calendar.DECEMBER, 25, 23, 59)
        );
        assertEquals(n, 2);

        // add a new event
        result = scheduler.Add(e4);
        assertEquals(result, true);

        // find events with the given search range below
        // 'list' has to null since no such events exits in 'scheduler'
        ArrayList<CalendarEvent> list = scheduler.Query(
                CalendarEvent.date(2014, Calendar.DECEMBER, 23, 0, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 24, 0, 10)
        );
        assertEquals(list == null, true);

        // 'list' has not to be null and has only one event 'e4'
        list = scheduler.Query(
                CalendarEvent.date(2014, Calendar.DECEMBER, 23, 0, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 26, 0, 0)
        );
        assertEquals(list != null, true);
        assertEquals(list.get(0).equals(e4), true);

        // search with a different range
        // this have to be the same as above since 'e4' occurs at every day
        list = scheduler.Query(
                CalendarEvent.date(2014, Calendar.DECEMBER, 27, 1, 23),
                CalendarEvent.date(2014, Calendar.DECEMBER, 27, 2, 45)
        );
        assertEquals(list != null, true);
        assertEquals(list.get(0).equals(e4), true);

        // remove 'e4' from 'scheduler'
        // since 'e4' occurs at every day and
        // the given search range covers the time at which 'e4' takes place
        n = scheduler.Remove(
                CalendarEvent.date(2014, Calendar.DECEMBER, 31, 0, 0),
                CalendarEvent.date(2014, Calendar.DECEMBER, 31, 23, 59)
        );
        assertEquals(n, 1);
    }
}
