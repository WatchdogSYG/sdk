package flinderstemi.util.listeners;

import android.util.Log;
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

    public BatteryStateListener(Robot robot, TextView tv, SetTextViewCallback stvc, StateMachine stateMachine) {
        this.robot = robot;
        this.tv = tv;
        this.stvc = stvc;
        this.stateMachine = stateMachine;
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
        if ((SOC >= GlobalVariables.SOC_LOW + GlobalVariables.SOC_BUFFER) && (stateMachine != null)) {
            //we have reached above low battery, we can change the UI buttons to force start

        } else if ((SOC >= GlobalVariables.SOC_HIGH) && (stateMachine != null)) {
            //the state machine has already been initialised and therefore is waiting for a notification. We can possibly auto start now.
            stateMachine.notify();
        }
    }
}
