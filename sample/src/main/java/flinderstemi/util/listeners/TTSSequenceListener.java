package flinderstemi.util.listeners;

//TODO handle cancelled and abort statuses due to "Hey, Temi" wakeups

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.NotNull;

import flinderstemi.StateMachine;

public class TTSSequenceListener implements Robot.TtsListener {
    StateMachine stateMachine;
    Robot robot;
    MainActivity main;

    public TTSSequenceListener(Robot robot, StateMachine stateMachine, MainActivity main) {
        System.out.println("FLINTEMI: Construct TTSSequenceListener");
        this.stateMachine = stateMachine;
        this.robot = robot;
        this.main = main;
    }

    //TODO move the synchronised block to the StateMachine class, keep this one simple
    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        synchronized (stateMachine) {
            //if the status of the current ttsrequest changes to COMPLETED
            System.out.println("FLINTEMI: TtsRequest.getStatus()=" + ttsRequest.getStatus() + ":" + ttsRequest.getSpeech());

            switch (ttsRequest.getStatus()) {
                case COMPLETED:
                    stateMachine.setWakeCondition(new String[]{"TTS", "COMPLETED"});
                    System.out.println("FLINTEMI: ttsReqeustStatus=COMPLETED,notify");
                    stateMachine.notify();
                    //if the speech routine is complete, remove the listener
                    if (stateMachine.isCompleteSpeechSub()) {
                        System.out.println("FLINTEMI: ttsRequestStatus=COMPLETED,stateMachine.isCompleteSub=true,notify");
                        stateMachine.notify();
                        System.out.println("FLINTEMI: addOnGoToLocationStatusChangedListener");
                        stateMachine.setPLL(new PatrolLocationListener(robot, stateMachine, main));
                        robot.removeTtsListener(this);
                        System.out.println("FLINTEMI: ttsListenerRemoved");
                    }
                    break;
                case ERROR:
                    //display error on textarea

                    //try again
                    stateMachine.setWakeCondition(new String[]{"TTS", "ERROR"});
                    stateMachine.notify();
                    break;
                case NOT_ALLOWED:
                    stateMachine.setWakeCondition(new String[]{"TTS", "NOT_ALLOWED"});
                    stateMachine.notify();
                    break;
                default:
            }

        }
    }
}