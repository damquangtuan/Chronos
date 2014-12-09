/**
 * Created by damquangtuan on 09/12/2014.
 */
package com.javaclass.androidcalendar;
import java.lang.String;
import java.util.Date;


public class Event {
    int eventID;
    String title;
    String description;
    Date start_time, end_time;
    int repeater;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public int getRepeater() {
        return repeater;
    }

    public void setRepeater(int repeater) {
        this.repeater = repeater;
    }
}
