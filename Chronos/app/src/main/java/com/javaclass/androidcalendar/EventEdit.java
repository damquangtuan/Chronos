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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Calendar;

@SuppressWarnings("rawtypes")
public class EventEdit extends Activity implements  OnClickListener, OnCheckedChangeListener,
        OnFocusChangeListener {

	public static final String TAG = "Chronos EventEdit";
	public static final int APPLY = 0;
	public static final int CANCEL = 1;
    public static final int START_DATE = 2;
    public static final int END_DATE = 3;

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
	private LinearLayout sidebar;
	private LinearLayout sidebarBottom;
	private TextView eventName;
	private TextView locationView;
	private TextView notesView;
	private TableLayout alarmsList;
	private Button repeatsView;
	private Button alarmsView;
	private LinearLayout llSelectCollection;
	private Button btnSelectCollection;

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

	private Dialog loadingDialog = null;
	private Dialog savingDialog = null;

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

		}
	};


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.event_edit);

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
		this.setupButton(R.id.event_apply_button, APPLY);
		this.setupButton(R.id.event_cancel_button, CANCEL);
        this.setupButton(R.id.imageStartButton, START_DATE);
        this.setupButton(R.id.imageEndButton, END_DATE);

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

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return new DatePickerDialog(this, datePickerListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            editStartDate.setText(selectedDay + " / " + (selectedMonth + 1) + " / "
                    + selectedYear);
        }
    };

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
		case APPLY:
			applyChanges();
			break;
		case CANCEL:
			finish();
        case START_DATE:
            showDialog(0);
        case END_DATE:
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
	}


	public void applyChanges() {
		//check if text fields changed
		//summary

		if (action == ACTION_EDIT && instances < 0)
			this.showDialog(WHICH_EVENT_DIALOG);
		else if ( !this.saveChanges() ) {
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
