package com.javaclass.androidcalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by Jeonghwan on 2014-12-12.
 */
public class Scheduler {

    private ArrayList<CalendarEvent> event_list; // hold events registered

    public Scheduler() {
        event_list = new ArrayList<CalendarEvent>();
    }

    public int Size() {
        return event_list.size();
    }

    // register a new event
    // this function will fail if the event overlaps others
    public boolean Add(CalendarEvent event) {
        ArrayList<CalendarEvent> selected_events = Query(event.start(), event.end());

        // this event overlaps other events
        if(selected_events != null)
            return false;

        event_list.add(event);

        return true;
    }

    // remove a event from the event list with a given event id
    // if succeed, return true
    /*public boolean Remove(int eventID) {
        Event e;
        Iterator<Event> i = event_list.iterator();
        while(i.hasNext()) {
            e = i.next();

            if(e.getEventID() == eventID) {
                i.remove();
                return true;
            }
        }

        return false;
    }*/

    // remove events ranging 'start' to 'end' from the event list
    // return the number of events removed
    public int Remove(Calendar start, Calendar end) {
        int n = 0;

        CalendarEvent e;
        Iterator<CalendarEvent> i = event_list.iterator();
        while(i.hasNext()) {
            e = i.next();

            if(_CheckBetween(e, start, end)) {
                i.remove();
                ++n;
            }
        }

        return n;
    }

    // remove all events in the list
    // returns the number of events
    public int RemoveAll() {
        int n = event_list.size();
        event_list.clear();

        return n;
    }

    // find events satisfying a given condition
    // if no such events, null will be returned
    public ArrayList<CalendarEvent> Query(Calendar start, Calendar end) {
        ArrayList<CalendarEvent> selected_events = new ArrayList<CalendarEvent>();

        CalendarEvent e;
        Iterator<CalendarEvent> i = event_list.iterator();
        while(i.hasNext()) {
            e = i.next();

            if(_CheckBetween(e, start, end))
                selected_events.add(e);
        }

        return !selected_events.isEmpty() ? selected_events : null;
    }

    // check out a given event is between 'start' and 'end'
    private boolean _CheckBetween(CalendarEvent e, Calendar start, Calendar end) {
        // no repeat
        if(e.repeat() == CalendarEvent.REPEAT_NO) {
            return (e.start().after(start) && e.start().before(end)) ||
                    (e.end().after(start) && e.end().before(end));
        }


        // algorithm
        // (s1, e1) : search range, (s2, e2) : event range
        // dt : time in milliseconds
        //
        // find some integer i satisfying two equations below
        // s1 <= s2 + dt * i < e1 or s1 < e2 + dt * i <= e1
        else if(!e.start().after(end)) {
            double dt = 1;

            switch(e.repeat()) {
                case CalendarEvent.REPEAT_DAY: // every day
                    dt = (long)86400000; // a day in milliseconds
                    break;

                case CalendarEvent.REPEAT_WEEK: // every week
                    dt = (long)604800000; // 7 days in milliseconds
                    break;

                default: // error
                    dt = 0;
                    break;
            }

            double lower = (double)(start.getTimeInMillis() - e.start().getTimeInMillis()) / dt;
            double upper = (double)(end.getTimeInMillis() - e.start().getTimeInMillis()) / dt;
            int m = (int)Math.ceil(lower);
            int n = (int)Math.floor(upper);

            if(m <= n)
                return true;

            lower = (double)(start.getTimeInMillis() - e.end().getTimeInMillis()) / dt;
            upper = (double)(end.getTimeInMillis() - e.end().getTimeInMillis()) / dt;
            m = (int)Math.ceil(lower);
            n = (int)Math.floor(upper);

            return (m <= n);
        }
        else
            return false;
    }
}
