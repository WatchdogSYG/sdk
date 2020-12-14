package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;

import flinderstemi.StateMachine;

public class DetectionListener implements OnDetectionStateChangedListener {

    StateMachine stateMachine;
    Robot robot;

    public DetectionListener(Robot robot, StateMachine stateMachine) {
        System.out.println("FLINTEMI: Construct DetectionListener");
        this.stateMachine = stateMachine;
        this.robot = robot;
    }

    @Override
    public void onDetectionStateChanged(int state) {
        //note
        //final int IDLE = 0
        //final int LOST = 1
        //final int DETECTED = 2


        Log.d("onDetectionStateChanged", "state = " + state);
        if (state == DETECTED) {
            robot.constraintBeWith();
            Log.d(this.getClass().getName(), "onDetectionState = DETECTED");

            //remove this listener as we do not want it triggering again and interrupting itself, note that we will have to listen for the end of the above speech so we can re-add this listener
            robot.removeOnDetectionStateChangedListener(this);
            robot.addTtsListener(new InteractionSpeechListener(this, robot, stateMachine));
        } else {
            robot.stopMovement();
        }
    }
}
