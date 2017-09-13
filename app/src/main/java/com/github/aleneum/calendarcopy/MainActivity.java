package com.github.aleneum.calendarcopy;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.aleneum.calendarcopy.models.EventSummary;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, View.OnClickListener,
        AdapterView.OnItemClickListener {

    private static final String DEBUG_TAG = "ccopy.MainActivity";
    public static final int REQUEST_PERMISSIONS = 0;

    private CalendarService service;
    private SparseBooleanArray selectedEventsPos;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // // In case the database has to be reset
        // this.deleteDatabase(RelationDatabaseHelper.DATABASE_NAME);


        Log.d(DEBUG_TAG, "onCreate() ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(DEBUG_TAG, "requestPermissions ...");
        if (Utils.checkPermissions(this)) {
            runCalendarStuff();
        }
    }

    private void runCalendarStuff() {
        selectedEventsPos = new SparseBooleanArray();
        service = new CalendarService(this);
        service.clearDatabase();
        service.getCalendars();
        loadConfiguration();

        // Initialize Copy Button
        Button buttonCopy = (Button) findViewById(R.id.buttonCopy);
        buttonCopy.setOnClickListener(this);


        // Set target calendar
        Spinner targetSpinner = (Spinner) findViewById(R.id.spinnerTargetCalendar);
        ArrayAdapter<String> targetAdapter = new CalendarAdapter(this, service.getCalendarInfo());
        targetSpinner.setAdapter(targetAdapter);
        targetSpinner.setOnItemSelectedListener(this);
        targetSpinner.setSelection(service.getCalendarIds().indexOf(service.targetCalendarId));

        // Set event list
        ListView listEvents = (ListView) findViewById(R.id.listEvents);
        listEvents.setOnItemClickListener(this);

        // Initialize source calender, set selection to trigger list update
        Spinner calendarSpinner = (Spinner) findViewById(R.id.spinnerSourceCalendar);
        ArrayAdapter<String> calendarAdapter = new CalendarAdapter(this, service.getCalendarInfo());
        targetSpinner.setAdapter(targetAdapter);
        calendarSpinner.setAdapter(calendarAdapter);
        calendarSpinner.setOnItemSelectedListener(this);
        calendarSpinner.setSelection(service.getCalendarIds().indexOf(service.sourceCalendarId));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
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
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        int pos = adapterView.getSelectedItemPosition();
        if (adapterView.getId() == R.id.spinnerSourceCalendar) {
            Log.d(DEBUG_TAG, "Calendar selected: " + pos);
            service.sourceCalendarId = service.getCalendarIds().get(pos);
            refreshList();
        } else if (adapterView.getId() == R.id.spinnerTargetCalendar) {
            service.targetCalendarId = service.getCalendarIds().get(pos);
            refreshList();
            Log.d(DEBUG_TAG, "Target calendar selected");
        }
        findViewById(R.id.buttonCopy).setEnabled(
                service.sourceCalendarId != service.targetCalendarId);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void refreshList() {
        service.getEvents();

        ListView listEvents = (ListView) findViewById(R.id.listEvents);
        eventAdapter = new EventAdapter(this, service);
        listEvents.setAdapter(eventAdapter);
    }

    @Override
    public void onClick(View view) {
        for (int i=0, size = service.events.size(); i < size; ++i) {
            if (selectedEventsPos.get(i, false)) {
                long eventId = service.events.get(i).getId();
                Log.i(DEBUG_TAG, "Copy event " + eventId + " to calendar " + service.targetCalendarId);
                long targetEventId = service.copyEvent(eventId);
                if (targetEventId > -1) {
                    EventSummary summary = service.getEventById(eventId);
                    summary.childrenEventIds.add(targetEventId);
                    summary.childrenCalendarIds.add(service.targetCalendarId);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
        ((ListView) findViewById(R.id.listEvents)).invalidateViews();
    }

    private void loadConfiguration() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        service.sourceCalendarId = settings.getLong("lastSourceCalendarId", 0);
        service.targetCalendarId = settings.getLong("lastTargetCalendarId", 0);
    }

    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("lastSourceCalendarId", service.sourceCalendarId);
        editor.putLong("lastTargetCalendarId", service.targetCalendarId);

        // Commit the edits!
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.stop();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ListView listEvents = (ListView) adapterView;
        selectedEventsPos = listEvents.getCheckedItemPositions();
        Log.d(DEBUG_TAG, "Events selected: " + selectedEventsPos.toString());
        view.setSelected(selectedEventsPos.get(i));
    }
}
