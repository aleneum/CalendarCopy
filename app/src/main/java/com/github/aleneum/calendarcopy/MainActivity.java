package com.github.aleneum.calendarcopy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, View.OnClickListener {

    public static String DEBUG_TAG = "ccopy.MainActivity";
    public static final int REQUEST_CALENDAR_READ = 0;
    public static final int REQUEST_CALENDAR_WRITE = 1;

    public CalendarService service;

    private long targetCalendarId = -1;
    private long targetEventId = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreate() ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(DEBUG_TAG, "requestPermissions ...");
        if (Utils.hasPermission(this, Manifest.permission.READ_CALENDAR, REQUEST_CALENDAR_READ)) {
            Button buttonCopy = (Button) findViewById(R.id.buttonCopy);
            buttonCopy.setOnClickListener(this);
            runCalendarStuff();
        }
    }

    protected void runCalendarStuff() {
        service = new CalendarService(this);
        service.getCalendars();

        Spinner calendarSpinner = (Spinner) findViewById(R.id.spinnerCalendar);

        // Specify the layout to use when the list of choices appears
        ArrayAdapter<String> calendarAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, service.calendarNames);
        // Apply the adapter to the spinner
        calendarSpinner.setAdapter(calendarAdapter);
        calendarSpinner.setOnItemSelectedListener(this);

        Spinner targetSpinner = (Spinner) findViewById(R.id.spinnerTarget);
        ArrayAdapter<String> targetAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, service.calendarNames);
        targetSpinner.setAdapter(targetAdapter);
        targetSpinner.setOnItemSelectedListener(this);

        Spinner eventSpinner = (Spinner) findViewById(R.id.spinnerEvent);
        eventSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CALENDAR_READ:
            case REQUEST_CALENDAR_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    runCalendarStuff();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e(DEBUG_TAG, "Permissions were not granted!");
                }
                return;
            }

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int pos = adapterView.getSelectedItemPosition();
        if (adapterView.getId() == R.id.spinnerCalendar) {
            Log.d(DEBUG_TAG, "Calendar selected: " + pos);
            service.getEvents(service.calendars.get(pos).getId());
            List<String> eventNames = new ArrayList<String>();

            for (EventSummary summary : service.events) {
                eventNames.add(summary.toString());
            }

            Spinner spinnerEvent = (Spinner) findViewById(R.id.spinnerEvent);

            // Specify the layout to use when the list of choices appears
            ArrayAdapter<String> eventAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, eventNames);
            // Apply the adapter to the spinner
            spinnerEvent.setAdapter(eventAdapter);
        } else if (adapterView.getId() == R.id.spinnerEvent) {
            this.targetEventId = service.events.get(pos).getId();
            Log.d(DEBUG_TAG, "Event selected");
        } else if (adapterView.getId() == R.id.spinnerTarget) {
            this.targetCalendarId = service.calendars.get(pos).getId();
            Log.d(DEBUG_TAG, "Target calendar selected");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        if ((targetEventId >= 0) && (targetCalendarId >=0)) {
            Log.d(DEBUG_TAG, "Copy event " + targetEventId + " to calendar " + targetCalendarId);
            this.service.copyEvent(targetEventId, targetCalendarId);
        } else {
            Log.w(DEBUG_TAG, "Event ID or Calendar ID not specified.");
        }
    }
}
