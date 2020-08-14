package com.robotemi.sdk.sample;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
import com.robotemi.sdk.listeners.OnConstraintBeWithStatusChangedListener;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.StateMachine;

public class MainActivity extends AppCompatActivity implements
        OnBeWithMeStatusChangedListener,
        OnConstraintBeWithStatusChangedListener,
        OnDetectionStateChangedListener,
        OnRobotReadyListener,
        SetTextViewCallback {

    /*******************************************************************************************
     *                                       Temi SDK                                          *
     ******************************************************************************************/

    private Robot robot;
    private StateMachine routine;

    /*******************************************************************************************
     *                                    Android Widgets                                      *
     ******************************************************************************************/

    private TextView textViewVariable;
    private Button operatorMenuButton;
    private TextView faceTextView;//temp
    private Button startButton;
    private Button stopButton;
    private Button returnButton;
    private ViewFlipper vf;

    String thoughtPrefix;

    /*******************************************************************************************
     *                                    Functionality                                        *
     ******************************************************************************************/

    public void initViews() {
        textViewVariable = findViewById(R.id.thoughtTextView);
        faceTextView = findViewById(R.id.face);
        startButton = findViewById(R.id.btnCustom);
        stopButton = findViewById(R.id.btnStop);
        returnButton = findViewById(R.id.btnRet);
        vf = findViewById(R.id.vf);
        operatorMenuButton = findViewById(R.id.menu);
        operatorMenuButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toOpMenu();
                return true;
            }
        });
    }

    private void toOpMenu() {
        vf.showNext();
    }

    public void toMainMenu(View view) {
        vf.showPrevious();
    }

    /**
     * This custom method implements the "patrol" functionality.
     *
     * @param view
     */
    MediaPlayer mp;
    public void startStateMachine(View view) {
        startButton.setVisibility(View.GONE);
        stopButton.setEnabled(true);
        returnButton.setEnabled(true);
        textViewVariable.setPadding(200, 0, 0, 0);
        textViewVariable.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        routine = new StateMachine(robot, this);
        System.out.println("FLINTEMI: Create Initialisation Routine");
        updateThought(getResources().getString(R.string.cInit));
        synchronized (routine) {
            new Thread(routine).start();
        }
        mp = MediaPlayer.create(this, R.raw.bensound_theelevatorbossanova);
        mp.setLooping(true);
        mp.start();
    }

    public void stopStateMachine(View view) {
        updateThought(getResources().getString(R.string.cTermination));
        routine.stop();
        stopButton.setEnabled(false);
        returnButton.setEnabled(false);
        startButton.setVisibility(View.VISIBLE);
        mp.stop();
    }

    public void ReturnToBase(View view) {
        updateThought(getResources().getString(R.string.cReturn));
        //TODO fix null obj ref
        if (routine == null) {
            //is this rigorous enough, can we remove/modify the home base
            robot.goTo("home base");
        } else {
            robot.goTo("home base");
        }
    }

    public void returnToLauncher(View view) {
        routine.stop();
        mp.stop();
        System.out.println("FLINTEMI: Calling finish(). Shutting down app immediately.");
        finish();
    }

    /*******************************************************************************************
     *                                    Initialisation                                       *
     ******************************************************************************************/

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

        thoughtPrefix = getResources().getString(R.string.cPrefix);
    }

    /**
     * Removing the event listeners upon leaving the app.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mp.stop();
        robot.removeOnRobotReadyListener(this);
        robot.removeOnBeWithMeStatusChangedListener(this);
        robot.removeOnConstraintBeWithStatusChangedListener(this);
        robot.removeDetectionStateChangedListener(this);
        robot.stopMovement();
    }

    /*******************************************************************************************
     *                                   Sample Functions                                      *
     ******************************************************************************************/

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
            Log.d(this.getClass().getName(), "onDetectionState = DETECTED");
            //TODO move this listener to another class and into the statemachine
            robot.speak(TtsRequest.create("Hello there, feel free to use my touchless sanitiser dispenser. Maintaining good hand hygiene is an important measure we can take to prevent infectious diseases from spreading.", false));
        } else {
            robot.stopMovement();
        }
    }

    /*******************************************************************************************
     *                         Manual Callback Interface Overrides                             *
     ******************************************************************************************/

    @Override
    public void updateThought(final String string) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO use string res with placeholders
                System.out.println("FLINTEMI: setText to \"" + string + "\"");
                textViewVariable.setText(thoughtPrefix + string);
            }
        });
    }
}
