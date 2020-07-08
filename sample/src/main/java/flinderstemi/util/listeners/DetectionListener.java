package flinderstemi.util.listeners;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;

import flinderstemi.util.StateMachine;

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
        System.out.println(state);
        switch (state) {
            case OnDetectionStateChangedListener.IDLE:
                stateMachine.setWakeCondition(new String[]{"DETECTION", "IDLE"});
                System.out.println("IDLE");
                break;
            case OnDetectionStateChangedListener.LOST:
                stateMachine.setWakeCondition(new String[]{"DETECTION", "LOST"});
                System.out.println("LOST");
                break;
            case OnDetectionStateChangedListener.DETECTED:
                stateMachine.setWakeCondition(new String[]{"DETECTION", "DETECTED"});
                System.out.println("DETECTED");
                break;
        }
    }
}
