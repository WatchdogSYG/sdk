package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.NotNull;

import flinderstemi.Global;
import flinderstemi.StateMachine;

public class PatrolLocationListener implements OnGoToLocationStatusChangedListener {
    StateMachine stateMachine;
    Robot robot;
    MainActivity main;

    public PatrolLocationListener(Robot r, StateMachine stateMachine, MainActivity main) {
        Log.d(Global.LISTENER, "Constructing patrolLocationListener");
        this.stateMachine = stateMachine;
        this.robot = r;
        this.main = main;
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {
        Log.v(Global.LOCATION, "onGoToLocationStatusChanged:\n" +
                "location\t=\t" + location + "\n" +
                "status\t\t=\t" + status + "\n" +
                "description\t=\t" + description);
        synchronized (stateMachine) {
            switch (status) {
                case OnGoToLocationStatusChangedListener.COMPLETE:
                    main.enableLanguageSwitching();
                    System.out.println("FLINTEMI: OnGoToLocationStatusChanged=COMPLETE,notify");
                    stateMachine.setWakeCondition(new String[]{"LOCATION", "COMPLETE"});
                    stateMachine.removeISL();
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
                    stateMachine.removeISL();
                    robot.speak(TtsRequest.create("Abort", false));
                    stateMachine.setWakeCondition(new String[]{"LOCATION", "ABORT"});
                    stateMachine.notify();
                    break;
            }
        }
    }
}