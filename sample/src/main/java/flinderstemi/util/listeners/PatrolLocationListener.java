package flinderstemi.util.listeners;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;

import org.jetbrains.annotations.NotNull;

import flinderstemi.util.StateMachine;

public class PatrolLocationListener implements OnGoToLocationStatusChangedListener {
    StateMachine stateMachine;
    Robot robot;

    public PatrolLocationListener(Robot r, StateMachine stateMachine) {
        System.out.println("FLINTEMI: Construct patrolLocationListener");
        this.stateMachine = stateMachine;
        this.robot = r;
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {
        System.out.println("FLINTEMI: onGoToLocationStatusChanged:location=" + location + ",status=" + status + ",description=" + description);
        synchronized (stateMachine) {
            switch (status) {
                case OnGoToLocationStatusChangedListener.COMPLETE:
                    System.out.println("FLINTEMI: OnGoToLocationStatusChanged=COMPLETE,notify");
                    stateMachine.setWakeCondition(new String[]{"LOCATION", "COMPLETE"});
                    stateMachine.notify();
                    //if the patrol routine is complete, remove the listener
                    if (stateMachine.isCompletePatrolSub()) {
                        System.out.println("FLINTEMI: OnGoToLocationStatusChanged=COMPLETED,stateMachine.isCompleteSub=true,notify");
                        stateMachine.setWakeCondition(new String[]{"LOCATION", "COMPLETE"});
                        stateMachine.notify();
                        //would add the next listener here
                        robot.removeOnGoToLocationStatusChangedListener(this);
                        System.out.println("FLINTEMI: OnGoToLocationStatusChangedListenerRemoved");
                    }
                    break;
                case OnGoToLocationStatusChangedListener.ABORT:
                    robot.speak(TtsRequest.create("Abort", false));
                    stateMachine.setWakeCondition(new String[]{"LOCATION", "ABORT"});
                    stateMachine.notify();
                    break;
            }
        }
    }
}