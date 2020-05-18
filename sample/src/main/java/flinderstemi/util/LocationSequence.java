package flinderstemi.util;

//TODO this javadoc
/**
 * JDoc
 */

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.List;

public class LocationSequence implements Runnable {

    Robot robot;
    List<TtsRequest> a;
    int index;
    int speechIndex;
    private boolean completeSpeechSub;

    public boolean isCompleteSpeechSub() {
        return completeSpeechSub;
    }

    public LocationSequence(Robot robot) {
        System.out.println("Constructor");
        completeSpeechSub = false;
        this.robot = robot;
        index = 0;
        speechIndex = 0;

        a = new ArrayList<TtsRequest>();
        a.add(TtsRequest.create("Starting custom routine. Beep boop.", true));
        a.add(TtsRequest.create("I will start my patrol.", true));
        a.add(TtsRequest.create("TTS Request 1", true));
        a.add(TtsRequest.create("TTS Request 2", true));
        a.add(TtsRequest.create("TTS Requests complete.", true));
        System.out.println("End constructor");
    }

    private void speech(int i) {
        robot.speak(a.get(i));
    }

    private void nextAction() {
        //don't want to use reflection yet
        switch (index) {
            case 0:
                speech(speechIndex);
                speechIndex++;
                break;
            case 1:
                speech(speechIndex);
                completeSpeechSub = true;
                break;
            case 2:
                robot.goTo(robot.getLocations().get(0));
                break;
            case 3:

                break;
            default:

                break;
        }
    }

    @Override
    public void run() {
        System.out.println("Run");
        for (; index < 3; index++) {
            System.out.println("Speak0");
            nextAction();
            System.out.println("End Speak0");
            System.out.println("Synch routine");
            synchronized (this) {
                try {
                    System.out.println("Try wait");
                    wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }

        }
        robot.speak(TtsRequest.create("Routine Complete.", true));

    }
}
