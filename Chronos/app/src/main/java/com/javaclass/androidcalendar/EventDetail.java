package com.javaclass.androidcalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
    public static final int DELETE = 6;

    //repeater
    public static final int REPEAT_NO = 0;
    public static final int REPEAT_DAY = 1;
    public static final int REPEAT_WEEK = 2;

    //Color
    public static final int COLOR_RED = 0;
    public static final int COLOR_BLUE = 1;
    public static final int COLOR_YELLOW = 2;
    public static final int COLOR_PURPLE = 3;
    public static final int COLOR_GREEN = 4;

    public static final int ACTION_EDIT = 1;
    public static final int ACTION_CREATE = 2;
    public static final int ACTION_COPY = 3;
    public static final int ACTION_DELETE = 4;

    private MySQLiteHelper db = null;
    private Scheduler scheduler = null;

    private EditText detailStartDate;
    private EditText detailEndDate;
    private EditText detailStartTime;
    private EditText detailEndTime;
    private EditText detailLocation;
    private EditText detailDescription;
    private int repeatOption = 0;
    private int colorOption = 0;

    private ImageButton imageStartButton;
    private ImageButton imageEndButton;
    private Button timeStartButton;
    private Button timeEndButton;
    private LinearLayout sidebar;
    private LinearLayout sidebarBottom;
    private TextView eventName;
    private TextView locationView;
    private TextView notesView;

    private Spinner repeatSpinner;
    private Spinner colorSpinner;

    private int action;



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
        this.loadLayout();
    }

    //to set all elements in detail screen disable
    private void setEnable(boolean value) {
        sidebar.setEnabled(value);
        sidebarBottom.setEnabled(value);
        imageStartButton.setEnabled(value); //set the button to be disable
        imageEndButton.setEnabled(value); //set the button to be disable
        timeStartButton.setEnabled(value); //set the button to be disable
        timeEndButton.setEnabled(value); //set the button to be disable

        eventName.setEnabled(value); //set to be disable
        detailStartDate.setEnabled(value); //set to be disable
        detailEndDate.setEnabled(value); //set to be disable
        detailStartTime.setEnabled(value); //set to be disable
        detailEndTime.setEnabled(value); //set to be disable
        repeatSpinner.setEnabled(value); //set to be disable
        colorSpinner.setEnabled(value); //set to be disable

        locationView.setEnabled(value); //set to be disable
        notesView.setEnabled(value); //set to be disable
    }

    private void loadLayout() {
        //Event Colour
        sidebar = (LinearLayout)this.findViewById(R.id.EventDetailColourBar);
        sidebarBottom = (LinearLayout)this.findViewById(R.id.EventDetailColourBarBottom);


        //Set up Save/Cancel buttons
        this.setupButton(R.id.event_save_buttonDetail, SAVE);
        this.setupButton(R.id.event_cancel_buttonDetail, CANCEL);
        this.setupButton(R.id.imageStartButtonDetail, START_DATE);
        this.setupButton(R.id.imageEndButtonDetail, END_DATE);
        this.setupButton(R.id.btnTimeStartDetail, START);
        this.setupButton(R.id.btnTimeEndDetail, END);
        this.setupButton(R.id.event_delete_buttonDetail, DELETE);

        imageStartButton = (ImageButton) findViewById(R.id.imageStartButtonDetail);

        imageEndButton = (ImageButton) findViewById(R.id.imageEndButtonDetail);


        timeStartButton = (Button) findViewById(R.id.btnTimeStartDetail);

        timeEndButton = (Button) findViewById(R.id.btnTimeEndDetail);


        //Title
        this.eventName = (TextView) this.findViewById(R.id.EventNameDetail);
        if ( action == ACTION_CREATE ) {
            eventName.setSelectAllOnFocus(true);
        }
        eventName.setOnFocusChangeListener(this);
        eventName.setText(event.getName()); // set event name


        //date/time fields
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        String dateFormat = "yyyy-MM-dd";
        detailStartDate = (EditText) findViewById(R.id.detailStartText);
        detailStartDate.setText(event.getStartDate(dateFormat));//set event start date


        detailEndDate = (EditText) findViewById(R.id.detailEndText);
        detailEndDate.setText(event.getEndDate(dateFormat));//set event end date


        String timeFormat = "hh-mm";
        detailStartTime = (EditText) findViewById(R.id.detailStartTime);
        detailStartTime.setText(event.getStartDate(timeFormat)); //set event start time


        detailEndTime = (EditText) findViewById(R.id.detailEndTime);
        detailEndTime.setText(event.getEndDate(timeFormat));//set event end time


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
        repeatSpinner.setSelection(event.getRepeat());

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
        colorSpinner.setSelection(event.getColor());


        //location view
        locationView = (TextView) this.findViewById(R.id.EventLocationContentDetail);
        locationView.setText(event.getLocation()); //set location content


        notesView = (TextView) this.findViewById(R.id.EventNotesContentDetail);
        notesView.setText(event.getDescription()); //set description content

        locationView.setOnFocusChangeListener(this);
        notesView.setOnFocusChangeListener(this);

        setEnable(false);
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
                Button saveButton = (Button) findViewById(R.id.event_save_buttonDetail);
                if (saveButton.getText().toString().equals("Edit")) {
                    setEnable(true);
                    saveButton.setText("Save");
                } else {
                    applyChanges();
                    finish();
                }
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
            case DELETE:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Deleting Event")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                scheduler.deleteEvent(event);
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. We can retrieve the selected item using
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

            //set message for 10 seconds to fail.
            mHandler.sendEmptyMessageDelayed(SAVE_FAILED, 100000);

            //showDialog(SAVING_DIALOG);
            mHandler.sendEmptyMessageDelayed(SHOW_SAVING,50);

            detailLocation = (EditText) findViewById(R.id.EventLocationContentDetail);
            detailDescription = (EditText) findViewById(R.id.EventNotesContentDetail);

            //get repeat option
            String repeat_str = repeatSpinner.getSelectedItem().toString();
            if(repeat_str.equals("No Repeat")) {
                repeatOption = REPEAT_NO;
            } else if (repeat_str.equals("Daily Repeat")) {
                repeatOption = REPEAT_DAY;
            } else if (repeat_str.equals("Weekly Repeat")) {
                repeatOption = REPEAT_WEEK;
            }

            //get color option
            String color_str = colorSpinner.getSelectedItem().toString();
            if(color_str.equals("Red")) {
                colorOption = COLOR_RED;
            } else if (color_str.equals("Blue")) {
                colorOption = COLOR_BLUE;
            } else if (color_str.equals("Yellow")) {
                colorOption = COLOR_YELLOW;
            } else if (color_str.equals("Purple")) {
                colorOption = COLOR_PURPLE;
            } else if (color_str.equals("Green")) {
                colorOption = COLOR_GREEN;
            }
            //saving event

            Event modifiedEvent = new Event();
            //get value from dialog
            String name = eventName.getText().toString();
            String location = detailLocation.getText().toString();
            String description = detailDescription.getText().toString();
            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd hh-mm"); //format for date
            Date s = formatDate.parse(detailStartDate.getText().toString() + " " + detailStartTime.getText().toString());
            Date e = formatDate.parse(detailEndDate.getText().toString() + " " + detailEndTime.getText().toString());
            long start = s.getTime();
            long end = e.getTime();

            modifiedEvent.setEventId(event.getEventId());
            modifiedEvent.setName(name);
            modifiedEvent.setLocation(location);
            modifiedEvent.setDescription(description);
            modifiedEvent.setStart(start);
            modifiedEvent.setEnd(end);
            modifiedEvent.setRepeat(repeatOption);
            modifiedEvent.setColor(colorOption);

            scheduler.updateEvent(modifiedEvent);
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
