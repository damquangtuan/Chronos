package com.javaclass.androidcalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Database.SQLite.MySQLiteHelper;
import Database.SQLite.model.Event;

/**
 * Created by damquangtuan on 21/12/2014.
 */
public class EventDetail extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        View.OnFocusChangeListener, AdapterView.OnItemSelectedListener{
    public static final String TAG = "EventEdit";
    public static final int SAVE = 0;
    public static final int CANCEL = 1;
    public static final int START_DATE = 2;
    public static final int END_DATE = 3;
    public static final int START = 4;
    public static final int END = 5;

    //repeater
    public static final int REPEAT_NO = 0;
    public static final int REPEAT_DAY = 1;
    public static final int REPEAT_WEEK = 2;

    // keys for the data we return.
    public static final String			resultSimpleAcalEvent		= "newSimpleEvent";
    public static final String			resultAcalEvent					= "newAcalEvent";
    public static final String			resultCollectionId			= "newCollectionId";

    public static final String RESOURCE_ID_KEY = "resourceId";
    public static final String RECCURENCE_ID_KEY = "reccurenceId";
    public static final String ACTION_KEY = "action";
    public static final String NEW_EVENT_DATE_TIME_KEY = "datetime";

    public static final int ACTION_EDIT = 1;
    public static final int ACTION_CREATE = 2;
    public static final int ACTION_COPY = 3;
    public static final int ACTION_DELETE = 4;

    private MySQLiteHelper db = null;
    private Scheduler scheduler = null;

    //GUI Components
    private ImageButton btnStartDate;
    private ImageButton btnEndDate;

    private EditText detailStartDate;
    private EditText detailEndDate;
    private EditText detailStartTime;
    private EditText detailEndTime;
    private EditText detailLocation;
    private EditText detailDescription;
    private int repeatOption = 0;
    private int colorOption = 0;

    private LinearLayout sidebar;
    private LinearLayout sidebarBottom;
    private TextView eventName;
    private TextView locationView;
    private TextView notesView;

    private Spinner repeatSpinner;
    private Spinner colorSpinner;
    private String[] collectionsArray;

    private int action;
    private int instances = -1;



    private static final int REFRESH = 0;
    private static final int FAIL = 1;
    private static final int CONFLICT = 2;
    private static final int SHOW_LOADING = 3;
    private static final int GIVE_UP = 4;
    private static final int SAVE_RESULT = 5;
    private static final int SAVE_FAILED = 6;
    private static final int SHOW_SAVING = 7;

    private boolean saveSucceeded = false;
    private boolean isSaving = false;
    private boolean isLoading = false;
    private Calendar cal;
    private int day;
    private int month;
    private int year;

    Event event;

    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;

