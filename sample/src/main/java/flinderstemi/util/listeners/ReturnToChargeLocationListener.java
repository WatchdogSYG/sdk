package flinderstemi.util.listeners;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.NotNull;

import flinderstemi.util.StateMachine;

public class ReturnToChargeLocationListener implements OnGoToLocationStatusChangedListener {

    Robot robot;
    TextView tv;
    MainActivity main;
    StateMachine stateMachine;
    Button startButton;

    public ReturnToChargeLocationListener(Robot robot, TextView tv, MainActivity main, StateMachine stateMachine, Button startButton) {
        this.robot = robot;
        this.tv = tv;
        this.main = main;
        this.stateMachine = stateMachine;
        this.startButton = startButton;
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {
        //reminder that we are now at low battery
        if (status == "COMPLETED") {
            //we are now charging
            robot.removeOnGoToLocationStatusChangedListener(this);
            //give feedback to the user
            main.updateThought("Charging... Auto-start when full battery: ON");
            //SOC Listener, do this before the next step (button enable)
            robot.addOnBatteryStatusChangedListener(new BatteryStateListener(robot, tv, main, stateMachine, startButton));
            //enable button and set to next function: Cancel autostart
            Log.d("LOCATION", "Reached Charging Station");
        }
    }
}
