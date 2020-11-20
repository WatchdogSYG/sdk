package flinderstemi.util.listeners;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;

import org.jetbrains.annotations.Nullable;

import flinderstemi.util.GlobalVariables;
import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.StateMachine;

//TODO Define a new textview that this listener should display to that does not require a SetTextViewCallback.

/**
 * This listener should be activated when the state machine declares a low SOC.
 * It checks the battery SOC as an int 0<SOC<=100 and prints it to a TextView.
 */
public class BatteryStateListener implements OnBatteryStatusChangedListener {
    Robot robot;
    TextView tv;
    SetTextViewCallback stvc;
    public int SOC;
    StateMachine stateMachine;
    Button startButton;
    ChargingLowOnClickListener lowListener;

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
            stvc.updateThought("Charging " + Integer.toString(SOC) + "%. Auto-start patrol when full battery DISABLED");
        } else {
            autoStart = true;
            //format button to be in the true state
            startButton.setText("Turn OFF Patrol Auto-Start");
            stvc.updateThought("Charging " + Integer.toString(SOC) + "%. Auto-start patrol when full battery ENABLED.");
        }
    }

    public BatteryStateListener(Robot robot, TextView tv, SetTextViewCallback stvc, StateMachine stateMachine, Button startButton) {
        this.robot = robot;
        this.tv = tv;
        this.stvc = stvc;
        this.stateMachine = stateMachine;
        this.startButton = startButton;
        autoStart = false;
        BatteryData bd = robot.getBatteryData();
        charging = bd.isCharging();
        SOC = bd.getBatteryPercentage();
        formatLowSOCStartButton(startButton);
    }

    public void formatLowSOCStartButton(Button startButton) {
        startButton.setEnabled(true);
        startButton.setText("Turn Off Patrol Auto-Start");
        stvc.updateThought("Charging " + SOC + "%. Auto-start patroll when full battery " + Boolean.toString(autoStart));
        lowListener = new ChargingLowOnClickListener(this);
        startButton.setOnClickListener(lowListener);
    }

    @Override
    public void onBatteryStatusChanged(@Nullable BatteryData batteryData) {
        //get the current battery
        SOC = batteryData.getBatteryPercentage();

        //give some user feedback
        Log.i("BATTERY", "SOC=" + SOC + "%");
        stvc.updateThought("SOC=" + SOC + "%");
        robot.speak(TtsRequest.create("Battery SOC changed to " + SOC + " percent.", false));

        //check thresholds
        //we are above the low threshold plus a small buffer
        if (stateMachine != null) {
            //state machine exists, notify it
            if (SOC < GlobalVariables.SOC_LOW) {
                if (lowListener == null) {
                    //we have moved from high to low battery, add a lowListener
                    formatLowSOCStartButton(startButton);
                } else {
                    //we have come from a low soc to another low soc, update soc values
                    stvc.updateThought("Charging " + SOC + "%. Auto-start patrol when full battery " + Boolean.toString(autoStart));
                }
            } else if (SOC >= GlobalVariables.SOC_LOW + GlobalVariables.SOC_BUFFER) {
                //The state machine is waiting for a notify from a button press, set the onclicklistener

            } else if (SOC >= GlobalVariables.SOC_HIGH) {
                //the state machine has already been initialised and therefore is waiting for a notification. We can possibly auto start now.
                //TODO set the state machine to patrolling state
                stateMachine.notify();
            }
        } else {
            //state machine doesnt exist, this is the start of the program, make one
            if (SOC >= GlobalVariables.SOC_LOW + GlobalVariables.SOC_BUFFER) {
                //The state machine is waiting for a notfy from a button press, set the onclicklistener

            } else if (SOC >= GlobalVariables.SOC_HIGH) {
                //the state machine has already been initialised and therefore is waiting for a notification. We can possibly auto start now.
                //TODO set the state machine to patrolling state
                stateMachine.notify();
            }
        }
    }
}
