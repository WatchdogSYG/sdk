package flinderstemi.util.listeners;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.Nullable;

import flinderstemi.util.GlobalVariables;
import flinderstemi.util.StateMachine;

//TODO Define a new textview that this listener should display to that does not require a SetTextViewCallback.

/**
 * This listener should be activated when the state machine declares a low SOC.
 * It checks the battery SOC as an int 0<SOC<=100 and prints it to a TextView.
 */
public class BatteryStateListener implements OnBatteryStatusChangedListener {
    public final int LOW = 0;
    public final int BUFFER = 1;
    public final int HIGH = 2;
    public final int FULL = 3;


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

    public boolean isAutoStart() {
        return autoStart;
    }

    public void toggleAutoStart() {
        if (autoStart) {
            autoStart = false;
            //format button to be in the false state
            startButton.setText("Turn ON Patrol Auto-Start");
            Log.i("BATTERY", "Patrol auto-start disabled.");
            main.updateThought("Charging " + Integer.toString(SOC) + "%. Auto-start patrol when full battery DISABLED");
        } else {
            autoStart = true;
            //format button to be in the true state
            startButton.setText("Turn OFF Patrol Auto-Start");
            Log.i("BATTERY", "Patrol auto-start enabled.");
            main.updateThought("Charging " + Integer.toString(SOC) + "%. Auto-start patrol when full battery ENABLED.");
        }
    }

    private void setPreviousBatteryPercentage(int soc) {
        prevSOC = soc;
    }

    private int getPreviousBatteryPercentage() {
        return prevSOC;
    }

    private int batteryState(int soc) {
        if (soc < GlobalVariables.SOC_LOW) {
            Log.d("BATTERY", "batteryState(soc)=0");
            return 0;
        } else if (soc < GlobalVariables.SOC_LOW + GlobalVariables.SOC_BUFFER) {
            Log.d("BATTERY", "batteryState(soc)=1");
            return 1;
        } else if (soc > GlobalVariables.SOC_HIGH) {
            Log.d("BATTERY", "batteryState(soc)=2");
            return 2;
        } else {
            Log.d("BATTERY", "batteryState(soc)=3");
            return 3;
        }
    }

    public BatteryStateListener(Robot robot, MainActivity main, StateMachine stateMachine, Button startButton) {
        this.robot = robot;
        this.main = main;
        this.stateMachine = stateMachine;
        this.startButton = startButton;
        autoStart = true;
        BatteryData bd = robot.getBatteryData();
        charging = bd.isCharging();
        SOC = bd.getBatteryPercentage();

        lowListener = new ChargingLowOnClickListener(this);
        highListener = new ChargingHighOnClickListener(main, stateMachine);
        fullListener = new ChargingFullOnClickListener(main, stateMachine);

        formatLowSOCStartButton();
    }

    //stores the soc in a variable so we can use it to compare history. (we can see if we are gong up or down)


    public void formatLowSOCStartButton() {
        Log.d("BATTERY", "formatLowSOCStartButton");
        //initially, when the robot is returning, the button is disabled
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);

        if (isAutoStart()) {
            startButton.setText("Turn OFF Patrol Auto-Start");
        } else {
            startButton.setText("Turn ON Patrol Auto-Start");
        }

        main.updateThought("Charging " + SOC + "%. Auto-start patrol when full battery " + Boolean.toString(autoStart));

        startButton.setOnClickListener(lowListener);
    }

    private void formatHighSOCStartButton() {
        Log.d("BATTERY", "formatHighSOCStartButton");
        startButton.setText("Force Start Patrol Now");
        main.updateThought("Charging " + SOC + "%. Auto-start patrol when full battery. Tap the button to start the patrol now." + Boolean.toString(autoStart));
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);


        startButton.setOnClickListener(highListener);
    }

    private void formatFullSOCStartButton() {
        Log.d("BATTERY", "formatFullSOCStartButton");
        //if the statemachine doesnt exist at startup, the ChargingFullOnClickListener will never have a @NonNull member variable for stateMachine, and therefore will never be able to call fullWakeStateMachine from its OnClick method.
        //we need to set one

        //if autostart, start
        //otherwise provide a button
        if (autoStart) {
            fullListener.fullWakeStateMachine(main, stateMachine);
            startButton.setVisibility(View.GONE);
        } else {
            startButton.setVisibility(View.VISIBLE);
            startButton.setText("Start Patrol");
            main.updateThought("Ready to Start Patrol");
            startButton.setOnClickListener(fullListener);
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
