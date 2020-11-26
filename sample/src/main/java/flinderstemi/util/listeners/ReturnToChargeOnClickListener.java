package flinderstemi.util.listeners;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.sample.MainActivity;

import flinderstemi.util.GlobalVariables;
import flinderstemi.util.StateMachine;

public class ReturnToChargeOnClickListener implements View.OnClickListener {

    Button startButton;
    Robot robot;
    MainActivity main;
    TextView tv;
    StateMachine stateMachine;

    public ReturnToChargeOnClickListener(Button startButton, Robot robot, MainActivity main, TextView tv, StateMachine stateMachine) {
        this.startButton = startButton;
        this.robot = robot;
        this.main = main;
        this.tv = tv;
        this.stateMachine = stateMachine;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        //goto the home base
        Log.d("LOCATION", robot.getLocations().get(0));
        robot.goTo(GlobalVariables.L_HOME_BASE);
        //start a LocationListener so we know when we reach the home base
        Log.d("LOCATION", "Going to Charging Station");
        robot.addOnGoToLocationStatusChangedListener(new ReturnToChargeLocationListener(robot, tv, main, stateMachine, startButton));
        //Robot robot, TextView tv, MainActivity main, StateMachine stateMachine, Button startButton
    }
}
