package flinderstemi.util.listeners;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.Nullable;

import flinderstemi.StateMachine;
import flinderstemi.util.GlobalVariables;

//TODO Define a new textview that this listener should display to that does not require a SetTextViewCallback.

/**
 * This listener should be activated when the state machine declares a low SOC.
 * It checks the battery SOC as an int 0<SOC<=100 and prints it to a TextView.
 */
public class BatteryStateListener implements OnBatteryStatusChangedListener {

    /*******************************************************************************************
     *                                       Definitions                                       *
     ******************************************************************************************/
    public static final int LOW = 0;
    public static final int BUFFER = 1;
    public static final int HIGH = 2;
    public static final int FULL = 3;


    Robot robot;
    MainActivity main;
    public int SOC;
    StateMachine stateMachine;
    Button startButton;
    ChargingLowOnClickListener lowListener;
    ChargingHighOnClickListener highListener;
    ChargingFullOnClickListener fullListener;
    private int prevSOC;

    private boolean autoStart;
    private boolean charging;

    /*******************************************************************************************
     *                                        Get/Set                                          *
     ******************************************************************************************/
    public boolean isAutoStart() {
        return autoStart;
    }

    private void setPreviousBatteryPercentage(int soc) {
        prevSOC = soc;
    }

    /*******************************************************************************************
     *                                     Functionality                                       *
     ******************************************************************************************/

    public static int batteryState(int soc) {
        if (soc <= GlobalVariables.SOC_LOW) {
            Log.d("BATTERY", "batteryState(" + soc + ")=0 [LOW]");
            return 0;
        } else if (soc <= GlobalVariables.SOC_LOW + GlobalVariables.SOC_BUFFER) {
            Log.d("BATTERY", "batteryState(" + soc + ")=1 [BUFFER]");
            return 1;
        } else if (soc > GlobalVariables.SOC_HIGH) {
            Log.d("BATTERY", "batteryState(" + soc + ")=2 [HIGH]");
            return 2;
        } else {
            Log.d("BATTERY", "batteryState(" + soc + ")=3 [FULL]");
            return 3;
        }
    }

    public void toggleAutoStart() {
        if (autoStart) {
            autoStart = false;
            Log.d(GlobalVariables.STATE, "Autostart FALSE");
            //format button to be in the false state
            startButton.setText("Turn ON Patrol Auto-Start");
            Log.d(GlobalVariables.UI, "startButton.setText( \"" + "Turn ON Patrol Auto-Start" + "\" )");
            Log.i("BATTERY", "Patrol auto-start disabled.");
            main.updateThought("Charging " + Integer.toString(SOC) + "%. Auto-start patrol when full battery DISABLED");
        } else {
            autoStart = true;
            Log.d(GlobalVariables.STATE, "Autostart FALSE");
            //format button to be in the true state
            startButton.setText("Turn OFF Patrol Auto-Start");
            Log.d(GlobalVariables.UI, "startButton.setText( \"" + "Turn OFF Patrol Auto-Start" + "\" )");
            Log.i("BATTERY", "Patrol auto-start enabled.");
            main.updateThought("Charging " + Integer.toString(SOC) + "%. Auto-start patrol when full battery ENABLED.");
        }
    }

    /*******************************************************************************************
     *                                    Constructor(s)                                       *
     ******************************************************************************************/

    public BatteryStateListener(Robot robot, MainActivity main, StateMachine stateMachine, Button startButton) {
        Log.d("SEQUENCE", "Constructing BatteryStateListener");
        this.robot = robot;
        this.main = main;
        this.stateMachine = stateMachine;
        this.startButton = startButton;


        autoStart = true;
        BatteryData bd = robot.getBatteryData();
        charging = bd.isCharging();
        SOC = bd.getBatteryPercentage();
        Log.v(GlobalVariables.STATE, "BatteryStateListener:\n" +
                "autoStart\t=\ttrue\n" +
                "charging\t=\t" + charging + "\n" +
                "soc\t\t=\t" + SOC);


        //move logs to constructors
        lowListener = new ChargingLowOnClickListener(this);
        Log.d(GlobalVariables.LISTENER, "Instantiated new ChargingLowOnClickListener");
        Log.v(GlobalVariables.LISTENER, "Instantiated new ChargingLowOnClickListener(BatteryStateListener bsl) implements OnClickListener" + lowListener.toString());
        highListener = new ChargingHighOnClickListener(this);
        Log.d(GlobalVariables.LISTENER, "Instantiated new ChargingHighOnClickListener");
        Log.v(GlobalVariables.LISTENER, "Instantiated new ChargingHighOnClickListener(BatteryStateListener bsl) implements OnClickListener: " + highListener.toString());
        fullListener = new ChargingFullOnClickListener(this);
        Log.d(GlobalVariables.LISTENER, "Instantiated new ChargingFullOnClickListener");
        Log.v(GlobalVariables.LISTENER, "Instantiated new ChargingFullOnClickListener(BatteryStateListener bsl) implements OnClickListener" + fullListener.toString());

        formatLowSOCStartButton();
    }

