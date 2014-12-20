package com.javaclass.androidcalendar;

import android.app.DatePickerDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import Database.SQLite.model.Event;


public class MonthlyActivity extends ActionBarActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private Calendar month_year; // use only month and year; day must be assigned to 1

    private Button month_year_btn;
    private Button new_event_btn;
    private ListView event_list_view;

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
        if(view.equals(month_year_btn)) {
            // show a picker dialog
            // let the picker selected with 'month_year' by default
            DatePickerDialog dialog = new DatePickerDialog(this, this,
                    month_year.get(Calendar.YEAR), month_year.get(Calendar.MONTH), 1);
            dialog.show();
        }

        else if(view.equals(new_event_btn)) {

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
        List<Event> event_list = null; // must be changed to something; not null
    }

}
