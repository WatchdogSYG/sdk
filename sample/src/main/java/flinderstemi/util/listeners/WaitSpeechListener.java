package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import flinderstemi.util.SetTextViewCallback;
import flinderstemi.util.StateMachine;

public class WaitSpeechListener implements Robot.TtsListener {

    Timer t;
    long duration;
    SetTextViewCallback stvc;
    StateMachine sm;
    Robot robot;
    TimerTask doneWaiting;
    DetectionListener dl;

    public WaitSpeechListener(long duration, SetTextViewCallback stvc, DetectionListener dl, final StateMachine sm, Robot robot) {
        System.out.println("FLINTEMI: Create Timer");
        t = new Timer();
        this.duration = duration;
        this.stvc = stvc;
        this.sm = sm;
        this.robot = robot;
        this.dl = dl;
        this.dl = dl;

        doneWaiting = new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                System.out.println("FLINTEMI: Waiting for 10 seconds completed:" + formatter.format(date));
                synchronized (sm) {
                    System.out.println("FLINTEMI: endWait(), notify()");
                    sm.notify();
                }
            }
        };
    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {

        //if the status of the current ttsrequest changes to COMPLETED, start waiting. Actually, this is not too important so we can just wait even if it fails
        //TODO move this back to the statemachine and make it wait for any trigger of an idle case

        if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED) {
            //TODO handle the cases where the TTSRequest fails into the NOT_ALLOWED or ERROR statuses
            System.out.println("FLINTEMI: TtsRequest.getStatus()=" + ttsRequest.getStatus() + ":" + ttsRequest.getSpeech());

            //schedule the task to be completed after the waiting period ends
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            System.out.println("FLINTEMI: Waiting for " + duration + "ms starting:" + formatter.format(date));

            //this is actually called on the UI thread so it doesnt freak out
            this.stvc.updateThought("Waiting ...");
            //schedule it
            t.schedule(doneWaiting, duration);

            //since we are on the UI thread already, we can activate the Detection listener now. The bot has already finished talking so this will not cause an interruption
            Log.d("Detection", "addOnDetectionStateChengedListener");
            robot.addOnDetectionStateChangedListener(dl);


            System.out.println("FLINTEMI: Removed WaitTTSListener");
            robot.removeTtsListener(this);
        }
    }
}