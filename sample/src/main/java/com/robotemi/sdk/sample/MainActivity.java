package com.robotemi.sdk.sample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
import com.robotemi.sdk.listeners.OnConstraintBeWithStatusChangedListener;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import org.jetbrains.annotations.NotNull;

import flinderstemi.util.InitialisationSequence;

public class MainActivity extends AppCompatActivity implements OnBeWithMeStatusChangedListener, OnConstraintBeWithStatusChangedListener, OnDetectionStateChangedListener, OnGoToLocationStatusChangedListener, OnRobotReadyListener {
    public EditText etSpeak;
    private Robot robot;
    private TextView textView;

    //TODO abstract these away neatly

    /**
     * This custom method implements the "patrol" functionality.
     *
     * @param view
     */
    public void custom(View view) {

        //this listener does not provide a way to select a specific tts request?
        InitialisationSequence routine = new InitialisationSequence(robot);
        System.out.println("Create Routine");
        synchronized (routine) {
            new Thread(routine).start();
            System.out.println("Started");
            Robot.TtsListener l = new ttsListener(robot, routine);
            System.out.println("Add new Listener");
            robot.addTtsListener(l);
            System.out.println("Added new Listener");
        }


        //robot.speak(TtsRequest.create("I will start my patrol.", false));
        /*robot.speak(TtsRequest.create("Getting saved locations.", false));
        //get locations
        List<String> locations;
        locations = robot.getLocations();
        if (locations.isEmpty()) {
            robot.speak(TtsRequest.create("No locations saved. I will turn on the spot instead", false));
            robot.turnBy(360);
        } else {
            //go to each location in the list n times
            int n = 2;
            //patrol loops
            for (int i = 0; i < 2; i++) {
                //location loop
                for (int j = 0; j < locations.size(); j++) {
                    String l = locations.get(i);
                    robot.speak(TtsRequest.create("Going to location named " + l + " with index " + j, true));
                    robot.goTo(l);
                }
                robot.speak(TtsRequest.create("I am finished with patrol loop " + i, true));
            }
            robot.speak(TtsRequest.create("I am done with my patrol.", true));
        }*/
    }

    public static class ttsListener implements Robot.TtsListener {
        InitialisationSequence ls;
        Robot robot;

        public ttsListener(Robot r, InitialisationSequence ls) {
            this.ls = ls;
            this.robot = r;
        }

        @Override
        public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
            synchronized (ls) {
                //if the status of the current ttsrequest changes to COMPLETED
                System.out.println("TtsRequest.getStatus()=" + ttsRequest.getStatus() + ":" + ttsRequest.getSpeech());
                if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED) {
                    ls.notify();
                    //if the speech routine is complete, remove the listener
                    if (ls.isCompleteSpeechSub()) {
                        System.out.println("ListenerRemoved");
                        ls.notify();
                        robot.removeTtsListener(this);
                    }
                }

            }
        }
    }

    /**************************************************************************************************************************
     * System calls and utilities
     *************************************************************************************************************************/

    /**
     * Hiding keyboard after every button press
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     *//*
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }*/

    /**
     * Setting up all the event listeners
     */
    @Override
    protected void onStart() {
        super.onStart();
        robot.addOnRobotReadyListener(this);
        robot.addOnBeWithMeStatusChangedListener(this);
        robot.addOnGoToLocationStatusChangedListener(this);
        robot.addOnConstraintBeWithStatusChangedListener(this);
        robot.addOnDetectionStateChangedListener(this);

        //demo speak
        robot.hideTopBar();
        robot.toggleNavigationBillboard(true);
        robot.speak(TtsRequest.create("Hello, World. This is when onStart functions are called.", true));

        textView.append("Initialising");
    }

    /**
     * Removing the event listeners upon leaving the app.
     */
    @Override
    protected void onStop() {
        super.onStop();
        robot.removeOnRobotReadyListener(this);
        robot.removeOnBeWithMeStatusChangedListener(this);
        robot.removeOnGoToLocationStatusChangedListener(this);
        robot.removeOnConstraintBeWithStatusChangedListener(this);
        robot.removeDetectionStateChangedListener(this);
        robot.stopMovement();

        //demo speak
        robot.speak(TtsRequest.create("Hello, World. This is when onStop functions are called.", true));
    }

    /**
     * Places this application in the top bar for a quick access shortcut.
     */
    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                robot.onStart(activityInfo);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initViews();

        //do not need storage permissions for this app
        //verifyStoragePermissions(this);
        robot = Robot.getInstance(); // get an instance of the robot in order to begin using its features.
    }

    /**************************************************************************************************************************
     * Sample functionality
     *************************************************************************************************************************/

    public void initViews() {
        textView = findViewById(R.id.textView);
    }

    /**
     * Have the robot speak while displaying what is being said.
     */
    public void speak(View view) {
        TtsRequest ttsRequest = TtsRequest.create(etSpeak.getText().toString().trim(), true);
        robot.speak(ttsRequest);
        hideKeyboard(MainActivity.this);
    }


    @Override
    public void onBeWithMeStatusChanged(String status) {
        //  When status changes to "lock" the robot recognizes the user and begin to follow.
        switch (status) {
            case "abort":
                // do something i.e. speak
                robot.speak(TtsRequest.create("Abort", false));
                break;

            case "calculating":
                robot.speak(TtsRequest.create("Calculating", false));
                break;

            case "lock":
                robot.speak(TtsRequest.create("Lock", false));
                break;

            case "search":
                robot.speak(TtsRequest.create("search", false));
                break;

            case "start":
                robot.speak(TtsRequest.create("Start", false));
                break;

            case "track":
                robot.speak(TtsRequest.create("Track", false));
                break;
        }
    }

    @Override
    public void onGoToLocationStatusChanged(String location, String status, int descriptionId, String description) {
        Log.d("GoToStatusChanged", "descriptionId=" + descriptionId + ", description=" + description);
        switch (status) {
            case "start":
                robot.speak(TtsRequest.create("Starting", false));
                break;

            case "calculating":
                robot.speak(TtsRequest.create("Calculating", false));
                break;

            case "going":
                robot.speak(TtsRequest.create("Going", false));
                break;

            case "complete":
                robot.speak(TtsRequest.create("Completed", false));
                break;

            case "abort":
                robot.speak(TtsRequest.create("Cancelled", false));
                break;
        }
    }

    @Override
    public void onConstraintBeWithStatusChanged(boolean isConstraint) {
        Log.d("onConstraintBeWith", "status = " + isConstraint);
    }

    @Override
    public void onDetectionStateChanged(int state) {
        Log.d("onDetectionStateChanged", "state = " + state);
        if (state == DETECTED) {
            robot.constraintBeWith();
        } else {
            robot.stopMovement();
        }
    }
}
