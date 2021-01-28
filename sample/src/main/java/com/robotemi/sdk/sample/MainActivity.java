package com.robotemi.sdk.sample;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.provider.FontRequest;
import androidx.emoji.text.EmojiCompat;
import androidx.emoji.text.FontRequestEmojiCompatConfig;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;
import com.robotemi.sdk.listeners.OnConstraintBeWithStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;
import com.robotemi.sdk.permission.Permission;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import flinderstemi.Global;
import flinderstemi.LanguageID;
import flinderstemi.StateMachine;
import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.listeners.BatteryStateListener;

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

    private TextView thoughtTextView;
    private TextView faceTextView;
    private Button operatorMenuButton;
    private Button startButton;
    private Button stopButton;
    private Button returnButton;
    private ViewFlipper vf;

    String thoughtPrefix;

    /*******************************************************************************************
     *                                         State                                           *
     ******************************************************************************************/

    private int lang;
    private int vfid;

    /*******************************************************************************************
     *                                        Get/Set                                          *
     ******************************************************************************************/

    public Button getStartButton() {
        return startButton;
    }

    public int getLang() {
        return lang;
    }

    /*******************************************************************************************
     *                                   UI Functionality                                      *
     ******************************************************************************************/

    /**
     * ViewFlipper Button to Operator Menu
     */
    private void toOpMenu() {
        if (vfid == 1) {
            vf.showNext();
            vfid = 2;
        } else if (vfid == 3) {
            vf.showPrevious();
            vfid = 2;
        }
    }

    /**
     * ViewFlipper Button to Main Menu.
     *
     * @param view
     */
    public void toMainMenu(View view) {
        if (vfid == 2) {
            vf.showPrevious();
            vfid = 1;
        } else if (vfid == 3) {
            vf.showPrevious();
            vfid = 2;
            vf.showPrevious();
            vfid = 1;
        }
    }

    public void toAbout(View view) {
        if (vfid == 1) {
            vf.showNext();
            vfid = 2;
            vf.showNext();
            vfid = 3;
        } else if (vfid == 2) {
            vf.showNext();
            vfid = 3;
        }
    }

    /**
     * This method is called by the default OnClickListener of the main button: <code>startButton</code>.
     * It checks if the robot is charging. If it is charging, the robot will notify the user and prompt the user to click the button to allow auto start when battery is full similar to the ChargingHighOnClickListener.
     *
     * @param view The startButton that was clicked.
     */
    public void startStateMachine(View view) {
        //check what to initially do based on SOC
        BatteryData bd = robot.getBatteryData();
        int soc = bd.getBatteryPercentage();
        Log.i("BATTERY", Integer.toString(soc));

        if (soc <= Global.SOC_LOW) {
            //low battery
            if (robot.getBatteryData().isCharging()) {
                //tell the user it is charging
                robot.speak(TtsRequest.create("Hello, I am low on battery. Press the button on the screen if you want me to start patrolling when my battery is full.", false));
                //set UI elements
                startButton.setText("Auto-start patrol when battery is full");
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startButton.setText("Cancel Auto-Start");
                        updateThought("Charging... will auto start", Global.Emoji.eSleeping);

                        robot.addOnBatteryStatusChangedListener(new WaitBatteryListener());
                    }
                });
            } else {
                //not charging
                robot.speak(TtsRequest.create("Hello, I am low on battery and also am not connected to a charging source. Please send me back to the home base so I can charge myself. I can do this automatically if you press the button on the screen.", false));
                //Give feedback to the user that we are returning and disable further input
                stvc.updateThought("My battery is low and I am not charging...", Global.Emoji.eWorried);
                startButton.setText("Tap to send me back to the home base");
                routine = new StateMachine(robot, this, StateMachine.RETURNING);
                synchronized (routine) {
                    new Thread(routine).start();
                }
                //startButton.setOnClickListener(new ReturnToChargeOnClickListener(this, robot, routine, startButton));
            }
            //start a SOCListener to detect when we should change the UI to the next stage
        } else {
            //enough battery
            //leave at default
            startRoutineFresh();
        }
    }

    //TODO prevent duplicate SMs when implementing this OCL
    private class WaitBatteryListener implements OnBatteryStatusChangedListener {
        @Override
        public void onBatteryStatusChanged(@Nullable BatteryData batteryData) {
            int soc = batteryData.getBatteryPercentage();
            if (BatteryStateListener.batteryState(soc) >= BatteryStateListener.FULL) {
                startRoutineFresh();
            }
        }
    }

    /**
     * @param view
     */
    public void stopStateMachine(View view) {
        updateThought(getResources().getString(R.string.cTermination), Global.Emoji.eTear);
        routine.stop();
        stopButton.setEnabled(false);
        returnButton.setEnabled(false);
        startButton.setVisibility(View.VISIBLE);
    }

    /**
     * @param view
     */
    public void ReturnToBase(View view) {
        updateThought(getResources().getString(R.string.cReturn), Global.Emoji.eRobot);
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
        try {
            routine.stop();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

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
     *                               Program Initialisation                                    *
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
            robot.requestToBeKioskApp();
        }
    }

    /**
     * Called on Activity instantiation.
     * <p>
     * Sets <code>GlobalParameters</code> member variables to appropriate /res fields.
     * Verifies permissions.
     * Gets TemiSDK's Robot Instance.
     * Initialises UI View Elements.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get Global Parameters for use in the app
        new Global(this, robot);//call GP constr to get values from /res

        FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "emoji compat Font Query",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest);
        config.registerInitCallback(new EmojiCompat.InitCallback() {

        });
        EmojiCompat.init(config);
        Log.v(Global.SYSTEM, Integer.toString(EmojiCompat.get().getLoadState()));
        //TODO locale?
        lang = LanguageID.EN;
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        //Verify permissions here
        //do not need storage permissions for this app, maybe later to have some persistent options or debug
        //verifyStoragePermissions(this);
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Global.SYSTEM, "READ_EXTERNAL_STORAGE GRANTED");
        } else {
            Log.d(Global.SYSTEM, "READ_EXTERNAL_STORAGE REQUESTING");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Global.SYSTEM, "WRITE_EXTERNAL_STORAGE GRANTED");
        } else {
            Log.d(Global.SYSTEM, "WRITE__EXTERNAL_STORAGE REQUESTING");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Global.SYSTEM, "CAPTURE_AUDIO_OUTPUT GRANTED");
        } else {
            Log.d(Global.SYSTEM, "CAPTURE_AUDIO_OUTPUT REQUESTING");
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }*/

        // get an instance of the robot in order to begin using its features.
        robot = Robot.getInstance();
        stvc = this;

        if (robot.checkSelfPermission(Permission.SETTINGS) == PackageManager.PERMISSION_DENIED) {
            Log.d(Global.SYSTEM, "SETTINGS PERMISSION DENIED");
            ArrayList<Permission> p = new ArrayList<Permission>();
            p.add(Permission.SETTINGS);
            robot.requestPermissions(p, 0);
        }


        //initialise Views in UI
        initViews();
    }

    //TODO initialise based on stored options

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

        //settings
        robot.setDetectionModeOn(true, 2.0f);
        robot.setGoToSpeed(SpeedLevel.SLOW);
        robot.hideTopBar();
        robot.setPrivacyMode(true);
        robot.toggleNavigationBillboard(false);
        robot.setTopBadgeEnabled(false);
        robot.toggleWakeup(true);

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

        ///remember to remove all listeners
        robot.removeOnRobotReadyListener(this);
        robot.removeOnConstraintBeWithStatusChangedListener(this);
        //robot.removeDetectionStateChangedListener(this);
        robot.stopMovement();
    }

    /*******************************************************************************************
     *                                    Behaviour Utils                                      *
     ******************************************************************************************/

    /**
     * Initialises views based on starting SOC and stored options.
     */
    public void initViews() {
        thoughtTextView = findViewById(R.id.thoughtTextView);
        faceTextView = findViewById(R.id.face);
        startButton = findViewById(R.id.btnCustom);
        stopButton = findViewById(R.id.btnStop);
        returnButton = findViewById(R.id.btnRet);
        vfid = 1;
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

    //TODO javadoc

    /**
     *
     */
    public StateMachine startRoutineFresh() {
        startButton.setVisibility(View.INVISIBLE);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        returnButton.setEnabled(true);
        thoughtTextView.setPadding(200, 0, 0, 0);
        thoughtTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        routine = new StateMachine(robot, this, StateMachine.GREETING);
        System.out.println("FLINTEMI: Create Initialisation Routine");
        updateThought(getResources().getString(R.string.cInit), Global.Emoji.eThinking);
        synchronized (routine) {
            new Thread(routine).start();
        }
        return routine;
    }

    private void setEn() {
        lang = LanguageID.EN;
        updateThought(getApplicationContext().getResources().getString(R.string.en_prompt), Global.Emoji.eGrinning);
    }

    /*******************************************************************************************
     *                              Debug and Sample Functions                                 *
     ******************************************************************************************/

    @Override
    public void onConstraintBeWithStatusChanged(boolean isConstraint) {
        Log.d(Global.STATE, "ConstraintBeWithStatus\t=\t" + isConstraint);
    }

    /*******************************************************************************************
     *                         Manual Callback Interface Overrides                             *
     ******************************************************************************************/

    /**
     * Callback interface override allows changing the text on <code>textViewVariable</code> with a prefix defined from <code>thoughtPrefix</code>.
     *
     * @param string The text string that is to be displayed after the prefix.
     */
    @Override
    public void updateThought(final String string, final String emoji) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO use string res with placeholders
                //TODO remove Thoughtprefix
                Log.d(Global.UI, "Thought\t=\t\"" + string + "\"");
                thoughtTextView.setText(string);
                faceTextView.setText(emoji);

            }
        });
    }
}
