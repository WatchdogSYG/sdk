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

import flinderstemi.Global;
import flinderstemi.StateMachine;

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

    /*******************************************************************************************
     *                                Constructed Arg Members                                  *
     ******************************************************************************************/

    Robot robot;
    MainActivity main;
    final StateMachine stateMachine;
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
     * @param robot        The singleton robot instance.
     * @param main         The MainActivity callback.
     * @param stateMachine The StateMachine that instantiated this class. (Callback)
     * @param startButton  The main interactive button
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
        Log.v(Global.STATE, "BatteryStateListener:\n" +
                "autoStart\t=\ttrue\n" +
                "charging\t=\t" + charging + "\n" +
                "soc\t\t=\t" + SOC);

        //move logs to constructors
        lowListener = new ChargingLowOnClickListener(this);
        Log.d(Global.LISTENER, "Instantiated new ChargingLowOnClickListener");
        Log.v(Global.LISTENER, "Instantiated new ChargingLowOnClickListener(BatteryStateListener bsl) implements OnClickListener" + lowListener.toString());
        highListener = new ChargingHighOnClickListener(this);
        Log.d(Global.LISTENER, "Instantiated new ChargingHighOnClickListener");
        Log.v(Global.LISTENER, "Instantiated new ChargingHighOnClickListener(BatteryStateListener bsl) implements OnClickListener: " + highListener.toString());
        fullListener = new ChargingFullOnClickListener(this);
        Log.d(Global.LISTENER, "Instantiated new ChargingFullOnClickListener");
        Log.v(Global.LISTENER, "Instantiated new ChargingFullOnClickListener(BatteryStateListener bsl) implements OnClickListener" + fullListener.toString());

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
        if (soc >= Global.SOC_HIGH) {
            Log.d("BATTERY", "batteryState(" + soc + ") = 3 [FULL]");
            return BatteryStateListener.FULL;
        } else if (soc >= Global.SOC_LOW + Global.SOC_BUFFER) {
            Log.d("BATTERY", "batteryState(" + soc + ") = 2 [HIGH]");
            return BatteryStateListener.HIGH;
        } else if (soc >= Global.SOC_LOW) {
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
            Log.d(Global.STATE, "autoStart = false");

            //format button to be in the false state
            startButton.setText(Global.resources.getText(R.string.b_turnOnAutoStart));
            main.updateThought(Global.resources.getString(R.string.t_chargingASOff), Global.Emoji.eSleeping);
//
            Log.d(Global.UI, "startButton.setText( \"" + Global.resources.getString(R.string.t_chargingASOff) + "\" )");
            Log.i(Global.BATTERY, "Patrol auto-start disabled.");
        } else {
            autoStart = true;
            Log.d(Global.STATE, "autoStart = true");

            //format button to be in the true state
            startButton.setText(Global.resources.getText(R.string.b_turnOffAutoStart));
            main.updateThought(Global.resources.getString(R.string.t_chargingASOn), Global.Emoji.eSleeping);

            Log.d(Global.UI, "startButton.setText( \"" + Global.resources.getString(R.string.t_chargingASOn) + "\" )");
            Log.i(Global.BATTERY, "Patrol auto-start enabled.");
        }
    }

    private void formatLowSOCStartButton() {
        Log.i(Global.BATTERY, "Battery SOC = LOW");
        Log.d(Global.BATTERY, "formatLowSOCStartButton");
        //initially, when the robot is returning, the button is disabled
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);

        //TODO format strings with vars properly
        Log.i(Global.SEQUENCE, "autoStart = " + isAutoStart());
        if (isAutoStart()) {
            //format button to be in the true state
            startButton.setText(Global.resources.getText(R.string.b_turnOffAutoStart));
            main.updateThought(Global.resources.getString(R.string.t_chargingASOn), Global.Emoji.eSleeping);

            Log.d(Global.UI, "startButton.setText( \"" + Global.resources.getString(R.string.t_chargingASOn) + "\" )");
            Log.i(Global.BATTERY, "Patrol auto-start enabled.");
        } else {
            //format button to be in the false state
            startButton.setText(Global.resources.getText(R.string.b_turnOnAutoStart));
            main.updateThought(Global.resources.getString(R.string.t_chargingASOff), Global.Emoji.eSleeping);
//
            Log.d(Global.UI, "startButton.setText( \"" + Global.resources.getString(R.string.t_chargingASOff) + "\" )");
            Log.i(Global.BATTERY, "Patrol auto-start disabled.");
        }

        Log.i(Global.LISTENER, "Start Button OnClickListener = lowListener");
        startButton.setOnClickListener(lowListener);
    }

    private void formatHighSOCStartButton() {
        Log.i(Global.BATTERY, "Battery SOC = HIGH");
        Log.d(Global.BATTERY, "formatHighSOCStartButton");

        startButton.setText(Global.resources.getString(R.string.b_forceStartCharging));
        main.updateThought(Global.resources.getString(R.string.t_forceStart) + autoStart, Global.Emoji.eSleeping);
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);

        Log.i(Global.LISTENER, "Start Button OnClickListener = highListener");
        startButton.setOnClickListener(highListener);
    }

    private void formatFullSOCStartButton() {
        Log.i(Global.BATTERY, "Battery SOC = FULL");
        Log.d(Global.BATTERY, "formatFULLSOCStartButton");

        //if the statemachine doesnt exist at startup, the ChargingFullOnClickListener will never
        // have a @NonNull member variable for stateMachine, and therefore will never be able to
        // call fullWakeStateMachine from its OnClick method.
        //we need to set one

        //if autostart, start
        //otherwise provide a button
        if (autoStart) {
            Log.i(Global.SEQUENCE, "Auto-starting");
            Log.d(Global.SEQUENCE, "autostart = true. fullWakeStateMachine()");
            fullWakeStateMachine();
            startButton.setVisibility(View.GONE);
        } else {
            startButton.setVisibility(View.VISIBLE);
            startButton.setText(Global.resources.getString(R.string.start));
            main.updateThought(Global.resources.getString(R.string.t_ready), Global.Emoji.eRobot);
            Log.i(Global.LISTENER, "Start Button OnClickListener = highListener");
            startButton.setOnClickListener(fullListener);
        }
    }

    /**
     *
     */
    public void fullWakeStateMachine() {
        startButton.setVisibility(View.INVISIBLE);
        startButton.setEnabled(false);
        if (stateMachine != null) {
            //sm does exist, notify it
            Log.d(Global.SEQUENCE, "StateMachine exists. notify()");
            synchronized (stateMachine) {
                stateMachine.setWakeCondition(new String[]{"BATTERYWAKE"});
                Log.v(Global.SEQUENCE, stateMachine.toString() + " notify()");
                stateMachine.notify();
            }
        } else {
            //sm doesnt exist, make a new one from fresh
            Log.d("SEQUENCE", "StateMachine is null. main.startRoutineFresh()");
            main.startRoutineFresh();
        }
    }

    /**
     * Sets the BatteryStateListener and relevant Views based on the battery level.
     * @param batteryData The current battery state
     */
    @Override
    public void onBatteryStatusChanged(@Nullable BatteryData batteryData) {
        //get the current battery
        SOC = batteryData.getBatteryPercentage();
        int bs = batteryState((SOC));

        Log.v(Global.LISTENER, "onBatteryStatusChanged event:\n\tsoc=\t=\t" + SOC +
                "\n\tisCharging\t=\t" + batteryData.isCharging());

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