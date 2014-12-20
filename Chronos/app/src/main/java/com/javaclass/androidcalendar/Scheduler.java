package com.javaclass.androidcalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import Database.SQLite.MySQLiteHelper;
import Database.SQLite.model.Event;

/**
 * Created by Jeonghwan on 2014-12-12.
 */
public class Scheduler {

    private ArrayList<CalendarEvent> event_list; // hold events registered

    private MySQLiteHelper db = null;

    public Scheduler() {
        event_list = new ArrayList<CalendarEvent>();
    }

    public Scheduler(MySQLiteHelper db) {
        this.db = db;
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

    /**
     * Get events with given year and month
     * @param year
     * @param month
     * @return
     */
    public List<Event> getEvents(int year, int month) {
        List<Event> eventList = db.getAllEvents(); // get all event from database
        List<Event> returnList = new ArrayList<Event>();
        for(int i = 0; i <= eventList.size() - 1; i++) {
            Event event = eventList.get(i); //get event at index i
            //start date
            long start = event.getStart(); //get start in long milisecond
            Calendar startCal = Calendar.getInstance(); //calendar for start
            startCal.setTimeInMillis(start); //set calendar in milisecond
            long startYear = startCal.get(startCal.YEAR);
            long startMonth = startCal.get(startCal.MONTH);//get start month
            //end date
            long end = event.getEnd(); //get end in long milisecond
            Calendar endCal = Calendar.getInstance(); //calendar for end
            endCal.setTimeInMillis(end); // set calendar in milisecond
            long endYear = endCal.get(endCal.YEAR); //get end year
            long endMonth = endCal.get(endCal.MONTH); //get end month

            if((startYear <= year) && (endYear >= year)) {
                if ((startMonth <= month) && (endMonth >= month)) {
                    // handle with repeat event
                    if(event.getRepeat() == 1) {
                        //first go through day by day from start date to end date
                        for (Date date = startCal.getTime(); !startCal.after(endCal); startCal.add(Calendar.DATE, 1),
                                date = startCal.getTime()) {
                            Event newEvent = new Event();
                            newEvent = event; // clone from existing event
                            newEvent.setStart(date.getTime()); //set date again
                            int hour = endCal.get(endCal.HOUR_OF_DAY); //get hour
                            int minute = endCal.get(endCal.MINUTE); //get minute
                            date.setHours(hour);//set hour
                            date.setMinutes(minute);//set minute
                            newEvent.setEnd(date.getTime()); //set end time which including end hour of event
                            returnList.add(newEvent); //add to event list
                        }
                    } else {
                        returnList.add(event);
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * Get events with given year, month and day
     * @param year
     * @param month
     * @param day
     * @return
     */
    public List<Event> getEvents(int year, int month, int day) {
        List<Event> eventList = db.getAllEvents(); // get all event from database
        List<Event> returnList = new ArrayList<Event>();
        for(int i = 0; i <= eventList.size() - 1; i++) {
            Event event = eventList.get(i); //get event at index i
            //start date
            long start = event.getStart(); //get start in long milisecond
            Calendar startCal = Calendar.getInstance(); //calendar for start
            startCal.setTimeInMillis(start); //set calendar in milisecond
            long startYear = startCal.get(startCal.YEAR);
            long startMonth = startCal.get(startCal.MONTH);//get start month
            long startDay = startCal.get(startCal.DAY_OF_MONTH); //get start day
            //end date
            long end = event.getEnd(); //get end in long milisecond
            Calendar endCal = Calendar.getInstance(); //calendar for end
            endCal.setTimeInMillis(end); // set calendar in milisecond
            long endYear = endCal.get(endCal.YEAR); //get end year
            long endMonth = endCal.get(endCal.MONTH); //get end month
            long endDay = endCal.get(endCal.DAY_OF_MONTH); //get end day

            if((startYear <= year) && (endYear >= year)) {
                if ((startMonth <= month) && (endMonth >= month))
                    if ((startDay <= day) && (endDay >= day))
                        returnList.add(event); // add event to list
            }
        }
        return returnList;
    }
}
