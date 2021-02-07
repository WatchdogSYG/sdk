package com.robotemi.sdk.sample;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;
import com.robotemi.sdk.permission.Permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import flinderstemi.Global;
import flinderstemi.LanguageID;
import flinderstemi.StateMachine;
import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.listeners.BatteryStateListener;

import static flinderstemi.LanguageID.EN;

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

    /*******************************************************************************************
     *                                         State                                           *
     ******************************************************************************************/

    private int lang;
    List<ImageButton> langButtons;

    Context c;
    Resources r;

    /*******************************************************************************************
     *                                        Get/Set                                          *
     ******************************************************************************************/

    public Button getStartButton() {
        return startButton;
    }

    public Button getStopButton() {
        return stopButton;
    }

    public Button getReturnButton() {
        return returnButton;
    }

    /*******************************************************************************************
     *                                   UI Functionality                                      *
     ******************************************************************************************/

    /**
     * ViewFlipper Button to Main Menu.
     *
     * @param view
     */
    public void toMainMenu(View view) {
        vf.setDisplayedChild(0);
    }

    /**
     *
     */
    public void toMainMenu() {
        vf.setDisplayedChild(0);
    }

    /**
     * ViewFlipper Button to Operator Menu
     */
    private void toOpMenu() {
        vf.setDisplayedChild(1);
    }

    /**
     * ViewFlipper Button to "About" screen.
     *
     * @param view
     */
    public void toAbout(View view) {
        vf.setDisplayedChild(2);
    }

    /**
     * This method is called by the default OnClickListener of the main button: <code>startButton</code>.
     * It checks if the robot is charging. If it is charging, the robot will notify the user and
     * prompt the user to click the button to allow auto start when battery is full similar to the
     * <code>ChargingHighOnClickListener</code>.
     *
     * @param view The startButton that was clicked.
     */
    public void startStateMachine(View view) {
        //check what to initially do based on SOC
        BatteryData bd = robot.getBatteryData();
        int soc = bd.getBatteryPercentage();
        Log.i(Global.BATTERY, "SOC\t=\t" + soc);

        if (soc <= Global.SOC_LOW) {
            //low battery
            if (robot.getBatteryData().isCharging()) {
                //tell the user it is charging
                robot.speak(TtsRequest.create(r.getString(R.string.lowCharging), false));
                //set UI elements
                startButton.setText(r.getString(R.string.b_turnOnAutoStart));
                updateThought(r.getString(R.string.t_chargingASOff), Global.Emoji.eSleeping);
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startButton.setEnabled(false);
                        startButton.setVisibility(View.INVISIBLE);
                        updateThought(r.getString(R.string.t_chargingASOn), Global.Emoji.eSleeping);

                        robot.addOnBatteryStatusChangedListener(new WaitBatteryListener());
                    }
                });
            } else {
                //not charging
                robot.speak(TtsRequest.create(r.getString(R.string.lowNotCharging), false));
                stvc.updateThought(r.getString(R.string.lowCharging), Global.Emoji.eWorried);
                startButton.setText(r.getString(R.string.b_returnRoutine));
                routine = new StateMachine(robot, this, StateMachine.RETURNING);
                synchronized (routine) {
                    new Thread(routine).start();
                }
            }
        } else {
            //enough battery, leave at default
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
     * @param view The view that was clicked
     */
    public void stopStateMachine(View view) {
        updateThought(getResources().getString(R.string.cTermination), Global.Emoji.eTear);
        if (routine != null) {
            routine.stop();
            routine = null;
        }
        stopButton.setEnabled(false);
        returnButton.setEnabled(false);
        startButton.setVisibility(View.VISIBLE);
        startButton.setEnabled(true);
    }

    /**
     * @param view The view that was clicked
     */
    public void ReturnToBase(View view) {
        updateThought(getResources().getString(R.string.cReturn), Global.Emoji.eRobot);
        stopButton.setEnabled(false);
        routine.setState(StateMachine.TERMINATED);

        if (routine != null) {
            routine.stop();
            routine = null;
        }

        robot.addOnGoToLocationStatusChangedListener(new OnGoToLocationStatusChangedListener() {
            @Override
            public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {
                if ((location == "home base") & (status == COMPLETE)) {
                    startButton.setVisibility(View.VISIBLE);
                    startButton.setEnabled(true);
                    returnButton.setEnabled(false);
                    updateThought(r.getString(R.string.en_prompt), Global.Emoji.eGrinning);
                    toMainMenu();
                    robot.removeOnGoToLocationStatusChangedListener(this);
                }
            }
        });

        robot.goTo("home base");
    }

    /**
     * Stops the routine and calls finish() on the main Activity.
     * @param view The view that was clicked
     */
    public void returnToLauncher(View view) {
        if (routine != null) {
            routine.stop();
        }
        Log.d(Global.SYSTEM, "finish(). Shutting down app immediately. If Kiosk mode is on, and it is set to this app the app should restart.");
        finish();
    }

    /**
     * @param view
     */
    public void debugArea(View view) {
        Log.i(Global.BATTERY, "SOC\t=\t" + robot.getBatteryData().getBatteryPercentage());
    }

    /*******************************************************************************************
     *                               Program Initialisation                                    *
     ******************************************************************************************/

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
        new Global(this);//call GP constr to get values from /res

        FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "emoji compat Font Query",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(this, fontRequest);
        config.registerInitCallback(new EmojiCompat.InitCallback() {

        });
        EmojiCompat.init(config);
        Log.v(Global.SYSTEM, "EmojiCompat LoadState\t=\t" + Integer.toString(EmojiCompat.get().getLoadState()));

        //This needs Android locale support to be done rubustly. Since we are only changing one line for 5 langs on button press, this is not necessary for this project.
        lang = EN;

        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // get an instance of the robot in order to begin using its features.
        robot = Robot.getInstance();
        stvc = this;

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

        c = getApplicationContext();
        r = c.getResources();
    }

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

        ArrayList<Permission> p = new ArrayList<Permission>();
        p.add(Permission.SETTINGS);
        robot.requestPermissions(p, 0);

        if (robot.checkSelfPermission(Permission.SETTINGS) == PackageManager.PERMISSION_DENIED) {
            Log.d(Global.SYSTEM, "SETTINGS PERMISSION DENIED");

        } else {
            Log.d(Global.SYSTEM, "SETTINGS PERMISSION GRANTED");
        }

        //settings
        robot.setDetectionModeOn(true, 2.0f);
        robot.setGoToSpeed(SpeedLevel.SLOW);
        robot.hideTopBar();
        robot.setPrivacyMode(true);
        robot.toggleNavigationBillboard(true);
        robot.setTopBadgeEnabled(true);
        robot.toggleWakeup(true);
        robot.requestToBeKioskApp();

        Log.v(Global.SYSTEM, "Settings:\n" +
                "\tDetectionMode\t=\t" + robot.isDetectionModeOn() +
                "\n\tSpeed\t\t\t=\t" + robot.getGoToSpeed() +
                "\n\tTop Badge\t\t=\t" + robot.isTopBadgeEnabled() +
                "\n\tNavBillboard\t=\t" + robot.isNavigationBillboardDisabled() +
                "\n\tWakeupDisabled\t=\t" + robot.isWakeupDisabled() +
                "\n\tKioskApp\t\t=\t" + robot.isSelectedKioskApp());
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
        langButtons = new ArrayList<>();
        langButtons.add((ImageButton) findViewById(R.id.btn_en));
        langButtons.add((ImageButton) findViewById(R.id.btn_cn));
        langButtons.add((ImageButton) findViewById(R.id.btn_el));
        langButtons.add((ImageButton) findViewById(R.id.btn_it));
        langButtons.add((ImageButton) findViewById(R.id.btn_vi));

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
        routine = new StateMachine(robot, this, StateMachine.GREETING);
        System.out.println("FLINTEMI: Create Initialisation Routine");
        updateThought(getResources().getString(R.string.cInit), Global.Emoji.eThinking);
        synchronized (routine) {
            new Thread(routine).start();
        }
        return routine;
    }

    public void langEN(View view) {
        Log.d(Global.SYSTEM, "Prompt language changed to EN");
        lang = LanguageID.EN;
        showPrompt();
    }

    public void langCN(View view) {
        Log.d(Global.SYSTEM, "Prompt language changed to CN");
        lang = LanguageID.CN;
        showPrompt();
    }

    public void langEL(View view) {
        Log.d(Global.SYSTEM, "Prompt language changed to EL");
        lang = LanguageID.EL;
        showPrompt();
    }

    public void langIT(View view) {
        Log.d(Global.SYSTEM, "Prompt language changed to IT");
        lang = LanguageID.IT;
        showPrompt();
    }

    public void langVI(View view) {
        Log.d(Global.SYSTEM, "Prompt language changed to VIN");
        lang = LanguageID.VI;
        showPrompt();
    }

    public void showPrompt() {
        switch (lang) {
            case LanguageID.EN:
                updateThought(getApplicationContext().getResources().getString(R.string.en_prompt), Global.Emoji.eGrinning);
                break;
            case LanguageID.CN:
                updateThought(getApplicationContext().getResources().getString(R.string.cn_prompt), Global.Emoji.eGrinning);
                break;
            case LanguageID.EL:
                updateThought(getApplicationContext().getResources().getString(R.string.el_prompt), Global.Emoji.eGrinning);
                break;
            case LanguageID.IT:
                updateThought(getApplicationContext().getResources().getString(R.string.it_prompt), Global.Emoji.eGrinning);
                break;
            case LanguageID.VI:
                updateThought(getApplicationContext().getResources().getString(R.string.vi_prompt), Global.Emoji.eGrinning);
                break;
        }
    }

    public void enableLanguageSwitching() {
        Log.d(Global.SYSTEM, "Language buttons enabled");
        for (int i = 0; i < langButtons.size(); i++) {
            langButtons.get(i).setEnabled(true);
        }
    }

    public void disableLanguageSwitching() {
        Log.d(Global.SYSTEM, "Language buttons disabled");
        for (int i = 0; i < langButtons.size(); i++) {
            langButtons.get(i).setEnabled(false);
        }
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
                Log.d(Global.UI, "Thought\t=\t\"" + string + "\"");
                thoughtTextView.setText(string);
                faceTextView.setText(emoji);

            }
        });
    }
}
