package com.github.aleneum.calendarcopy;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


// TODO: Add parent circle
public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, View.OnClickListener,
        AdapterView.OnItemClickListener {

    public static String DEBUG_TAG = "ccopy.MainActivity";
    public static final int REQUEST_CALENDAR_READ = 0;
    public static final int REQUEST_CALENDAR_WRITE = 1;
    public static final String calendarMimeType = "text/x-vcalendar";

    public CalendarService service;
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
        if (Utils.hasPermission(this, Manifest.permission.READ_CALENDAR, REQUEST_CALENDAR_READ)) {
            runCalendarStuff();
        }
    }

    protected void runCalendarStuff() {
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
        ArrayAdapter<String> targetAdapter = new CalendarAdapter(this, service.getCalendarInfos());
        targetSpinner.setAdapter(targetAdapter);
        targetSpinner.setOnItemSelectedListener(this);
        targetSpinner.setSelection(service.getCalendarIds().indexOf(service.targetCalendarId));

        // Set event list
        ListView listEvents = (ListView) findViewById(R.id.listEvents);
        listEvents.setOnItemClickListener(this);

        // Initialize source calender, set selection to trigger list update
        Spinner calendarSpinner = (Spinner) findViewById(R.id.spinnerSourceCalendar);
        ArrayAdapter<String> calendarAdapter = new CalendarAdapter(this, service.getCalendarInfos());
        targetSpinner.setAdapter(targetAdapter);
        calendarSpinner.setAdapter(calendarAdapter);
        calendarSpinner.setOnItemSelectedListener(this);
        calendarSpinner.setSelection(service.getCalendarIds().indexOf(service.sourceCalendarId));
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
        if (adapterView.getId() == R.id.spinnerSourceCalendar) {
            Log.d(DEBUG_TAG, "Calendar selected: " + pos);
            service.sourceCalendarId = service.getCalendarIds().get(pos);
            refreshList();
        } else if (adapterView.getId() == R.id.spinnerTargetCalendar) {
            service.targetCalendarId = service.getCalendarIds().get(pos);
            refreshList();
            Log.d(DEBUG_TAG, "Target calendar selected");
        }
        TextView tview = (TextView) adapterView.getChildAt(0);
        if (tview != null) {
            tview.setTextColor(Color.rgb(255, 255, 255));
        }
        findViewById(R.id.buttonCopy).setEnabled(
                service.sourceCalendarId != service.targetCalendarId);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void refreshList() {
        service.getEvents();
        List<String> eventNames = new ArrayList<String>();

        for (EventSummary summary : service.events) {
            eventNames.add(summary.toString());
        }

        ListView listEvents = (ListView) findViewById(R.id.listEvents);

        // Specify the layout to use when the list of choices appears
        eventAdapter = new EventAdapter(this, service);
        // Apply the adapter to the spinner
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
        editor.commit();
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


/**
 * Working with Indents does not work as expected since not all calendars allow to
 * share entries. Sony's calendar shares a vCal version which requires manually parsing
 * and also does not contain internal info such as _ID -> postpone this until this features
 * becomes more vital.
 **/


// <intent-filter>
// <action android:name="android.intent.action.SEND" />
// <category android:name="android.intent.category.DEFAULT" />
// <data android:mimeType="text/x-vCalendar" />
// </intent-filter>
//
// Intent intent = getIntent();
// String action = intent.getAction();
// String type = intent.getType();
//
// if (Intent.ACTION_SEND.equals(action) && type != null) {
// Log.d(DEBUG_TAG, "Dumping Intent start");
// Log.d(DEBUG_TAG, "Action: "+ intent.getAction());
// Log.d(DEBUG_TAG, "URI: "+ intent.getDataString());
// Log.d(DEBUG_TAG, "Type: "+ intent.getType());
// Bundle bundle = intent.getExtras();
// if (bundle != null) {
// Set<String> keys = bundle.keySet();
// Iterator<String> it = keys.iterator();
// while (it.hasNext()) {
// String key = it.next();
// Log.d(DEBUG_TAG, "[" + key + "=" + bundle.get(key) + "]");
// }
// }
// Uri vcsUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
// try {
// InputStream is = this.getContentResolver().openInputStream(vcsUri);
// String vCalString = convertStreamToString(is);
// Log.d(DEBUG_TAG, "String: " + vCalString);
// VCal entry = VCal.parse(vCalString);
// entry.dump();
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// }
//
//
// }
//             } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
//                 if (type.startsWith("image/")) {
//                     handleSendMultipleImages(intent); // Handle multiple images being sent
//                 }
//             }

//static String convertStreamToString(InputStream is) {
//    StringBuilder textBuilder = new StringBuilder();
//    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//
//    try {
//        String line = "";
//        while ((line = reader.readLine()) != null) {
//            Log.d(DEBUG_TAG, "Line: " + line);
//        }
//    } catch (IOException err) {
//        return "";
//    }
//    return "";
//}
//
//    static void parseVCal(String vcalString) {
//        String[] entries = vcalString.split("\n");
//        for (String entry: entries) {
//            String[] kv = entry.split(":");
//            Log.d(DEBUG_TAG, "Key: " + kv[0] + " Value: " + kv[1]);
//        }
//    }
