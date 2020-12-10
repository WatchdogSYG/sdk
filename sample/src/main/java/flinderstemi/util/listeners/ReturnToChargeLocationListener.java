package flinderstemi.util.listeners;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import flinderstemi.util.StateMachine;

/**
 * This Listener should be applied to a button that has been pressed to send the robot back to the base for charging.
 * It listens for the point at which either of the following are met:
 * <p>
 * a. The robot finishes its GoTo("home base") command by itself and fires the OnGoToStatusChanged status=COMPLETED event
 * b. The robot senses that it is charging and fires the OnBatteryStatusChanged isCharging=true event. This catches the case where the user manually moves the machine back to the home base.
 * <p>
 * Note that this is vulnerable to external charging sources triggering the isCharging=true event before it reaches the HB. This is extremely unlikely and easy to reset.
 * Note that this is currently vulnerable to the user closing the app via the top bar and sending Temi back to the HB with the Locations app. //TODO Catch this by disabling the top bar properly (see Temi SDK FAQS) and allowing the user to safely exit the app and send Temi back using the Locations app
 */
public class ReturnToChargeLocationListener implements OnGoToLocationStatusChangedListener {

    Robot robot;
    MainActivity main;
    StateMachine stateMachine;
    Button startButton;
    MediaPlayer mp;

    ReturnToChargeListener rtcl;

    /**
     * @param robot
     * @param main
     * @param stateMachine
     * @param startButton
     * @param mp
     */
    public ReturnToChargeLocationListener(Robot robot, MainActivity main, StateMachine stateMachine, Button startButton, MediaPlayer mp) {
        this.robot = robot;
        this.main = main;
        this.stateMachine = stateMachine;
        this.startButton = startButton;
        this.mp = mp;

        rtcl = new ReturnToChargeListener(robot);
        robot.addOnBatteryStatusChangedListener(rtcl);
    }

    /**
     * @param location
     * @param status
     * @param descriptionId
     * @param description
     */
    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {
        //reminder that we are now at low battery
        if (status == "COMPLETED") {
            //we are now charging
            reachedCharging();
        }
    }

    /**
     *
     */
    private void reachedCharging() {
        //give feedback to the user
        main.updateThought("Charging... Auto-start when full battery: ON");
        //SOC Listener, do this before the next step (button enable)
        stateMachine.setBSL(new BatteryStateListener(robot, main, stateMachine, startButton));
        //enable button and set to next function: Cancel autostart
        Log.d("LOCATION", "Reached Charging Station");

        robot.removeOnBatteryStatusChangedListener(rtcl);
        robot.removeOnGoToLocationStatusChangedListener(this);
    }

    //detects if charging occurs when going back to the home base, in case of a onGoToLocationStatusChanged event not firing

    /**
     *
     */
    public class ReturnToChargeListener implements OnBatteryStatusChangedListener {
        Robot robot;

        ReturnToChargeListener(Robot robot) {
            this.robot = robot;
        }

        /**
         *
         * @param batteryData
         */
        @Override
        public void onBatteryStatusChanged(@Nullable BatteryData batteryData) {
            BatteryData bd = robot.getBatteryData();
            if (bd.isCharging()) {
                reachedCharging();
            }
        }
    }
}
