package com.javaclass.androidcalendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
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


public class MonthlyActivity extends ActionBarActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private Calendar month_year = null; // use only month and year; day must be assigned to 1

    private Button month_year_btn = null;
    private Button new_event_btn = null;
    private ListView event_list_view = null;

    //
    // custom adapter for monthly view
    //
    private class EventAdapter extends ArrayAdapter<Event> {
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
                TextView day = (TextView) findViewById(R.id.day_of_month);
                TextView event_name = (TextView) findViewById(R.id.event_name);

                // calculate a day of a month
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(p.getStart());

                // set the day and the event day on that day
                day.setText(c.get(Calendar.DAY_OF_MONTH) + " th");
                event_name.setText(p.getName());
            }
            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        event_list_view = (ListView)findViewById(R.id.event_list_view);


        setContentView(R.layout.activity_monthly);
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
        String month_year_str = month_year.get(Calendar.MONTH) + " "  +
                month_year.get(Calendar.YEAR);

        // show selected month & year
        month_year_btn.setText(month_year_str);

        //
        // show a list of events of the given month and year using 'ListView'
        //

        // get events from the db
        MySQLiteHelper db = new MySQLiteHelper(this);
        Scheduler scheduler = new Scheduler(db);
        List<Event> event_list = scheduler.getEvents(year, month);

        // update monthly event list with the given list
        event_list_view.setAdapter(new EventAdapter(this, R.layout.monthly_list, event_list));
    }

}
