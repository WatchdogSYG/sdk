package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.sample.MainActivity;
import com.robotemi.sdk.sample.R;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.UUID;

import flinderstemi.Global;
import flinderstemi.util.PauseTimerTask;

//TODO JDoc
public class IdleSpeechListener implements Robot.TtsListener {

    //references
    Robot robot;
    private long duration;

    //state
    int index;
    UUID uuid;
    boolean initialSpeech;

    Timer t;
    PauseTimerTask pause;

    //content
    String[] s;

    public IdleSpeechListener(Robot robot, MainActivity main) {
        Log.d(Global.SEQUENCE, "Constructing IdleSpeechListener");
        this.robot = robot;
        this.duration = Global.resources.getInteger(R.integer.pause_ms);

        Log.d(Global.SEQUENCE, "Instantiated Timer in IdleSpeechListener");
        t = new Timer();

        index = 0;
        s = main.getResources().getStringArray(R.array.ambientSpeech);
        TtsRequest tts = TtsRequest.create("I'm going to the next waypoint now. Goodbye.", false);
        initialSpeech = true;
        uuid = tts.getId();

        robot.speak(tts);
    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        //listen for the specific tts.
        if (!initialSpeech) {
            uuid = pause.getID();
        }
        Log.v(Global.LISTENER, "IdleSpeechListener \n\tCheck UUID\t=\t" + uuid.toString() + "\n\tTTSR.ID\t=\t" + ttsRequest.getId() + "\nbool\t=\t" + (uuid.compareTo(ttsRequest.getId()) == 0));
        if (uuid.compareTo(ttsRequest.getId()) == 0)
            switch (ttsRequest.getStatus()) {
                case COMPLETED:
                case NOT_ALLOWED:
                case ERROR:
                    //uuid = speakNext();
                    Log.d(Global.SEQUENCE, "initialSpeech = false");
                    initialSpeech = false;

                    Log.d(Global.SEQUENCE, "Instantiated TimerTask to pause between speech.");
                    pause = new PauseTimerTask(robot, s, uuid);
                    t.schedule(pause, duration);
                    break;/*
            case PROCESSING:
                break;
            case STARTED:
                break;
            case PENDING:
                break;*/
            }
        Log.d(Global.LISTENER, "IdleSpeechListener onTtsStatusChanged\n" + ttsRequest.toString());
    }

    /**
     *
     */
    public void cancelTimer() {
        t.cancel();
    }

}