    private Dialog loadingDialog = null;
    private Dialog savingDialog = null;

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.event_detail);
        db = new MySQLiteHelper(this);
        scheduler = new Scheduler(db);

        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        this.event = (Event)extras.get("event");
        //db.deleteAll();
        //getEventAction();
        this.loadLayout();
    }

    private void loadLayout() {
        //Event Colour
        sidebar = (LinearLayout)this.findViewById(R.id.EventDetailColourBar);
        sidebarBottom = (LinearLayout)this.findViewById(R.id.EventDetailColourBarBottom);
        sidebar.setEnabled(false);
        sidebarBottom.setEnabled(false);

        //Set up Save/Cancel buttons
        this.setupButton(R.id.event_save_buttonDetail, SAVE);
        this.setupButton(R.id.event_cancel_buttonDetail, CANCEL);
        this.setupButton(R.id.imageStartButtonDetail, START_DATE);
        this.setupButton(R.id.imageEndButtonDetail, END_DATE);
        this.setupButton(R.id.btnTimeStartDetail, START);
        this.setupButton(R.id.btnTimeEndDetail, END);

        ImageButton imageStartButton = (ImageButton) findViewById(R.id.imageStartButtonDetail);
        imageStartButton.setEnabled(false); //set the button to be disable
        ImageButton imageEndButton = (ImageButton) findViewById(R.id.imageEndButtonDetail);
        imageEndButton.setEnabled(false); //set the button to be disable

        Button timeStartButton = (Button) findViewById(R.id.btnTimeStartDetail);
        timeStartButton.setEnabled(false); //set the button to be disable
        Button timeEndButton = (Button) findViewById(R.id.btnTimeEndDetail);
        timeEndButton.setEnabled(false); //set the button to be disable

        //Title
        this.eventName = (TextView) this.findViewById(R.id.EventNameDetail);
        if ( action == ACTION_CREATE ) {
            eventName.setSelectAllOnFocus(true);
        }
        eventName.setOnFocusChangeListener(this);
        eventName.setText(event.getName()); // set event name
        eventName.setEnabled(false); //set to be disable

        //date/time fields
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        String dateFormat = "yyyy-mm-dd";
        detailStartDate = (EditText) findViewById(R.id.detailStartText);
        detailStartDate.setText(event.getStartDate(dateFormat));//set event start date
        detailStartDate.setEnabled(false); //set to be disable

        detailEndDate = (EditText) findViewById(R.id.detailEndText);
        detailEndDate.setText(event.getEndDate(dateFormat));//set event end date
        detailEndDate.setEnabled(false); //set to be disable

        String timeFormat = "hh-mm";
        detailStartTime = (EditText) findViewById(R.id.detailStartTime);
        detailStartTime.setText(event.getStartDate(timeFormat)); //set event start time
        detailStartTime.setEnabled(false); //set to be disable

        detailEndTime = (EditText) findViewById(R.id.detailEndTime);
        detailEndTime.setText(event.getEndDate(timeFormat));//set event end time
        detailEndTime.setEnabled(false); //set to be disable

        //get and set repeater
        repeatSpinner = (Spinner) findViewById(R.id.repeat_optionDetail);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        repeatSpinner.setAdapter(adapter);
        repeatSpinner.setOnItemSelectedListener(this);
        repeatSpinner.setEnabled(false); //set to be disable

        //get and set color
        colorSpinner = (Spinner) findViewById(R.id.color_optionDetail);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,
                R.array.color_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        colorSpinner.setAdapter(colorAdapter);
        colorSpinner.setOnItemSelectedListener(this);
        colorSpinner.setEnabled(false); //set to be disable

        //location view
        locationView = (TextView) this.findViewById(R.id.EventLocationContentDetail);
        locationView.setText(event.getLocation()); //set location content


        notesView = (TextView) this.findViewById(R.id.EventNotesContentDetail);
        notesView.setText(event.getDescription()); //set description content

        locationView.setOnFocusChangeListener(this);
        notesView.setOnFocusChangeListener(this);
        locationView.setEnabled(false); //set to be disable
        notesView.setEnabled(false); //set to be disable
    }

    private void setListen(Button b, final int dialog) {
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showDialog(dialog);

            }
        });
    }

    private void setupButton(int id, int val) {
        if((id == R.id.imageStartButtonDetail) || (id == R.id.imageEndButtonDetail)) {
            ImageButton button = (ImageButton) this.findViewById(id);
            button.setOnClickListener(this);
            button.setTag(val);
        } else {
            Button button = (Button) this.findViewById(id);
            button.setOnClickListener(this);
            button.setTag(val);
        }
    }

    @Override
    public void onClick(View arg0) {
        int button = ((Integer)arg0.getTag());
        switch ( button ) {
            case SAVE:
                applyChanges();
                //hangout for 500 mini seconds
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                break;
            case CANCEL:
                finish();
                break;
            case START_DATE:
                // Process to get Current Date
                final Calendar startCal = Calendar.getInstance();
                mYear = startCal.get(Calendar.YEAR);
                mMonth = startCal.get(Calendar.MONTH);
                mDay = startCal.get(Calendar.DAY_OF_MONTH);

                // Launch Date Picker Dialog
                DatePickerDialog dpdStartDate = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                detailStartDate.setText(year + "-"
                                        + (monthOfYear + 1) + "-" + dayOfMonth);

                            }
                        }, mYear, mMonth, mDay);
                dpdStartDate.show();
                break;
            case END_DATE:
                // Process to get Current Date
                final Calendar endCal = Calendar.getInstance();
                mYear = endCal.get(Calendar.YEAR);
                mMonth = endCal.get(Calendar.MONTH);
                mDay = endCal.get(Calendar.DAY_OF_MONTH);
                // Launch Date Picker Dialog
                DatePickerDialog dpdEndDate = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                detailEndDate.setText(year + "-"
                                        + (monthOfYear + 1) + "-" + dayOfMonth);

                            }
                        }, mYear, mMonth, mDay);
                dpdEndDate.show();
                break;
            case START:
                // Process to get Current Time
                final Calendar calStart = Calendar.getInstance();
                mHour = calStart.get(Calendar.HOUR_OF_DAY);
                mMinute = calStart.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog tpdStart = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // Display Selected time in textbox
                                detailStartTime.setText(hourOfDay + "-" + minute);
                            }
                        }, mHour, mMinute, false);
                tpdStart.show();
                break;
            case END:
                // Process to get Current Time
                final Calendar calEnd = Calendar.getInstance();
                mHour = calEnd.get(Calendar.HOUR_OF_DAY);
                mMinute = calEnd.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog tpdEnd = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                // Display Selected time in textbox
                                detailEndTime.setText(hourOfDay + "-" + minute);
                            }
                        }, mHour, mMinute, false);
                tpdEnd.show();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos).toString();
        //repeatOption = pos;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    public void applyChanges() {
        //check if text fields changed
        //summary
        if ( !this.saveChanges() ) {
            Toast.makeText(this, "Save failed: retrying!", Toast.LENGTH_LONG).show();
            this.saveChanges();
        }
        else {
            Toast.makeText(this, "Event(s) Saved.", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean saveChanges() {


        try {
            //display savingdialog

            //ResourceManager.getInstance(this).sendRequest(new RREventEditedRequest(this, event, action, instances));

            //set message for 10 seconds to fail.
            mHandler.sendEmptyMessageDelayed(SAVE_FAILED, 100000);

            //showDialog(SAVING_DIALOG);
            mHandler.sendEmptyMessageDelayed(SHOW_SAVING,50);

            detailLocation = (EditText) findViewById(R.id.EventLocationContent);
            detailDescription = (EditText) findViewById(R.id.EventNotesContent);

            //get repeat option
            String repeat_str = repeatSpinner.getSelectedItem().toString();
            if(repeat_str.equals("No Repeat")) {
                repeatOption = 0;
            } else if (repeat_str.equals("Daily Repeat")) {
                repeatOption = 1;
            } else if (repeat_str.equals("Weekly Repeat")) {
                repeatOption = 2;
            }

            //get color option
            String color_str = colorSpinner.getSelectedItem().toString();
            if(color_str.equals("Red")) {
                colorOption = 0;
            } else if (color_str.equals("Blue")) {
                colorOption = 1;
            } else if (color_str.equals("Yellow")) {
                colorOption = 2;
            } else if (color_str.equals("Purple")) {
                colorOption = 3;
            } else if (color_str.equals("Green")) {
                colorOption = 4;
            }
            //saving event

            Event event = new Event();
            //get value from dialog
            String name = eventName.getText().toString();
            String location = detailLocation.getText().toString();
            String description = detailDescription.getText().toString();
            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd hh-mm"); //format for date
            Date s = formatDate.parse(detailStartDate.getText().toString() + " " + detailStartTime.getText().toString());
            Date e = formatDate.parse(detailEndDate.getText().toString() + " " + detailEndTime.getText().toString());
            long start = s.getTime();
            long end = e.getTime();

            event.setName(name);
            event.setLocation(location);
            event.setDescription(description);
            event.setStart(start);
            event.setEnd(end);
            event.setRepeat(repeatOption);
            event.setColor(colorOption);

            scheduler.addEvent(event);
        }
        catch (Exception e) {
            if ( e.getMessage() != null ) Log.d(TAG,e.getMessage());
            //if (Constants.LOG_DEBUG)Log.d(TAG,Log.getStackTraceString(e));
            return false;
        }

        return true;
    }

    private void checkpointCurrentValues() {
        // Make sure the text fields are all preserved before we start any dialogs.
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if ( !hasFocus ) checkpointCurrentValues();
    }
}
