package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import org.jetbrains.annotations.NotNull;

import flinderstemi.util.StateMachine;

/**
 * This class should be added and removed from the Robot as needed when any TTSRequest needs temporary listening to.
 */
public class InteractionSpeechListener implements Robot.TtsListener {
    Robot robot;
    StateMachine sm;
    DetectionListener dl;

    public InteractionSpeechListener(DetectionListener dl, Robot robot, StateMachine sm) {
        this.robot = robot;
        this.sm = sm;
        this.dl = dl;

        Log.d("Interaction", "speak greeting line");

        robot.speak(TtsRequest.create("Hello there, feel free to use my touchless sanitiser dispenser. Maintaining good hand hygiene is an important measure we can take to prevent infectious diseases from spreading.", false));
    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        switch (ttsRequest.getStatus()) {
            case COMPLETED:
                Log.d("Interaction", "COMPLETED");
                //robot.removeTtsListener(this);
                //robot.addOnDetectionStateChangedListener(dl);
                break;
            case ERROR:
                Log.d("Interaction", "ERROR");
                break;
            case PENDING:
                Log.d("Interaction", "PENDING");
                break;
            case STARTED:
                Log.d("Interaction", "STARTED");
                break;
            case PROCESSING:
                Log.d("Interaction", "PROCESSING");
                break;
            case NOT_ALLOWED:
                Log.d("Interaction", "NOT_ALLOWED");
                break;
        }

    }
}
