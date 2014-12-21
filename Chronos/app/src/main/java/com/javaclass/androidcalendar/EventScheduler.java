package com.javaclass.androidcalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import Database.SQLite.MySQLiteHelper;
import Database.SQLite.model.Event;

/**
 * Created by Jeonghwan on 2014-12-21.
 */
public class EventScheduler {

    private MySQLiteHelper db = null;

    public EventScheduler(MySQLiteHelper db) {
        this.db = db;
    }

    public boolean addEvent(Event event) {
        Event e;
        List<Event> eventList = db.getAllEvents();
        List<Event> selected = new ArrayList<Event>();
        Iterator<Event> i = eventList.iterator();

        while(i.hasNext()) {
            e = i.next();
            if(_CheckBetween(e, event.getStart(), event.getEnd())) {
                selected.add(e);
            }
        }

        // this event overlaps other events
        if(eventList.isEmpty())
            return false;

        db.addEvent(event);

        return true;
    }

    public void deleteAll() {
        db.deleteAll();
    }


    List<Event> getEvents(int year, int month) {
        List<Event> eventList = db.getAllEvents();
        List<Event> returnList = new ArrayList<Event>();
        Iterator<Event> i = eventList.iterator();

        Calendar start = Calendar.getInstance();
        start.set(year, month, 1);

        Calendar end = Calendar.getInstance();
        end.set(year, month, start.getActualMaximum(Calendar.DAY_OF_MONTH));

        Event e = null;
        while(i.hasNext()) {
            e = i.next();
            if(_CheckBetween(e, start.getTimeInMillis(), end.getTimeInMillis())) {
                returnList.add(e);
            }
        }

        return returnList;
    }

    List<Event> getEvents(int year, int month, int day) {
        List<Event> eventList = db.getAllEvents();
        List<Event> returnList = new ArrayList<Event>();
        Iterator<Event> i = eventList.iterator();

        Calendar start = Calendar.getInstance();
        start.set(year, month, day, 0, 0);

        Calendar end = Calendar.getInstance();
        start.set(year, month, day, 23, 59);

        Event e = null;
        while(i.hasNext()) {
            e = i.next();
            if(_CheckBetween(e, start.getTimeInMillis(), end.getTimeInMillis())) {
                returnList.add(e);
            }
        }

        return returnList;
    }

    // check out a given event is between 'start' and 'end'
    private boolean _CheckBetween(Event e, long start, long end) {
        // no repeat
        if(e.getRepeat() == Event.REPEAT_NO) {
            return (e.getStart() >= start && e.getStart() <= end) ||
                    (e.getEnd() >= start && e.getEnd() <= end);
        }


        // algorithm
        // (s1, e1) : search range, (s2, e2) : event range
        // dt : time in milliseconds
        //
        // find some integer i satisfying two equations below
        // s1 <= s2 + dt * i < e1 or s1 < e2 + dt * i <= e1
        else if(start <= e.getStart()) {
            double dt = 1;

            switch(e.getRepeat()) {
                case Event.REPEAT_DAY: // every day
                    dt = (long)86400000; // a day in milliseconds
                    break;

                case Event.REPEAT_WEEK: // every week
                    dt = (long)604800000; // 7 days in milliseconds
                    break;

                default: // error
                    dt = 0;
                    break;
            }

            double lower = (double)(start - e.getStart()) / dt;
            double upper = (double)(end - e.getStart()) / dt;
            int m = (int)Math.ceil(lower);
            int n = (int)Math.floor(upper);

            if(m <= n)
                return true;

            lower = (double)(start - e.getEnd()) / dt;
            upper = (double)(end - e.getEnd()) / dt;
            m = (int)Math.ceil(lower);
            n = (int)Math.floor(upper);

            return (m <= n);
        }
        else
            return false;
    }

}
