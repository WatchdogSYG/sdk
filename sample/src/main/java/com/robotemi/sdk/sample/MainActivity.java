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

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnConstraintBeWithStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;

import flinderstemi.util.GlobalVariables;
import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.StateMachine;
import flinderstemi.util.listeners.ReturnToChargeOnClickListener;

/**
 * MainActivity JavaDoc
 */
public class MainActivity extends AppCompatActivity implements
        OnConstraintBeWithStatusChangedListener,
        OnRobotReadyListener,
        SetTextViewCallback {

    /*******************************************************************************************
     *                                       Temi SDK                                          *
     ******************************************************************************************/

    private Robot robot;
    private SetTextViewCallback stvc;
    private StateMachine routine;

    /*******************************************************************************************
     *                                    Android Widgets                                      *
     ******************************************************************************************/

    private TextView textViewVariable;
    private Button operatorMenuButton;
    private Button startButton;
    private Button stopButton;
    private Button returnButton;
    private ViewFlipper vf;

    String thoughtPrefix;

    private MediaPlayer mp;

    /*******************************************************************************************
     *                                        Get/Set                                          *
     ******************************************************************************************/
    public TextView getTextViewVariable() {
        return textViewVariable;
    }

    public Button getStartButton() {
        return startButton;
    }

    public MediaPlayer getMediaPlayer() {
        return mp;
    }

    //TODO use this fn for all routine existence checks
    public boolean routineExists() {
        if (routine != null) {
            return true;
        } else {
            return false;
        }
    }

    /*******************************************************************************************
     *                                   UI Functionality                                      *
     ******************************************************************************************/

    /**
     * ViewFlipper Button to Operator Menu
     */
    private void toOpMenu() {
        vf.showNext();
    }

    /**
     * ViewFlipper Button to Main Menu.
     *
     * @param view
     */
    public void toMainMenu(View view) {
        vf.showPrevious();
    }

    /**
     * This method is called by the default OnClickListener of the main button: <code>startButton</code>.
     * It checks if the robot is charging. If it is charging, the robot will notify the user and prompt the user to click the button to alow auto start when battery is full similar to the ChargingHighOnClickListener.
     * //TODO make this more than cosmetic
     *
     * @param view The startButton that was clicked.
     */
    public void startStateMachine(View view) {
        //check what to initially do based on SOC
        BatteryData bd = robot.getBatteryData();
        int soc = bd.getBatteryPercentage();
        Log.i("BATTERY", Integer.toString(soc));
        if (soc <= GlobalVariables.SOC_LOW) {
            //low battery
            if (robot.getBatteryData().isCharging()) {
                //tell the user it is charging
                robot.speak(TtsRequest.create("Hello, I am low on battery. Press the button on the screen if you want me to start patrolling when my battery is full.", false));
                //set UI elements
                startButton.setText("Auto-start patrol when battery is full");
                //TODO set non-cosmetic functionality
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startButton.setText("Cancel Auto-Start");
                    }
                });
            } else {
                //not charging
                robot.speak(TtsRequest.create("Hello, I am low on battery and also am not connected to a charging source. Please send me back to the home base so I can charge myself. I can do this automatically if you press the button on the screen.", false));
                //Give feedback to the user that we are returning and disable further input
                stvc.updateThought("My battery is low and I am not charging...");
                startButton.setText("Tap to send me back to the home base");
                startButton.setOnClickListener(new ReturnToChargeOnClickListener(startButton, robot, this, routine, mp));
            }
            //start a SOCListener to detect when we should change the UI to the next stage
        } else {
            //enough battery
            //leave at default
            startRoutineFresh();
        }
    }

    /**
     * @param view
     */
    public void stopStateMachine(View view) {
        updateThought(getResources().getString(R.string.cTermination));
        routine.stop();
        stopButton.setEnabled(false);
        returnButton.setEnabled(false);
        startButton.setVisibility(View.VISIBLE);
        mp.stop();
    }

    /**
     * @param view
     */
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

    /**
     * @param view
     */
    public void returnToLauncher(View view) {
        if (routine != null) {
            routine.stop();
        }
        mp.stop();
        System.out.println("FLINTEMI: Calling finish(). Shutting down app immediately.");
        finish();
    }

    /**
     * @param view
     */
    public void debugArea(View view) {
        Log.i("Battery", Integer.toString(robot.getBatteryData().getBatteryPercentage()));
    }

    /*******************************************************************************************
     *                                    Initialisation                                       *
     ******************************************************************************************/

    /**
     * Called on Temi Android software initialisation.
     * Places this application in the top bar for a quick access shortcut.
     *
     * @param isReady
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

    /**
     * Called on Activity instantiation.
     * Sets <code>GlobalParameters</code> member variables to appropriate /res/values fields.
     * Verifies permissions.
     * Gets TemiSDK's Robot Instance.
     * Initialises UI View Elements.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //get Global Parameters for use in the app
        new GlobalVariables(this, robot);//call GP constr to get values from /res

        //Verify permissions here
        //do not need storage permissions for this app, maybe later to have some persistent options
        //verifyStoragePermissions(this);

        // get an instance of the robot in order to begin using its features.
        robot = Robot.getInstance();
        stvc = this;

        //initialise Views in UI
        initViews();
    }

    //TODO initialise based on stored options

    /**
     * Initialises views based on starting SOC and stored options.
     */
    public void initViews() {
        textViewVariable = findViewById(R.id.thoughtTextView);
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

    /**
     * Called on Activity start.
     * Sets Event Listeners.
     * Sets TemiSDK.Robot options.
     * Miscellaneous UI initialisations.
     */
    @Override
    protected void onStart() {
        super.onStart();
        robot.addOnRobotReadyListener(this);
        robot.addOnConstraintBeWithStatusChangedListener(this);

        robot.setDetectionModeOn(true, 2.0f);
        robot.setGoToSpeed(SpeedLevel.SLOW);

        //demo speak
        robot.hideTopBar();
        robot.setPrivacyMode(true);
        robot.toggleNavigationBillboard(false);

        thoughtPrefix = getResources().getString(R.string.cPrefix);
    }

    /**
     * Called when Activity stops.
     * Removes all event listeners.
     * Stops all other objects such as <code>MediaPlayer</code>s.
     * Stops robot movement.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (!(mp == null)) {
            mp.stop();
        }

        ///remember to remove all listeners
        robot.removeOnRobotReadyListener(this);
        robot.removeOnConstraintBeWithStatusChangedListener(this);
        //robot.removeDetectionStateChangedListener(this);
        robot.stopMovement();
    }

/*******************************************************************************************
 *                                    Behaviour Utils                                      *
 ******************************************************************************************/
    //TODO javadoc

    /**
     *
     */
    public StateMachine startRoutineFresh() {
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
        //TODO set the correct file for music
        mp = MediaPlayer.create(this, R.raw.twiceicsm);
        //mp = MediaPlayer.create(this, R.raw.dragonforcettfaf);
        //mp = MediaPlayer.create(this, R.raw.bensound_theelevatorbossanova);
        mp.setLooping(true);
        mp.setVolume(0.5f, 0.5f);
        mp.start();

        return routine;
    }

    /*******************************************************************************************
     *                              Debug and Sample Functions                                 *
     ******************************************************************************************/

    /**
     *
     * @param location
     * @param status
     * @param descriptionId
     * @param description
     */
    /*
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
    }*/

    /**
     *
     */
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
    }*/
    @Override
    public void onConstraintBeWithStatusChanged(boolean isConstraint) {
        Log.d("onConstraintBeWith", "status = " + isConstraint);
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
