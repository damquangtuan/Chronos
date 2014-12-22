package com.javaclass.androidcalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import Database.SQLite.MySQLiteHelper;
import Database.SQLite.model.Event;


public class MonthlyActivity extends Activity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    public static final String[] MONTH_NAMES = {
            "Jan.",
            "Feb.",
            "Mar.",
            "Apr.",
            "May",
            "Jun.",
            "Jul.",
            "Aug.",
            "Sep.",
            "Oct.",
            "Nov.",
            "Dec."
    };


    private Calendar month_year = null; // use only month and year; day must be assigned to 1

    private Button month_year_btn = null;
    private Button new_event_btn = null;
    private Button clear_events_btn = null;
    private ListView event_list_view = null;
    private TextView no_event_msg = null;

    //
    // custom adapter for monthly view
    //
    private class EventAdapter extends ArrayAdapter<Event> implements View.OnClickListener{
        private List<Event> items;

        public EventAdapter(Context context, int textViewResourceId, List<Event> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.monthly_list, null);
            }

            Event p = items.get(position);
            if (p != null) {
                TextView day = (TextView)v.findViewById(R.id.day_of_month);
                TextView event_name = (TextView)v.findViewById(R.id.event_name);
                TextView start_time = (TextView)v.findViewById(R.id.start_time);
                TextView end_time = (TextView)v.findViewById(R.id.end_time);
                TextView event_desc = (TextView)v.findViewById(R.id.event_desc);

                Button del_btn = (Button)v.findViewById(R.id.delete_event_btn);
                del_btn.setOnClickListener(this);

                // calculate event time
                Calendar start = Calendar.getInstance();
                start.setTimeInMillis(p.getStart());

                Calendar end = Calendar.getInstance();
                end.setTimeInMillis(p.getEnd());

                // set event information
                day.setText(start.get(Calendar.DAY_OF_MONTH) + " th");
                event_name.setText(p.getName());
                start_time.setText(p.getStartDate("yyyy-MM-dd HH:mm"));
                end_time.setText(p.getEndDate("yyyy-MM-dd HH:mm"));
                event_desc.setText(p.getDescription());
            }
            return v;
        }

        @Override
        public void onClick(View view) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly);

        // set today
        month_year = Calendar.getInstance();
        month_year.set(month_year.get(Calendar.YEAR), month_year.get(Calendar.MONTH), 1);

        //
        // get views
        //

        month_year_btn = (Button)findViewById(R.id.month_year_btn);
        month_year_btn.setOnClickListener(this);

        new_event_btn = (Button)findViewById(R.id.new_event_btn);
        new_event_btn.setOnClickListener(this);

        clear_events_btn = (Button)findViewById(R.id.clear_events_btn);
        clear_events_btn.setOnClickListener(this);

        event_list_view = (ListView)findViewById(R.id.event_list_view);
        event_list_view.setVisibility(View.VISIBLE);

        no_event_msg = (TextView)findViewById(R.id.no_event_msg);
        no_event_msg.setVisibility(View.INVISIBLE);

        // show current month and year
        updateCalender(month_year.get(Calendar.YEAR), month_year.get(Calendar.MONTH));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monthly, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        // change a month and a year
        if(view.equals(month_year_btn)) {
            // show a picker dialog
            // let the picker selected with 'month_year' by default
            DatePickerDialog dialog = new DatePickerDialog(this, this,
                    month_year.get(Calendar.YEAR), month_year.get(Calendar.MONTH), 1);
            dialog.show();
        }

        // add a new event
        else if(view.equals(new_event_btn)) {
            // TO DO
            // 1. change activity to get new event information from the user
            // 2. add it to db
            // 3. update monthly event list

            Intent intent = new Intent(this, EventEdit.class);
            startActivity(intent);
        }

        // clear all event on a given month
        else if(view.equals(clear_events_btn)) {

        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // we do not use 'day'
        month_year.set(year, month, 1);
        updateCalender(year, month);
    }

    // set the calendar with a given year and month
    private void updateCalender(int year, int month) {
        String month_year_str = MONTH_NAMES[month_year.get(Calendar.MONTH)] + " "  +
                month_year.get(Calendar.YEAR);

        // show selected month & year
        month_year_btn.setText(month_year_str);

        //
        // show a list of events of the given month and year using 'ListView'
        //

        // get events from the db
        MySQLiteHelper db = new MySQLiteHelper(this);
        EventScheduler scheduler = new EventScheduler(db);
        List<Event> event_list = scheduler.getEvents(year, month);

        if(!event_list.isEmpty()) {
            event_list_view.setVisibility(View.VISIBLE);
            no_event_msg.setVisibility(View.INVISIBLE);

            // update monthly event list with the given list
            event_list_view.setAdapter(new EventAdapter(this, R.layout.monthly_list, event_list));
        }
        else {
            event_list_view.setVisibility(View.INVISIBLE);
            no_event_msg.setVisibility(View.VISIBLE);
        }
    }

}
