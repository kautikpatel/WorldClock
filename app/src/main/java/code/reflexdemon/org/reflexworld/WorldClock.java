package code.reflexdemon.org.reflexworld;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WorldClock extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String DEFAULT_TIME_ZONE_KEY = "defaultTimeZone";
    private static final int[] GRAVITY_LIST = {Gravity.CENTER, Gravity.TOP, Gravity.BOTTOM, Gravity.LEFT, Gravity.RIGHT};
    private static int gravityPointer = 0;
    private final Handler mHideHandler = new Handler();
    private TextClock currentLocalTime;
    private TextClock currentTimeZoneTime;
    private long miliSeconds;
    private ArrayAdapter<String> idAdapter;
    private TextView textTimeZone;
    private Spinner spinnerAvailableID;
    private Calendar current;
    private SimpleDateFormat sdf;
    private Date resultdate;
    private TableLayout tableMaincontent;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            currentLocalTime.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_world_clock);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        currentLocalTime = (TextClock) findViewById(R.id.currentLocalTime);
        currentTimeZoneTime = (TextClock) findViewById(R.id.currentTimeZoneTime);
        spinnerAvailableID = (Spinner) findViewById(R.id.availableID);
        spinnerAvailableID.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));

        textTimeZone = (TextView) findViewById(R.id.timezone);
        textTimeZone.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.black_overlay, null));
        textTimeZone.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        // Set up the user interaction to manually show or hide the system UI.
        tableMaincontent = (TableLayout) findViewById(R.id.tableMaincontent);
        tableMaincontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        loadImages();
        String[] idArray = TimeZone.getAvailableIDs();
        sdf = new SimpleDateFormat(getString(R.string.Time_Format));

        idAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, idArray);

        idAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailableID.setAdapter(idAdapter);
        String savedValue = getSavedValue(DEFAULT_TIME_ZONE_KEY);
        spinnerAvailableID.setSelection(Arrays.asList(idArray).indexOf(savedValue), true);
        getGMTTime();
        setSelectedText(TimeZone.getTimeZone(savedValue));

        spinnerAvailableID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        getGMTTime();
                        String selectedId = (String) (parent
                                .getItemAtPosition(position));

                        TimeZone timezone = TimeZone.getTimeZone(selectedId);
                        setSelectedText(timezone);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }
                });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.dummy_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callOnExit();
                    }
                }
        );
    }

    private void setSelectedText(TimeZone timezone) {

        String TimeZoneName = timezone.getDisplayName();

        int TimeZoneOffset = timezone.getRawOffset()
                / (60 * 1000);

        int hrs = TimeZoneOffset / 60;
        int mins = TimeZoneOffset % 60;

        miliSeconds = miliSeconds + timezone.getRawOffset();

        resultdate = new Date(miliSeconds);
        System.out.println(sdf.format(resultdate));

        textTimeZone.setText(TimeZoneName + " : GMT " + hrs + "."
                + mins);
        currentTimeZoneTime.setTimeZone(timezone.getID());
        saveTimeZonePrefs(DEFAULT_TIME_ZONE_KEY, timezone.getID());
        currentTimeZoneTime.setFormat12Hour(getString(R.string.Time_Format));
        miliSeconds = 0;
    }

    private void loadImages() {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        new LoadImage(imageView).execute();
    }

    private String getSavedValue(String key) {
        return PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .getString(key, getString(R.string.TimeZone_IST));
    }

    private void saveTimeZonePrefs(String key, String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        settings.edit().putString(key, value).apply();
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        currentLocalTime.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    // Convert Local Time into GMT time

    private void getGMTTime() {
        current = Calendar.getInstance();
//        currentLocalTime.setText("" + current.getTime());
        currentLocalTime.setFormat12Hour(getString(R.string.Time_Format));

        miliSeconds = current.getTimeInMillis();

        TimeZone tzCurrent = current.getTimeZone();
        int offset = tzCurrent.getRawOffset();
        if (tzCurrent.inDaylightTime(new Date())) {
            offset = offset + tzCurrent.getDSTSavings();
        }

        miliSeconds = miliSeconds - offset;

        resultdate = new Date(miliSeconds);
        Log.i("Current:", sdf.format(resultdate));
    }

    @Override
    public void onBackPressed() {
       callOnExit();
    }

    private void callOnExit() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.exitMessageTitle);
        alertDialogBuilder
                .setMessage(R.string.exitMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
