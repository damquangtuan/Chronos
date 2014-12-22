package Database.SQLite.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Event implements Serializable {
	
	private int color;
	private String name;
	private String description;
	private String location;
	private long start;
	private long end;
	private long eventId;
    private int repeat;

	public static final int COLOR_RED = 0;
	public static final int COLOR_BLUE = 1;
	public static final int COLOR_YELLOW = 2;
	public static final int COLOR_PURPLE = 3;
	public static final int COLOR_GREEN = 4;

    // 'repeat' should be one of below
    public static final int REPEAT_NO = 0;
    public static final int REPEAT_DAY = 1;
    public static final int REPEAT_WEEK = 2;

    public Event() {

    }
	
	public Event(String name, String description, String location, int color, long startMills, long endMills){
        this.name = name;
        this.description = description;
        this.location = location;
        this.color = color;
		this.start = startMills;
		this.end = endMills;
	}

    public long getEventID() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }
	public int getColor(){
		return color;
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	/**
	 * Get the event title
	 * 
	 * @return title
	 */
	public String getName(){
		return name;
	}

	/**
	 * Get the event description
	 * 
	 * @return description
	 */
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public void setLocation(String location){
		this.location = location;
	}
	
	public String getLocation(){
		return location;
	}
	
	/**
	 * Set the name of the event
	 * 
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Gets the event id in the database
	 * 
	 * @return event database id
	 */
	public long getEventId(){
		return eventId;
	}

    public long getStart(){
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd(){
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
	/**
	 * Get the start date of the event
	 * 
	 * @return start date
	 */
	public String getStartDate(String dateFormat){
		DateFormat df = new SimpleDateFormat(dateFormat,Locale.getDefault());
		String date = df.format(start);
		
		return date;
	}
	
	/**
	 * Get the end date of the event
	 * 
	 * @return end date
	 */
	public String getEndDate(String dateFormat){
		DateFormat df = new SimpleDateFormat(dateFormat,Locale.getDefault());
		String date = df.format(end);
		
		return date;
	}

    public int getRepeat(){
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }
}
