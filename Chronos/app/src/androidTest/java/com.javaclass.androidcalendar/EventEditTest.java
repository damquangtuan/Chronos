package com.javaclass.androidcalendar;

import static org.junit.Assert.assertEquals;
import junit.framework.TestCase;
import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;

import org.junit.Test;

/**
 * Created by damquangtuan on 17/12/2014.
 */
public class EventEditTest extends ActivityInstrumentationTestCase2<EventEdit>{
    public EventEditTest() {
        super(EventEdit.class);
    }
    @Test
    public void testApplyButtonString() {
        Activity activity = getActivity();
        Button btnSave = (Button) activity.findViewById(R.id.event_save_button);
        assertEquals(activity.getText(R.string.Save).toString(), btnSave.getText().toString());
    }

    @Test
    public void testCancelButtonString() {
        Activity activity = getActivity();
        Button btnCancel = (Button) activity.findViewById(R.id.event_cancel_button);
        assertEquals(activity.getText(R.string.Cancel).toString(), btnCancel.getText().toString());
    }
}
