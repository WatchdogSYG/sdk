package flinderstemi.listeners;
//TODO handle cancelled and abort statuses due to "Hey, Temi" wakeups

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import org.jetbrains.annotations.NotNull;

import flinderstemi.util.StateMachine;

public class TTSListener implements Robot.TtsListener {
    StateMachine stateMachine;
    Robot robot;

    public TTSListener(Robot robot, StateMachine stateMachine) {
        System.out.println("FLINTEMI: Construct ttsListener");
        this.stateMachine = stateMachine;
        this.robot = robot;
    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        synchronized (stateMachine) {
            //if the status of the current ttsrequest changes to COMPLETED
            System.out.println("FLINTEMI: TtsRequest.getStatus()=" + ttsRequest.getStatus() + ":" + ttsRequest.getSpeech());
            //producer condition
            stateMachine.setTtsStatus(ttsRequest.getStatus());
            notify();
        }
    }
}