package com.javaclass.androidcalendar;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by hcpl on 03/11/14.
 */
public class MainActivityTest extends TestCase {

    @Test
    public void testThatSucceeds(){
        // all OK
        int a = 1;
        int b = 1;
        assertEquals(a, b);
    }

    @Test
    public void testThatFails(){
        // all OK
        assert true;
    }
}
