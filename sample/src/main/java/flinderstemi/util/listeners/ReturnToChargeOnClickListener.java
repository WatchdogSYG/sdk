package flinderstemi.util.listeners;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.sample.MainActivity;

import flinderstemi.util.GlobalVariables;
import flinderstemi.util.StateMachine;

/**
 * This listener should be used after the user is offered the choice to manually send the robot back to the home base.
 * This listener will fire OnClick and will send the robot back to the charging station, format views accordingly. It then will
 */
public class ReturnToChargeOnClickListener implements View.OnClickListener {

    Button startButton;
    Robot robot;
    MainActivity main;
    StateMachine stateMachine;
    MediaPlayer mp;

    /**
     * Initialises relevant member variables
     *
     * @param startButton  The button that the user should press.
     * @param robot        The singular robot instance.
     * @param main         The MainActivity of this program.
     * @param stateMachine The StateMachine which is running the routine.
     * @param mp           The MediaPlayer that is playing the ambient music.
     */
    public ReturnToChargeOnClickListener(Button startButton, Robot robot, MainActivity main, StateMachine stateMachine, MediaPlayer mp) {
        this.startButton = startButton;
        this.robot = robot;
        this.main = main;
        this.stateMachine = stateMachine;
        this.mp = mp;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        //format the button
        startButton.setEnabled(false);
        startButton.setText("Returning to the home base");
        main.updateThought("I am going back to the home base to charge");
        //goto the home base
        Log.d("LOCATION", robot.getLocations().get(0));
        robot.goTo(GlobalVariables.L_HOME_BASE);
        //start a LocationListener so we know when we reach the home base
        Log.d("LOCATION", "Going to Charging Station");
        robot.addOnGoToLocationStatusChangedListener(new ReturnToChargeLocationListener(robot, main, stateMachine, startButton, mp));

        //TODO start or continue mp here

    }
}
