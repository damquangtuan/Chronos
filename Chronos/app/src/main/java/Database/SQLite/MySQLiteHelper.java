package Database.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import Database.SQLite.model.Event;

public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TAG = MySQLiteHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "Chronos";

    private static final String EVENTS_TABLE = "events";
    private static final String EVENT = "event";
    private static final String LOCATION = "location";
    private static final String DESCRIPTION = "description";
    private static final String START = "start";
    private static final String END = "end";
    private static final String ID = "_id";
    private static final String START_DAY = "start_day";
    private static final String END_DAY = "end_day";
    private static final String COLOR = "color";
    private static final String REPEAT = "repeat";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create Events table
        String CREATE_EVENT_TABLE = "CREATE TABLE " + EVENTS_TABLE + "(" + ID + " integer primary key autoincrement, " +
                EVENT + " TEXT, " + LOCATION + " TEXT, " + DESCRIPTION + " TEXT, "
                + START + " INTEGER, "+ END + " INTEGER, " + START_DAY + " INTEGER, " + END_DAY + " INTEGER, " + COLOR +" INTEGER, "
                    + REPEAT + " INTEGER );";

        // create Events table
        db.execSQL(CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older Events table if existed
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);

        // create fresh Events table
        this.onCreate(db);
    }

    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) EVENTS + get all EVENTS + delete all EVENTS
     */

    private static final String[] COLUMNS = {ID,EVENT,LOCATION, DESCRIPTION, START, END,
            START_DAY, END_DAY, COLOR};

    public void addEvent(Event event){
        //for logging
        Log.d(TAG, "addEvent " + event.toString());
        String dataFormat = "yyyy-mm-dd hh:mm:ss";
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(EVENT, event.getName()); // get title
        values.put(LOCATION, event.getLocation()); // get location
        values.put(DESCRIPTION, event.getDescription()); // get description
        values.put(START, event.getStart()); // get start
        values.put(END, event.getEnd()); // get end
        values.put(START_DAY, event.getStartDate(dataFormat)); // get start day
        values.put(END_DAY, event.getEndDate(dataFormat)); // get end day
        values.put(DESCRIPTION, event.getDescription()); // get description
        values.put(COLOR, event.getColor()); // get color
        values.put(REPEAT, event.getRepeat()); //get repeat

        // 3. insert
        db.insert(EVENTS_TABLE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Event getEvent(long id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + EVENTS_TABLE + " WHERE _id = " + id;

        Cursor cursor = db.rawQuery(query, null);
        // 2. build query
/*        Cursor cursor =
                db.query(EVENTS_TABLE, // a. table
                        COLUMNS, // b. column names
                        " _id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit*/

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build event object
        Event event = new Event();
        event.setEventId(cursor.getLong(cursor.getColumnIndex("_id")));
        event.setName(cursor.getString(1));
        event.setLocation(cursor.getString(2));
        event.setDescription(cursor.getString(3));
        event.setStart(cursor.getLong(4));
        event.setEnd(cursor.getLong(5));
        event.setColor(cursor.getInt(8));
        event.setRepeat(cursor.getInt(9));

        Log.d(TAG, "getEvent("+id+") " + event.toString());

        // 5. return event
        return event;
    }
    // Get All Events
    public List<Event> getAllEvents() {
        List<Event> eventList = new LinkedList<Event>();

        // 1. build the query
        String query = "SELECT  * FROM " + EVENTS_TABLE;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build Event and add it to list
        Event event = null;
        if (cursor.moveToFirst()) {
            do {
                event = new Event();
                event.setEventId(cursor.getLong(0));
                event.setName(cursor.getString(1));
                event.setLocation(cursor.getString(2));
                event.setDescription(cursor.getString(3));
                event.setStart(cursor.getLong(4));
                event.setEnd(cursor.getLong(5));
                event.setColor(cursor.getInt(8));
                event.setRepeat(cursor.getInt(9));

                // Add event to eventList
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "getAllEvents() " + eventList.toString());

        // return eventList
        return eventList;
    }
    // Updating single event
    public int updateEvent(Event event) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        String dataFormat = "yyyyy-mm-dd hh:mm:ss";

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(EVENT, event.getName()); // get title
        values.put(LOCATION, event.getLocation()); // get location
        values.put(DESCRIPTION, event.getDescription()); // get description
        values.put(START, event.getStart()); // get start
        values.put(END, event.getEnd()); // get end
        values.put(START_DAY, event.getStartDate(dataFormat)); // get start day
        values.put(END_DAY, event.getEndDate(dataFormat)); // get end day
        values.put(DESCRIPTION, event.getDescription()); // get description
        values.put(COLOR, event.getColor()); // get color
        values.put(REPEAT, event.getRepeat()); //get repeat

        // 3. updating row
        int i = db.update(EVENTS_TABLE, //table
                values, // column/value
                ID+" = ?", // selections
                new String[] { String.valueOf(event.getEventId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single event
    public void deleteEvent(Event event) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(EVENTS_TABLE,
                ID+" = ?",
                new String[] { String.valueOf(event.getEventId()) });

        // 3. close
        db.close();

        Log.d(TAG, "deleteEvent " + event.toString());

    }

    // Deleting all event records
    public void deleteAll() {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(EVENTS_TABLE, "", null);

        // 3. close
        db.close();

        Log.d(TAG, "deleteAll");

    }
}