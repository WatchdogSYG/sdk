package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.sample.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import flinderstemi.Global;
import flinderstemi.StateMachine;

/**
 * //TODO JavaDoc
 * A listener that will check for the COMPLETED status of a global TtsRequest and then start a timer that wakes the State thread.
 */
public class WaitSpeechListener implements Robot.TtsListener {

    //arguments
    long duration;
    MainActivity main;
    DetectionListener dl;
    StateMachine sm;
    Robot robot;

    //members
    Timer t;
    TimerTask doneWaiting;

    /**
     * Initialises members and defines a TimerTask that will fire upon completion of the Timer.
     *
     * @param duration
     * @param main
     * @param dl
     * @param sm
     * @param robot
     */
    public WaitSpeechListener(long duration, MainActivity main, DetectionListener dl, final StateMachine sm, Robot robot) {

        t = new Timer();
        this.duration = duration;
        this.main = main;
        this.sm = sm;
        this.robot = robot;
        this.dl = dl;
        this.dl = dl;

        //define a timertask that waits for duration milliseconds and notifies after.
        doneWaiting = new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                Log.v(Global.SEQUENCE, "TimerTask doneWaiting run() triggered at " + formatter.format(date));
                Log.d(Global.SEQUENCE, "Waiting Completed");

                sm.removeDetectionListener();

                synchronized (sm) {
                    Log.d(Global.SEQUENCE, "synchronized " + sm.toString() + " notify()");
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

            Log.v(Global.LISTENER, "TtsRequest\n" +
                    "UUID\t=\t" + ttsRequest.getId() + "\n" +
                    "String\t=\t" + ttsRequest.getSpeech() + "\n" +
                    "Status\t=\tCOMPLETED");
            Log.d(Global.LISTENER, "TTSStatus changed to COMPLETED.");

            //schedule the task to be completed after the waiting period ends
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();


            //this is actually called on the UI thread so it doesn't freak out
            this.main.showPrompt();
            //schedule it
            Log.d(Global.SEQUENCE, "Waiting for " + duration + "ms starting: " + formatter.format(date));
            t.schedule(doneWaiting, duration);

            //since we are on the UI thread already, we can activate the Detection listener now. The bot has already finished talking so this will not cause an interruption
            sm.addDetectionListener();

            robot.removeTtsListener(this);
            Log.d(Global.LISTENER, "Removed WaitSpeechListener implements TtsListener");
            Log.v(Global.LISTENER, "Removed WaitSpeechListener: " + this.toString());
        }
    }
}