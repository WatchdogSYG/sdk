package flinderstemi.util;

//TODO this javadoc
/**
 * JDoc
 */

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.List;

import flinderstemi.listeners.TTSListener;

public class StateMachine implements Runnable {

    final int PAUSED = 0;
    final int EXIT = 1;
    final int TALKING = 2;
    final int MOVING = 3;
    final int WAITING = 4;
    final int FOLLOWING = 5;
    final int LISTENING = 6;
    final int RETURNING = 7;

    public TalkingState ts;

    //robot and state variables
    Robot robot;
    int state;

    //lists of actions
    List<String> speechQueue;
    List<String> locations;

    final int INITIAL_STATE = TALKING;

    public TtsRequest.Status ttsStatus;

    public void setTtsStatus(TtsRequest.Status ttsStatus) {
        this.ttsStatus = ttsStatus;
    }

    public StateMachine(Robot robot) {
        System.out.println("FLINTEMI: InitialisationSequence Constructor");
        this.robot = robot;
        state = INITIAL_STATE;

        speechQueue = new ArrayList<String>();
        speechQueue.add("1");
        speechQueue.add("2");
        speechQueue.add("3");

        locations = robot.getLocations();
        System.out.println("FLINTEMI Locations=" + locations.toString());
        System.out.println("End constructor");
    }

    //must be called bu UI thread
    public TTSListener initialiseTalkingState(TTSListener tTSListener) {
        ts = new TalkingState(speechQueue, tTSListener);
        return tTSListener;
    }

    ;

    private void nextAction() {
        //state switching status goes here
        switch (state) {
            case PAUSED:

                break;
            case EXIT:

                break;
            case TALKING:
                ts.hearTTS(ttsStatus);
                break;
            case MOVING:

                break;
            case WAITING:
                break;
            case FOLLOWING:
                break;
            case LISTENING:
                break;
            case RETURNING:
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        System.out.println("FLINTEMI: Run");

        while (state != EXIT) {
            System.out.println("FLINTEMI: Do action at index=" + state);
            nextAction();
            System.out.println("FLINTEMI: Finished action at index=" + state);//TODO fix print bug on state change
            System.out.println("FLINTEMI: synchronise routine");

            synchronized (this) {
                try {
                    System.out.println("FLINTEMI: try state machine wait");
                    wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        robot.speak(TtsRequest.create("Routine Complete.", true));
    }

    //TODO enumerate states or turn into constants
    public enum states {
        PATROLLING,
        EXIT
    }
}
