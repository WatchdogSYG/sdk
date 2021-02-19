package flinderstemi.util;

import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import org.jetbrains.annotations.NotNull;

import java.util.TimerTask;
import java.util.UUID;

import flinderstemi.Global;

public class PauseTimerTask extends TimerTask {
    Robot robot;
    String[] s;
    UUID uuid;

    public PauseTimerTask(Robot robot, String[] s, UUID uuid) {
        this.robot = robot;
        this.s = s;
        this.uuid = uuid;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        uuid = speakRandom();
        Log.d(Global.SEQUENCE, "Speak random");
    }

    //speaks a random line in the array and returns the TtsRequest UUID
    @NotNull
    private UUID speakRandom() {
        double d = Math.random();
        d = d * s.length;
        int i = (int) d;
        Log.v(Global.STATE, Integer.toString(i));
        TtsRequest tts = TtsRequest.create(s[i], false);
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

    /**
     * Getter for the UUID of the spoken TtsRequest
     *
     * @return uuid the UUID of the current TtsRequest to listen for.
     */
    public UUID getID() {
        return uuid;
    }
}
