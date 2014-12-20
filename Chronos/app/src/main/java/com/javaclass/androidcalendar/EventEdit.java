/*
 * Copyright (C) 2011 Morphoss Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.javaclass.androidcalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Database.SQLite.model.Event;
import Database.SQLite.MySQLiteHelper;

@SuppressWarnings("rawtypes")
public class EventEdit extends Activity implements  OnClickListener, OnCheckedChangeListener,
        OnFocusChangeListener, AdapterView.OnItemSelectedListener {

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

	public static final int INSTANCES_SINGLE = 0;
	public static final int INSTANCES_ALL = 1;
	public static final int INSTANCES_THIS_FUTURE = 2;

	private static final int START_DATE_DIALOG = 0;
	private static final int END_DATE_DIALOG = 2;
	private static final int SELECT_COLLECTION_DIALOG = 4;
	private static final int ADD_ALARM_DIALOG = 5;
	private static final int SET_REPEAT_RULE_DIALOG = 6;
	private static final int WHICH_EVENT_DIALOG = 7;
	private static final int LOADING_EVENT_DIALOG = 8;
	private static final int SAVING_DIALOG = 9;

	boolean prefer24hourFormat = false;
    private MySQLiteHelper db = null;

	private String[] repeatRules;
	private String[] repeatRulesValues;
	//private String[] eventChangeRanges; // See strings.xml R.array.EventChangeAffecting

	private String[] alarmRelativeTimeStrings;
	// Must match R.array.RelativeAlarmTimes (strings.xml)

	//GUI Components
	private ImageButton btnStartDate;
	private ImageButton btnEndDate;

    private EditText editStartDate;
    private EditText editEndDate;
    private EditText editStartTime;
    private EditText editEndTime;
    private EditText editLocation;
    private EditText editDescription;
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
		this.setContentView(R.layout.event_edit);
        db = new MySQLiteHelper(this);
        //db.deleteAll();
		//getEventAction();
		this.loadLayout();
	}

	@SuppressWarnings("unchecked")
	private void getEventAction() {
		Bundle b = this.getIntent().getExtras();
		if ( b.containsKey(ACTION_KEY) ) {
			action = b.getInt(ACTION_KEY);
		} else {
			//default action is create
			action = ACTION_CREATE;
		}

		switch ( action ) {
			case ACTION_COPY:
			case ACTION_EDIT:

				// show loading screen.
				mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_LOADING), 50);

				// We need to load the event - we must be given rid about the
				// event to be edited.
				if ( !b.containsKey(RESOURCE_ID_KEY) ) {
					// invalid data supplied
					this.finish();
					return;
				}
				long rid = b.getLong(RESOURCE_ID_KEY);
				String rrid = null;
				// get the recurrenceId if there is one
				if ( b.containsKey(RECCURENCE_ID_KEY) ) {
					// get master
					rrid = b.getString(RECCURENCE_ID_KEY);
				}

				break;

			case ACTION_CREATE:

				try {
				}
				catch ( Exception e ) {}

				try {
				}
				catch ( Exception e ) {
					Log.e(TAG, "Error creating a new event: " + e + Log.getStackTraceString(e));
					this.finish();
					return;
				}
				break;
		}
	}

	private void loadLayout() {
		//Event Colour
		sidebar = (LinearLayout)this.findViewById(R.id.EventEditColourBar);
		sidebarBottom = (LinearLayout)this.findViewById(R.id.EventEditColourBarBottom);

		//Set up Save/Cancel buttons
		this.setupButton(R.id.event_save_button, SAVE);
		this.setupButton(R.id.event_cancel_button, CANCEL);
        this.setupButton(R.id.imageStartButton, START_DATE);
        this.setupButton(R.id.imageEndButton, END_DATE);
        this.setupButton(R.id.btnTimeStart, START);
        this.setupButton(R.id.btnTimeEnd, END);

		//Title
		this.eventName = (TextView) this.findViewById(R.id.EventName);
		if ( action == ACTION_CREATE ) {
			eventName.setSelectAllOnFocus(true);
		}
		eventName.setOnFocusChangeListener(this);


		//date/time fields
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        editStartDate = (EditText) findViewById(R.id.editStartText);
        editEndDate = (EditText) findViewById(R.id.editEndText);

        editStartTime = (EditText) findViewById(R.id.editStartTime);
        editEndTime = (EditText) findViewById(R.id.editEndTime);

        //get and set repeater
        repeatSpinner = (Spinner) findViewById(R.id.repeat_option);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        repeatSpinner.setAdapter(adapter);
        repeatSpinner.setOnItemSelectedListener(this);

        //get and set color
        colorSpinner = (Spinner) findViewById(R.id.color_option);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,
                R.array.color_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        colorSpinner.setAdapter(colorAdapter);
        colorSpinner.setOnItemSelectedListener(this);

        //location view
		locationView = (TextView) this.findViewById(R.id.EventLocationContent);
		notesView = (TextView) this.findViewById(R.id.EventNotesContent);
		locationView.setOnFocusChangeListener(this);
		notesView.setOnFocusChangeListener(this);

		//Button listeners
		//setListen(btnStartDate,START_DATE_DIALOG);
		//setListen(btnEndDate,END_DATE_DIALOG);
		//setListen(alarmsView,ADD_ALARM_DIALOG);
		//setListen(repeatsView,SET_REPEAT_RULE_DIALOG);
	}

	private void setListen(Button b, final int dialog) {
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(dialog);

			}
		});
	}

	private void setupButton(int id, int val) {
        if((id == R.id.imageStartButton) || (id == R.id.imageEndButton)) {
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
                            editStartDate.setText(year + "-"
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
                            editEndDate.setText(year + "-"
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
                            editStartTime.setText(hourOfDay + "-" + minute);
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
                                editEndTime.setText(hourOfDay + "-" + minute);
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

            editLocation = (EditText) findViewById(R.id.EventLocationContent);
            editDescription = (EditText) findViewById(R.id.EventNotesContent);

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
            String location = editLocation.getText().toString();
            String description = editDescription.getText().toString();
            SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd hh-mm"); //format for date
            Date s = formatDate.parse(editStartDate.getText().toString() + " " + editStartTime.getText().toString());
            Date e = formatDate.parse(editEndDate.getText().toString() + " " + editEndTime.getText().toString());
            long start = s.getTime();
            long end = e.getTime();

            event.setName(name);
            event.setLocation(location);
            event.setDescription(description);
            event.setStart(start);
            event.setEnd(end);
            event.setRepeat(repeatOption);
            event.setColor(colorOption);

            db.addEvent(event);
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
