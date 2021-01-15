package flinderstemi.util.listeners;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import flinderstemi.GlobalVariables;

public class IdleSpeechListener implements Robot.TtsListener {

    //references
    Robot robot;

    //state
    int index;
    UUID uuid;

    //content
    String[] s;

    public IdleSpeechListener(Robot robot) {
        Log.d(GlobalVariables.SEQUENCE, "Constructing IdleSpeechListener");
        this.robot = robot;

        index = 0;
        s = new String[]{"Watch out", "Beep beep coming through", "i like donuts"};
        TtsRequest tts = TtsRequest.create(s[index], false);
        uuid = tts.getId();


    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
        //listen for the specific tts.
        switch (ttsRequest.getStatus()) {
            case COMPLETED:
                //uuid = speakNext();
                Log.v(GlobalVariables.STATE, ttsRequest.getStatus().toString());
                uuid = speakRandom();
                break;
            case NOT_ALLOWED:
                break;
            case PROCESSING:
                break;
            case ERROR:
                break;
            case STARTED:
                break;
            case PENDING:
                break;
        }
        Log.d(GlobalVariables.LISTENER, "IdleSpeechListener onTtsStatusChanged\n" + ttsRequest.toString());
    }

    //speaks a random line in the array and returns the TtsRequest UUID
    private UUID speakRandom() {
        double d = Math.random();
        d = d * s.length;
        int i = (int) d;
        Log.v(GlobalVariables.STATE, Integer.toString(i));
        TtsRequest tts = TtsRequest.create(s[i], false);
        robot.speak(tts);
        return tts.getId();
    }

    //speaks the next line in the array and returns the TtsRequest UUID.
    private UUID speakNext() {
        if (index == s.length - 1) {
            index = 0;
        } else {
            index++;
        }
        TtsRequest tts = TtsRequest.create(s[index], false);
        robot.speak(tts);
        return tts.getId();
    }
}
