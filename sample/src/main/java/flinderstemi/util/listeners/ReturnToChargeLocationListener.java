package flinderstemi.util.listeners;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import flinderstemi.util.StateMachine;

public class ReturnToChargeLocationListener implements OnGoToLocationStatusChangedListener {

    Robot robot;
    TextView tv;
    MainActivity main;
    StateMachine stateMachine;
    Button startButton;
    MediaPlayer mp;

    ReturnToChargeListener rtcl;

    public ReturnToChargeLocationListener(Robot robot, TextView tv, MainActivity main, StateMachine stateMachine, Button startButton, MediaPlayer mp) {
        this.robot = robot;
        this.tv = tv;
        this.main = main;
        this.stateMachine = stateMachine;
        this.startButton = startButton;
        this.mp = mp;

        rtcl = new ReturnToChargeListener(robot);
        robot.addOnBatteryStatusChangedListener(rtcl);
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {
        //reminder that we are now at low battery
        if (status == "COMPLETED") {
            //we are now charging
            reachedCharging();
        }
    }

    private void reachedCharging() {
        //give feedback to the user
        main.updateThought("Charging... Auto-start when full battery: ON");
        //SOC Listener, do this before the next step (button enable)
        robot.addOnBatteryStatusChangedListener(new BatteryStateListener(robot, tv, main, stateMachine, startButton));
        //enable button and set to next function: Cancel autostart
        Log.d("LOCATION", "Reached Charging Station");

        robot.removeOnBatteryStatusChangedListener(rtcl);
        robot.removeOnGoToLocationStatusChangedListener(this);

    }

    //detects if charging occurs when going back to the home base, in case of a onGoToLocatoinStatusChanged event not firing
    public class ReturnToChargeListener implements OnBatteryStatusChangedListener {
        Robot robot;

        ReturnToChargeListener(Robot robot) {
            this.robot = robot;

        }

        @Override
        public void onBatteryStatusChanged(@Nullable BatteryData batteryData) {
            BatteryData bd = robot.getBatteryData();
            if (bd.isCharging()) {
                reachedCharging();
            }
        }
    }
}