    //stores the soc in a variable so we can use it to compare history. (we can see if we are gong up or down)


    public void formatLowSOCStartButton() {
        Log.i(GlobalVariables.BATTERY, "Battery SOC = LOW");
        Log.d(GlobalVariables.BATTERY, "formatLowSOCStartButton");
        //initially, when the robot is returning, the button is disabled
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);

        Log.i(GlobalVariables.SEQUENCE, "autostart = " + isAutoStart());
        if (isAutoStart()) {
            startButton.setText("Turn OFF Patrol Auto-Start");
        } else {
            startButton.setText("Turn ON Patrol Auto-Start");
        }

        main.updateThought("Charging " + SOC + "%. Auto-start patrol when full battery " + Boolean.toString(autoStart));

        Log.i(GlobalVariables.LISTENER, "Start Button OnClickListener = lowListener");
        startButton.setOnClickListener(lowListener);
    }

    private void formatHighSOCStartButton() {
        Log.i(GlobalVariables.BATTERY, "Battery SOC = HIGH");
        Log.d(GlobalVariables.BATTERY, "formatHighSOCStartButton");

        startButton.setText("Force Start Patrol Now");
        main.updateThought("Charging " + SOC + "%. Auto-start patrol when full battery. Tap the button to start the patrol now." + Boolean.toString(autoStart));
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);

        Log.i(GlobalVariables.LISTENER, "Start Button OnClickListener = highListener");
        startButton.setOnClickListener(highListener);
    }

    private void formatFullSOCStartButton() {
        Log.i(GlobalVariables.BATTERY, "Battery SOC = FULL");
        Log.d(GlobalVariables.BATTERY, "formatFULLSOCStartButton");

        //if the statemachine doesnt exist at startup, the ChargingFullOnClickListener will never have a @NonNull member variable for stateMachine, and therefore will never be able to call fullWakeStateMachine from its OnClick method.
        //we need to set one

        //if autostart, start
        //otherwise provide a button
        if (autoStart) {
            Log.i("SEQUENCE", "Auto-starting");
            Log.d("SEQUENCE", "autostart = true. fullWakeStateMachine()");
            fullWakeStateMachine();
            startButton.setVisibility(View.GONE);
        } else {
            startButton.setVisibility(View.VISIBLE);
            startButton.setText("Start Patrol");
            main.updateThought("Ready to Start Patrol");
            Log.i("LISTENER", "Start Button OnClickListener = highListener");
            startButton.setOnClickListener(fullListener);
        }
    }

    public void fullWakeStateMachine() {
        if (stateMachine != null) {
            //sm does exist, notify it
            Log.d(GlobalVariables.SEQUENCE, "StateMachine exists. notify()");
            synchronized (stateMachine) {
                stateMachine.setWakeCondition(new String[]{"BATTERYWAKE"});
                Log.v(GlobalVariables.SEQUENCE, stateMachine.toString() + " notify()");
                stateMachine.notify();
            }
        } else {
            //sm doesnt exist, make a new one from fresh
            Log.d("SEQUENCE", "StateMachine is null. main.startRoutineFresh()");
            main.startRoutineFresh();
        }
    }

    @Override
    public void onBatteryStatusChanged(@Nullable BatteryData batteryData) {
        //get the current battery
        SOC = batteryData.getBatteryPercentage();
        int bs = batteryState((SOC));

        //give some user feedback
        Log.i("BATTERY", "SOC=" + SOC + "%");
        main.updateThought("SOC=" + SOC + "%");
        //TODO warn devs about interrupting the sm using the battery soc changed announcement

        //check thresholds
        //we are above the low threshold plus a small buffer

        //state machine exists, notify it
        if ((bs == LOW) || (bs == BUFFER)) {
            formatLowSOCStartButton();
        } else if (bs == HIGH) {//this construct is necessary to provide the hysteresis effect

            //The state machine is waiting for a notify from a button press, set the onclicklistener, we have moved from low to high battery
            formatHighSOCStartButton();
        } else if (bs == FULL) {
            //the state machine has already been initialised and therefore is waiting for a notification. We can possibly auto start now.
            formatFullSOCStartButton();
        }

        //store the previous soc in an int for comparison later
        setPreviousBatteryPercentage(SOC);
    }
}
