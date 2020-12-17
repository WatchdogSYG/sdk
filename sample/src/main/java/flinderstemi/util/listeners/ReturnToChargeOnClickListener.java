package flinderstemi.util.listeners;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.sample.MainActivity;
import com.robotemi.sdk.sample.R;

import flinderstemi.StateMachine;
import flinderstemi.util.GlobalVariables;

/**
 * This listener should be used after the user is offered the choice to manually send the robot back to the home base.
 */
public class ReturnToChargeOnClickListener implements View.OnClickListener {

    MainActivity main;
    Robot robot;
    StateMachine stateMachine;
    Button startButton;
    MediaPlayer mp;

    /**
     * Initialises relevant member variables.
     *
     * @param main         The MainActivity of this program.
     * @param robot        The singular robot instance.
     * @param stateMachine The StateMachine which is running the routine.
     * @param startButton  The button that the user should press.
     * @param mp           The MediaPlayer that is playing the ambient music.
     */
    public ReturnToChargeOnClickListener(MainActivity main, Robot robot, StateMachine stateMachine, Button startButton, MediaPlayer mp) {
        this.main = main;
        this.robot = robot;
        this.stateMachine = stateMachine;
        this.startButton = startButton;
        this.mp = mp;
    }

    /**
     * Called when a view has been clicked. Sends the robot back to the home base to charge.
     * <p>
     * Formats the UI for feedback and  sends the robot to the home base. Adds a new <code>ReturnToChargeLocationListener</code>.Removes all OnClickListeners from the member <code>startButton</code>.
     * The @param View v should be startButton.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        //format the button
        startButton.setEnabled(false);
        startButton.setText(GlobalVariables.resources.getText(R.string.b_returning));
        main.updateThought(GlobalVariables.resources.getString(R.string.t_returning));

        //start a LocationListener so we know when we reach the home base
        ReturnToChargeLocationListener l = new ReturnToChargeLocationListener(main, robot, stateMachine, startButton, mp);
        robot.addOnGoToLocationStatusChangedListener(l);
        Log.d(GlobalVariables.LISTENER, "Added new ReturnToChargeLocationListener implements OnGoToLocationStatusChangedListener: " + l.toString());

        //goto the home base
        Log.v(GlobalVariables.LOCATION, "HB locations[0]\t=\t" + robot.getLocations().get(0));
        robot.goTo(GlobalVariables.L_HOME_BASE);
        Log.i(GlobalVariables.LOCATION, "Going to Charging Station");

        String s = this.toString();//is this needed? does an OnclickListener's toString() return value change when removed from a View?
        startButton.setOnClickListener(null);
        Log.d(GlobalVariables.LISTENER, "Removed OnClickListener from startButton: " + s.toString());
    }
}
