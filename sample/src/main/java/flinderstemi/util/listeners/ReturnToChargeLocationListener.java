package flinderstemi.util.listeners;

import android.util.Log;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;

import org.jetbrains.annotations.NotNull;

import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.StateMachine;

public class ReturnToChargeLocationListener implements OnGoToLocationStatusChangedListener {

    Robot robot;
    TextView tv;
    SetTextViewCallback stvc;
    StateMachine stateMachine;

    public ReturnToChargeLocationListener(Robot robot, TextView tv, SetTextViewCallback stvc, StateMachine stateMachine) {
        this.robot = robot;
        this.tv = tv;
        this.stvc = stvc;
        this.stateMachine = stateMachine;
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {
        if (status == "COMPLETED") {
            //we are now charging
            robot.removeOnGoToLocationStatusChangedListener(this);
            //give feedback to the user
            stvc.updateThought("Charging... Auto-start when full battery: ON");
            //set SOC Listener, do this before the next step (button enable)
            robot.addOnBatteryStatusChangedListener(new BatteryStateListener(robot, tv, stvc, stateMachine));
            //enable button and set to next function: Cancel autostart
            Log.d("LOCATION", "Reached Charging Station");
        }
    }
}
