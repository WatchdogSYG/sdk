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
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.StateMachine;

public class MainActivity extends AppCompatActivity implements OnBeWithMeStatusChangedListener, OnConstraintBeWithStatusChangedListener, OnDetectionStateChangedListener, OnRobotReadyListener, SetTextViewCallback {
    public EditText etSpeak;
    private Robot robot;
    private TextView textViewVariable;

    /**
     * This custom method implements the "patrol" functionality.
     *
     * @param view
     */
    StateMachine routine;

    public void custom(View view) {
        routine = new StateMachine(robot, this);
        System.out.println("FLINTEMI: Create Initialisation Routine");
        updateThought("Current Action: Initialising");
        synchronized (routine) {
            new Thread(routine).start();
        }
    }

    public void stopStateMachine(View view) {
        routine.stop();
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

    /**
     * Setting up all the event listeners
     */
    @Override
    protected void onStart() {
        super.onStart();
        robot.addOnRobotReadyListener(this);
        robot.addOnBeWithMeStatusChangedListener(this);
        robot.addOnConstraintBeWithStatusChangedListener(this);
        robot.addOnDetectionStateChangedListener(this);

        //demo speak
        robot.hideTopBar();
        robot.setPrivacyMode(true);
        robot.toggleNavigationBillboard(true);

        textViewVariable.setText("Current Action: onStart");
    }

    /**
     * Removing the event listeners upon leaving the app.
     */
    @Override
    protected void onStop() {
        super.onStop();
        robot.removeOnRobotReadyListener(this);
        robot.removeOnBeWithMeStatusChangedListener(this);
        robot.removeOnConstraintBeWithStatusChangedListener(this);
        robot.removeDetectionStateChangedListener(this);
        robot.stopMovement();

        //demo speak
        robot.speak(TtsRequest.create("Hello, World. This is when onStop functions are called.", false));
    }

    public void initViews() {
        textViewVariable = findViewById(R.id.textView);
    }
    /**************************************************************************************************************************
     * Sample functionality
     *************************************************************************************************************************/

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

    /*
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
                    robot.speak(TtsRequest.create("Abort", false));
                    break;
            }
        }
    */
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

    @Override
    public void updateThought(String string) {
        System.out.println("FLINTEMI: setText to \"" + string + "\"");
        textViewVariable.setText(string);
    }
}
