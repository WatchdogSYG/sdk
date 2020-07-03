package flinderstemi.util;

//TODO this javadoc
/**
 * JDoc
 */

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StateMachine implements Runnable {

    //robot and state variables
    Robot robot;
    int state;

    //lists of actions
    List<TtsRequest> speechQueue;
    List<String> locations;

    //state sentinel indices
    int speechIndex;
    int locationIndex;
    int locationLoopIndex;
    final int maxPatrolLoops = 100;
    final int initialState = 0;

    public void setWakeCondition(String[] wakeCondition) {
        this.wakeCondition = wakeCondition;
    }

    public String[] wakeCondition;

    //send messages to other thread through these variables
    private boolean completeSpeechSub;
    private boolean completePatrolSub;
    private boolean waiting;

    public boolean isCompleteSpeechSub() {
        return completeSpeechSub;
    }

    public boolean isCompletePatrolSub() {
        return completePatrolSub;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public void waitFor(long millis) {
        TimerTask doneWaiting = new TimerTask() {
            @Override
            public void run() {
                System.out.println("FLINTEMI; done waiting");
                endWait();
            }
        };

        Timer t = new Timer();
        System.out.println("FLINTEMI: Start timer");
        t.schedule(doneWaiting, millis);
    }

    private void endWait() {
        System.out.println("FLINTEMI: state=patrol");
        synchronized (this) {
            state = 1;
            System.out.println("FLINTEMI: state set to " + state);
            notify();
        }
    }

    public void wake() {
        switch (wakeCondition[0]) {
            case "TTS":
                switch (wakeCondition[1]) {
                    case "COMPLETED":
                        speechIndex++;
                        break;
                }
                break;
            case "LOCATION":
                switch (wakeCondition[1]) {
                    case "COMPLETE":

                        int prev = locationIndex;
                        locationIndex++;
                        if (locationIndex >= locations.size()) {
                            System.out.println("FLINTEMI: Reset the location index to 1 from " + prev);
                            locationIndex = 1;//set it to the first location (0==homebase)

                            System.out.println("FLINTEMI: Loops complete=" + locationLoopIndex);
                            locationLoopIndex++;
                        }
                        break;
                }
                break;
            default:
                System.out.println("FLINTEMI: default wake switch");
                break;
        }
    }

    public StateMachine(Robot robot) {
        System.out.println("FLINTEMI: InitialisationSequence Constructor");
        completeSpeechSub = false;
        completePatrolSub = false;
        this.robot = robot;
        state = initialState;
        speechIndex = 0;
        locationIndex = 1;//not homebase at 0
        locationLoopIndex = 0;

        speechQueue = new ArrayList<TtsRequest>();
        speechQueue.add(TtsRequest.create("Starting custom routine. Beep boop.", true));
        speechQueue.add(TtsRequest.create("I will start my patrol.", true));

        locations = robot.getLocations();
        System.out.println("FLINTEMI Locations=" + locations.toString());
        System.out.println("End constructor");
    }

    private void speech(int i) {
        robot.speak(speechQueue.get(i));
    }

    private void nextAction() {
        //don't want to use reflection yet
        switch (state) {


            case 0://initial speaks
                System.out.println("FLINTEMI: speak line at speechIndex=" + speechIndex);
                speech(speechIndex);
                System.out.println("FLINTEMI: speechIndex++");
                System.out.println("FLINTEMI: speechIndex=" + speechIndex);
                if (speechIndex >= speechQueue.size() - 1) {
                    //we have reached the end of the ttsrequest queue
                    System.out.println("FLINTEMI: completeSpeechSub set to true");
                    completeSpeechSub = true; //this will remove the ttsstatuslistener on the main thread once this goes into the waiting state
                    state = 1;
                }
                break;


            case 1://patrolling
                //TODO fix the slight synch problem where the Routine complete message occurs when the last goto is started and not when it is finished
                //TODO print message when no locations are saved as this throws an indexoutofbounds exception
                System.out.println("FLINTEMI: going to location=" + locationIndex + ", name=" + locations.get(locationIndex));
                robot.goTo(locations.get(locationIndex));
                state = 2;
                //if there are no more loops to be done, go to next state
                if (locationLoopIndex >= maxPatrolLoops - 1) {
                    System.out.println("FLINTEMI: Completed all loops");
                    completePatrolSub = true;
                    state = 3;
                }
                break;


            case 2://stop and wait
                System.out.println("FLINTEMI: Waiting for 5 seconds.");
                waitFor(10000L);
                break;


            case 3://terminate

                break;
            default:

                break;
        }
    }

    @Override
    public void run() {
        System.out.println("FLINTEMI: Run");
        //first index is 0

        while (state != 3) {
            System.out.println("FLINTEMI: Do action at state=" + state);
            nextAction();
            System.out.println("FLINTEMI: Finished action at state=" + state);//TODO fix print bug on state change
            System.out.println("FLINTEMI: synchronise routine");

            synchronized (this) {
                try {
                    System.out.println("FLINTEMI: try state machine wait");
                    wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                wake();
            }
        }
        robot.speak(TtsRequest.create("Routine Complete.", true));
    }
}
