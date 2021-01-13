package flinderstemi.util.listeners;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;
import com.robotemi.sdk.sample.MainActivity;
import com.robotemi.sdk.sample.R;

import org.jetbrains.annotations.Nullable;

import flinderstemi.StateMachine;
import flinderstemi.GlobalVariables;
import flinderstemi.util.TemiListener;

//TODO Define a new textview that this listener should display to that does not require a SetTextViewCallback.

/**
 * This listener should be activated when the state machine declares a low SOC.
 * It checks the battery SOC as an int 0<SOC<=100 and prints it to a TextView.
 */
public class BatteryStateListener extends TemiListener implements OnBatteryStatusChangedListener {

    /*******************************************************************************************
     *                                       Definitions                                       *
     ******************************************************************************************/

    public static final int LOW = 0;
    public static final int BUFFER = 1;
    public static final int HIGH = 2;
    public static final int FULL = 3;

    /*******************************************************************************************
     *                                Constructed Arg Members                                  *
     ******************************************************************************************/

    Robot robot;
    MainActivity main;
    StateMachine stateMachine;
    Button startButton;

    ChargingLowOnClickListener lowListener;
    ChargingHighOnClickListener highListener;
    ChargingFullOnClickListener fullListener;

    /*******************************************************************************************
     *                                         State                                           *
     ******************************************************************************************/

    private boolean autoStart;
    private int SOC;

    /*******************************************************************************************
     *                                        Get/Set                                          *
     ******************************************************************************************/

    public boolean isAutoStart() {
        return autoStart;
    }

    /*******************************************************************************************
     *                                    Constructor(s)                                       *
     ******************************************************************************************/

    /**
     * @param robot
     * @param main
     * @param stateMachine
     * @param startButton
     */
    public BatteryStateListener(Robot robot, MainActivity main, StateMachine stateMachine, Button startButton) {
        Log.d("SEQUENCE", "Constructing BatteryStateListener");
        this.robot = robot;
        this.main = main;
        this.stateMachine = stateMachine;
        this.startButton = startButton;

        autoStart = true;
        BatteryData bd = robot.getBatteryData();
        boolean charging = bd.isCharging();
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

    /*******************************************************************************************
     *                                     Functionality                                       *
     ******************************************************************************************/

    /**
     * Returns an integer describing the state of charge when given the @soc percentage.
     * <p>
     * Returns an int defined by BatteryStateListener.[ FULL | HIGH | BUFFER | LOW ] based on if the @soc is at or above the values defined in res.
     * <p>
     *
     * @param soc the battery state of charge percentage to evaluate
     * @return an integer describing the state of charge as defined in <code>BatteryStateListener</code>
     */
    public static int batteryState(int soc) {
        if (soc >= GlobalVariables.SOC_HIGH) {
            Log.d("BATTERY", "batteryState(" + soc + ") = 3 [FULL]");
            return BatteryStateListener.FULL;
        } else if (soc >= GlobalVariables.SOC_LOW + GlobalVariables.SOC_BUFFER) {
            Log.d("BATTERY", "batteryState(" + soc + ") = 2 [HIGH]");
            return BatteryStateListener.HIGH;
        } else if (soc >= GlobalVariables.SOC_LOW) {
            Log.d("BATTERY", "batteryState(" + soc + ") = 1 [BUFFER]");
            return BatteryStateListener.BUFFER;
        } else {
            Log.d("BATTERY", "batteryState(" + soc + ") = 0 [LOW]");
            return BatteryStateListener.LOW;
        }
    }

    /**
     * Toggles the member <code>autoStart</code> variable and formats the member <code>startButton</code> for user feedback.
     */
    public void toggleAutoStart() {
        if (autoStart) {
            autoStart = false;
            Log.d(GlobalVariables.STATE, "autoStart = false");

            //format button to be in the false state
            String s = GlobalVariables.resources.getString(R.string.t_chargingASOff);
            startButton.setText(GlobalVariables.resources.getText(R.string.b_turnOnAutoStart));
            main.updateThought(String.format(s) + SOC);
//
            Log.d(GlobalVariables.UI, "startButton.setText( \"" + s + "\" )");
            Log.i("BATTERY", "Patrol auto-start disabled.");
        } else {
            autoStart = true;
            Log.d(GlobalVariables.STATE, "autoStart = true");

            //format button to be in the true state
            String s = GlobalVariables.resources.getString(R.string.t_chargingASOn);
            startButton.setText(GlobalVariables.resources.getText(R.string.b_turnOffAutoStart));
            main.updateThought(String.format(s) + SOC);

            Log.d(GlobalVariables.UI, "startButton.setText( \"" + s + "\" )");
            Log.i("BATTERY", "Patrol auto-start enabled.");
        }
    }

    private void formatLowSOCStartButton() {
        Log.i(GlobalVariables.BATTERY, "Battery SOC = LOW");
        Log.d(GlobalVariables.BATTERY, "formatLowSOCStartButton");
        //initially, when the robot is returning, the button is disabled
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);

        //TODO format strings with vars properly
        Log.i(GlobalVariables.SEQUENCE, "autoStart = " + isAutoStart());
        if (isAutoStart()) {
            //format button to be in the true state
            String s = GlobalVariables.resources.getString(R.string.t_chargingASOn);
            startButton.setText(GlobalVariables.resources.getText(R.string.b_turnOffAutoStart));
            main.updateThought(String.format(s) + SOC);

            Log.d(GlobalVariables.UI, "startButton.setText( \"" + s + "\" )");
            Log.i("BATTERY", "Patrol auto-start enabled.");
        } else {
            //format button to be in the false state
            String s = GlobalVariables.resources.getString(R.string.t_chargingASOff);
            startButton.setText(GlobalVariables.resources.getText(R.string.b_turnOnAutoStart));
            main.updateThought(String.format(s) + SOC);
//
            Log.d(GlobalVariables.UI, "startButton.setText( \"" + s + "\" )");
            Log.i("BATTERY", "Patrol auto-start disabled.");
        }

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

    /**
     *
     */
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

    /**
     * @param batteryData
     */
    @Override
    public void onBatteryStatusChanged(@Nullable BatteryData batteryData) {
        //get the current battery
        SOC = batteryData.getBatteryPercentage();
        int bs = batteryState((SOC));

        Log.v("LISTENER", "onBatteryStatusChanged event:\n\tsoc=\t=\t" + SOC +
                "\n\tisCharging\t=\t" + batteryData.isCharging());

        //TODO warn devs about interrupting the sm using the battery soc changed announcement
        //check thresholds
        if ((bs == LOW) || (bs == BUFFER)) {
            formatLowSOCStartButton();
        } else if (bs == HIGH) {//this construct is necessary to provide the hysteresis effect
            formatHighSOCStartButton();
        } else if (bs == FULL) {
            formatFullSOCStartButton();
        }
    }
}