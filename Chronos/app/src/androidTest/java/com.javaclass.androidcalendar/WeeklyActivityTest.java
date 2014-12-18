package com.javaclass.androidcalendar;


import junit.framework.TestCase;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Created by damquangtuan on 16/12/2014.
 */
public class WeeklyActivityTest extends ActivityInstrumentationTestCase2<WeeklyActivity>{
    public WeeklyActivityTest() {
        super(WeeklyActivity.class);
    }

    @Test
    public void testWeekTodayButtonString() {
        WeeklyActivity activity = getActivity();
        Button btnToday = (Button) activity.findViewById(R.id.week_today_button);
        assertEquals(activity.getText(R.string.Today).toString(), btnToday.getText().toString());
    }

    @Test
    public void testWeekYearButtonString() {
        WeeklyActivity activity = getActivity();
        Button btnYear = (Button) activity.findViewById(R.id.week_year_button);
        assertEquals(activity.getText(R.string.Year).toString(), btnYear.getText().toString());
    }

    @Test
    public void testWeekWeekButtonString() {
        WeeklyActivity activity = getActivity();
        Button btnWeek = (Button) activity.findViewById(R.id.week_week_button);
        assertEquals(activity.getText(R.string.Week).toString(), btnWeek.getText().toString());
    }

    @Test
    public void testInt() {
        assert true;
    }
}
