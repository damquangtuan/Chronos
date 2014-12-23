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

    private MySQLiteHelper db = null;

    public Scheduler(MySQLiteHelper db) {
        this.db = db;
    }

    public void deleteEvent(Event event) {
        db.deleteEvent(event);
    }
    public void updateEvent(Event event) {
        db.updateEvent(event);
    }

    public void addEvent(Event event) {
        db.addEvent(event);
    }
    /**
     *
     * @param eventID
     * @return
     */
    public Event getEvent(long eventID) {
        Event event = db.getEvent(eventID);
        return event;
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
                            Event newEvent = new Event(); //declare a new event and set value for that
                            newEvent.setEventId(event.getEventId());
                            newEvent.setName(event.getName());
                            newEvent.setDescription(event.getDescription());
                            newEvent.setLocation(event.getLocation());
                            newEvent.setColor(event.getColor());
                            newEvent.setRepeat(event.getRepeat());

                            newEvent.setStart(date.getTime()); //set date again
                            int hour = endCal.get(endCal.HOUR_OF_DAY); //get hour
                            int minute = endCal.get(endCal.MINUTE); //get minute
                            date.setHours(hour);//set hour of end date
                            date.setMinutes(minute);//set minute of end date
                            newEvent.setEnd(date.getTime()); //set end time which including end hour of event
                            returnList.add(newEvent); //add to event list
                        }
                    } else if (event.getRepeat() == 2) {
                        //first go through day by day from start date to end date
                        for (Date date = startCal.getTime(); !startCal.after(endCal); startCal.add(Calendar.DATE, 7),
                                date = startCal.getTime()) {
                            Event newEvent = new Event(); //declare a new event and set value for that
                            newEvent.setEventId(event.getEventId());
                            newEvent.setName(event.getName());
                            newEvent.setDescription(event.getDescription());
                            newEvent.setLocation(event.getLocation());
                            newEvent.setColor(event.getColor());
                            newEvent.setRepeat(event.getRepeat());

                            newEvent.setStart(date.getTime()); //set date again
                            int hour = endCal.get(endCal.HOUR_OF_DAY); //get hour
                            int minute = endCal.get(endCal.MINUTE); //get minute
                            date.setHours(hour);//set hour of end date
                            date.setMinutes(minute);//set minute of end date
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
