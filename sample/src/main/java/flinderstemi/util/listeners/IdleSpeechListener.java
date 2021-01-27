package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.sample.MainActivity;
import com.robotemi.sdk.sample.R;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import flinderstemi.Global;

//TODO JDoc
public class IdleSpeechListener implements Robot.TtsListener {

    //references
    Robot robot;

    //state
    int index;
    UUID uuid;

    //content
    String[] s;

    public IdleSpeechListener(Robot robot, MainActivity main) {
        Log.d(Global.SEQUENCE, "Constructing IdleSpeechListener");
        this.robot = robot;

        index = 0;
        s = main.getResources().getStringArray(R.array.ambientSpeech);
        TtsRequest tts = TtsRequest.create(s[0], false);
        uuid = tts.getId();


    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        //listen for the specific tts.
        switch (ttsRequest.getStatus()) {
            case COMPLETED:
            case NOT_ALLOWED:
            case ERROR:
                //uuid = speakNext();
                uuid = speakRandom();
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

    //speaks a random line in the array and returns the TtsRequest UUID
    @NotNull
    private UUID speakRandom() {
        double d = Math.random();
        d = d * s.length;
        int i = (int) d;
        Log.v(Global.STATE, Integer.toString(i));
        TtsRequest tts = TtsRequest.create(s[i] + "...", false);
        robot.speak(tts);
        return tts.getId();
    }

    /*
    //speaks the next line in the array and returns the TtsRequest UUID.
    @NotNull
    private UUID speakNext() {
        if (index == s.length - 1) {
            index = 0;
        } else {
            index++;
        }
        TtsRequest tts = TtsRequest.create(s[index], false);
        robot.speak(tts);
        return tts.getId();
    }*/
}
